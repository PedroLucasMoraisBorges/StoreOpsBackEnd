package com.store_ops_backend.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.store_ops_backend.models.dtos.CreateStockMovementDTO;
import com.store_ops_backend.models.dtos.StockItemResponseDTO;
import com.store_ops_backend.models.dtos.StockMovementResponseDTO;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.Product;
import com.store_ops_backend.models.entities.ProductComponentOption;
import com.store_ops_backend.models.entities.ProductVariant;
import com.store_ops_backend.models.entities.StockItem;
import com.store_ops_backend.models.entities.StockMovement;
import com.store_ops_backend.models.entities.User;
import com.store_ops_backend.repositories.CompanyRepository;
import com.store_ops_backend.repositories.ProductComponentOptionRepository;
import com.store_ops_backend.repositories.ProductRepository;
import com.store_ops_backend.repositories.ProductVariantRepository;
import com.store_ops_backend.repositories.StockItemRepository;
import com.store_ops_backend.repositories.StockMovementRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class StockService {

    @Autowired private StockItemRepository stockItemRepository;
    @Autowired private StockMovementRepository movementRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductVariantRepository variantRepository;
    @Autowired private ProductComponentOptionRepository componentOptionRepository;
    @Autowired private CompanyRepository companyRepository;

    public List<StockItemResponseDTO> listStock(String companyId) {
        return stockItemRepository.findByCompanyIdOrderByProductNameAsc(companyId)
            .stream().map(this::toItemResponse).toList();
    }

    public List<StockItemResponseDTO> listBelowMinimum(String companyId) {
        return stockItemRepository.findBelowMinimum(companyId)
            .stream().map(this::toItemResponse).toList();
    }

    public List<StockMovementResponseDTO> listMovements(String companyId) {
        return movementRepository.findByCompanyIdOrderByCreatedAtDesc(companyId)
            .stream().map(this::toMovementResponse).toList();
    }

    public List<StockMovementResponseDTO> listMovementsByProduct(String companyId, String productId) {
        List<StockItem> items = stockItemRepository.findAllByProductIdAndCompanyId(productId, companyId);
        if (items.isEmpty()) throw new EntityNotFoundException("Item de estoque não encontrado");
        List<String> itemIds = items.stream().map(StockItem::getId).toList();
        return movementRepository.findByStockItemIdInOrderByCreatedAtDesc(itemIds)
            .stream().map(this::toMovementResponse).toList();
    }

    @Transactional
    public StockMovementResponseDTO registerMovement(String companyId, CreateStockMovementDTO data, User user) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada"));

        StockItem stockItem = resolveStockItem(data, company);

        BigDecimal delta = switch (data.type()) {
            case "ENTRADA" -> data.quantity();
            case "SAIDA"   -> data.quantity().negate();
            case "AJUSTE"  -> data.quantity().subtract(stockItem.getQuantity());
            default        -> throw new IllegalArgumentException("Tipo de movimentação inválido: " + data.type());
        };

        if (stockItem.getQuantity().add(delta).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantidade em estoque não pode ser negativa");
        }

        stockItem.applyMovement(delta);
        stockItemRepository.save(stockItem);

        StockMovement movement = new StockMovement(stockItem, company, user, data.type(), data.quantity(), data.notes());
        return toMovementResponse(movementRepository.save(movement));
    }

    private StockItem resolveStockItem(CreateStockMovementDTO data, Company company) {
        // Component option level
        if (data.componentOptionId() != null && !data.componentOptionId().isBlank()) {
            ProductComponentOption option = componentOptionRepository.findById(data.componentOptionId())
                .orElseThrow(() -> new EntityNotFoundException("Opção de componente não encontrada"));
            Product product = option.getGroup().getProduct();
            return stockItemRepository
                .findComponentLevelStock(option.getId(), company.getId())
                .orElseGet(() -> stockItemRepository.save(new StockItem(product, option, company, BigDecimal.ZERO)));
        }

        // Product must be provided for product/variant level
        if (data.productId() == null || data.productId().isBlank()) {
            throw new IllegalArgumentException("ID do produto ou da opção de componente é obrigatório");
        }
        Product product = productRepository.findByIdAndCompanyId(data.productId(), company.getId())
            .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        // Variant level
        if (data.variantId() != null && !data.variantId().isBlank()) {
            ProductVariant variant = variantRepository.findById(data.variantId())
                .orElseThrow(() -> new EntityNotFoundException("Variante não encontrada"));
            return stockItemRepository
                .findVariantLevelStock(data.productId(), variant.getId(), company.getId())
                .orElseGet(() -> stockItemRepository.save(new StockItem(product, variant, company, BigDecimal.ZERO)));
        }

        // Product level (no variant, no component)
        return stockItemRepository
            .findProductLevelStock(data.productId(), company.getId())
            .orElseGet(() -> stockItemRepository.save(new StockItem(product, (ProductVariant) null, company, BigDecimal.ZERO)));
    }

    @Transactional
    public StockItemResponseDTO setMinQuantity(String companyId, String productId, BigDecimal minQuantity, String variantId, String componentOptionId) {
        StockItem item;
        if (componentOptionId != null && !componentOptionId.isBlank()) {
            item = stockItemRepository.findComponentLevelStock(componentOptionId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Item de estoque não encontrado"));
        } else if (variantId != null && !variantId.isBlank()) {
            item = stockItemRepository.findVariantLevelStock(productId, variantId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Item de estoque não encontrado"));
        } else {
            item = stockItemRepository.findProductLevelStock(productId, companyId)
                .orElseThrow(() -> new EntityNotFoundException("Item de estoque não encontrado"));
        }
        item.updateMinQuantity(minQuantity);
        return toItemResponse(stockItemRepository.save(item));
    }

    public StockItemResponseDTO toItemResponse(StockItem s) {
        String variantId = s.getVariant() != null ? s.getVariant().getId() : null;
        String variantName = s.getVariant() != null ? s.getVariant().getName() : null;
        String componentOptionId = s.getComponentOption() != null ? s.getComponentOption().getId() : null;
        String componentOptionName = s.getComponentOption() != null ? s.getComponentOption().getName() : null;
        return new StockItemResponseDTO(
            s.getId(), s.getProduct().getId(), s.getProduct().getName(),
            s.getProduct().getCategory(), s.getProduct().getUnit(),
            s.getQuantity(), s.getMinQuantity(), s.isBelowMinimum(),
            s.getProduct().getSellPrice(), s.getUpdatedAt(),
            variantId, variantName,
            componentOptionId, componentOptionName
        );
    }

    private StockMovementResponseDTO toMovementResponse(StockMovement m) {
        String variantName = m.getStockItem().getVariant() != null
            ? m.getStockItem().getVariant().getName() : null;
        String componentOptionName = m.getStockItem().getComponentOption() != null
            ? m.getStockItem().getComponentOption().getName() : null;
        return new StockMovementResponseDTO(
            m.getId(), m.getType(), m.getQuantity(), m.getNotes(),
            m.getStockItem().getProduct().getName(),
            variantName != null ? variantName : componentOptionName,
            m.getUser().getId(), m.getCreatedAt()
        );
    }
}
