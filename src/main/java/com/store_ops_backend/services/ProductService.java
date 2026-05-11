package com.store_ops_backend.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.store_ops_backend.models.dtos.CreateProductDTO;
import com.store_ops_backend.models.dtos.ProductComponentGroupDTO;
import com.store_ops_backend.models.dtos.ProductComponentOptionDTO;
import com.store_ops_backend.models.dtos.ProductExtraDTO;
import com.store_ops_backend.models.dtos.ProductResponseDTO;
import com.store_ops_backend.models.dtos.ProductVariantDTO;
import com.store_ops_backend.models.dtos.UpdateProductDTO;
import com.store_ops_backend.models.entities.Company;
import com.store_ops_backend.models.entities.Product;
import com.store_ops_backend.models.entities.ProductComponentGroup;
import com.store_ops_backend.models.entities.ProductComponentOption;
import com.store_ops_backend.models.entities.ProductExtra;
import com.store_ops_backend.models.entities.ProductVariant;
import com.store_ops_backend.repositories.CompanyRepository;
import com.store_ops_backend.repositories.ProductComponentGroupRepository;
import com.store_ops_backend.repositories.ProductComponentOptionRepository;
import com.store_ops_backend.repositories.ProductExtraRepository;
import com.store_ops_backend.repositories.ProductRepository;
import com.store_ops_backend.repositories.ProductVariantRepository;
import com.store_ops_backend.repositories.StockItemRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private ProductExtraRepository extraRepository;

    @Autowired
    private ProductComponentGroupRepository componentGroupRepository;

    @Autowired
    private ProductComponentOptionRepository componentOptionRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    public ProductResponseDTO create(String companyId, CreateProductDTO data) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada"));

        Product product = new Product(
            company, data.name(), data.category(), data.unit(),
            null, data.sellPrice()
        );
        return toResponse(productRepository.save(product));
    }

    public List<ProductResponseDTO> listAll(String companyId) {
        return productRepository.findByCompanyIdOrderByNameAsc(companyId)
            .stream().map(this::toResponse).toList();
    }

    public List<ProductResponseDTO> listActive(String companyId) {
        return productRepository.findByCompanyIdAndActiveOrderByNameAsc(companyId, true)
            .stream().map(this::toResponse).toList();
    }

    public ProductResponseDTO getById(String companyId, String productId) {
        return toResponse(findOrThrow(companyId, productId));
    }

    public ProductResponseDTO update(String companyId, String productId, UpdateProductDTO data) {
        Product product = findOrThrow(companyId, productId);
        product.update(data.name(), data.category(), data.unit(),
            null, data.sellPrice(), data.active());
        return toResponse(productRepository.save(product));
    }

    public ProductResponseDTO uploadImage(String companyId, String productId, MultipartFile file) {
        Product product = findOrThrow(companyId, productId);
        String ext = getExtension(file.getOriginalFilename());
        String filename = "product_" + productId + "_" + UUID.randomUUID() + ext;
        Path uploadDir = Paths.get("uploads", "products");
        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), uploadDir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar imagem", e);
        }
        product.updateImage("/uploads/products/" + filename);
        return toResponse(productRepository.save(product));
    }

    public void delete(String companyId, String productId) {
        Product product = findOrThrow(companyId, productId);
        productRepository.delete(product);
    }

    // --- Variants ---

    public ProductVariantDTO addVariant(String companyId, String productId, String name, BigDecimal priceDelta) {
        Product product = findOrThrow(companyId, productId);
        ProductVariant variant = new ProductVariant(product, name, priceDelta);
        variantRepository.save(variant);
        return new ProductVariantDTO(variant.getId(), variant.getName(), variant.getPriceDelta(), variant.getActive(), null);
    }

    public void deleteVariant(String companyId, String productId, String variantId) {
        findOrThrow(companyId, productId);
        ProductVariant variant = variantRepository.findById(variantId)
            .orElseThrow(() -> new EntityNotFoundException("Variante não encontrada"));
        if (!variant.getProduct().getId().equals(productId)) {
            throw new EntityNotFoundException("Variante não pertence a este produto");
        }
        variantRepository.delete(variant);
    }

    // --- Extras ---

    public ProductExtraDTO addExtra(String companyId, String productId, String name, BigDecimal price) {
        Product product = findOrThrow(companyId, productId);
        ProductExtra extra = new ProductExtra(product, name, price);
        extraRepository.save(extra);
        return new ProductExtraDTO(extra.getId(), extra.getName(), extra.getPrice(), extra.getActive());
    }

    public void deleteExtra(String companyId, String productId, String extraId) {
        findOrThrow(companyId, productId);
        ProductExtra extra = extraRepository.findById(extraId)
            .orElseThrow(() -> new EntityNotFoundException("Extra não encontrado"));
        if (!extra.getProduct().getId().equals(productId)) {
            throw new EntityNotFoundException("Extra não pertence a este produto");
        }
        extraRepository.delete(extra);
    }

    // --- Component Groups ---

    public ProductComponentGroupDTO addComponentGroup(String companyId, String productId,
                                                      String name, int maxSelections, boolean required) {
        Product product = findOrThrow(companyId, productId);
        ProductComponentGroup group = new ProductComponentGroup(product, name, maxSelections, required);
        componentGroupRepository.save(group);
        List<ProductComponentOptionDTO> opts = List.of();
        return new ProductComponentGroupDTO(group.getId(), group.getName(), group.getMaxSelections(),
            group.isRequired(), group.getActive(), opts);
    }

    public void deleteComponentGroup(String companyId, String productId, String groupId) {
        findOrThrow(companyId, productId);
        ProductComponentGroup group = componentGroupRepository.findById(groupId)
            .orElseThrow(() -> new EntityNotFoundException("Grupo de componentes não encontrado"));
        if (!group.getProduct().getId().equals(productId)) {
            throw new EntityNotFoundException("Grupo não pertence a este produto");
        }
        componentGroupRepository.delete(group);
    }

    // --- Component Options ---

    public ProductComponentOptionDTO addComponentOption(String companyId, String productId,
                                                        String groupId, String name) {
        findOrThrow(companyId, productId);
        ProductComponentGroup group = componentGroupRepository.findByIdAndProductId(groupId, productId)
            .orElseThrow(() -> new EntityNotFoundException("Grupo de componentes não encontrado"));
        ProductComponentOption option = new ProductComponentOption(group, name);
        componentOptionRepository.save(option);
        return new ProductComponentOptionDTO(option.getId(), option.getName(), null);
    }

    public void deleteComponentOption(String companyId, String productId, String groupId, String optionId) {
        findOrThrow(companyId, productId);
        componentGroupRepository.findByIdAndProductId(groupId, productId)
            .orElseThrow(() -> new EntityNotFoundException("Grupo de componentes não encontrado"));
        ProductComponentOption option = componentOptionRepository.findById(optionId)
            .orElseThrow(() -> new EntityNotFoundException("Opção não encontrada"));
        if (!option.getGroup().getId().equals(groupId)) {
            throw new EntityNotFoundException("Opção não pertence a este grupo");
        }
        componentOptionRepository.delete(option);
    }

    // --- Helpers ---

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf('.'));
    }

    private Product findOrThrow(String companyId, String productId) {
        return productRepository.findByIdAndCompanyId(productId, companyId)
            .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
    }

    public ProductResponseDTO toResponse(Product product) {
        return toResponse(product, null);
    }

    public ProductResponseDTO toResponse(Product product, java.util.Map<String, java.math.BigDecimal> componentOptionStockMap) {
        List<ProductVariantDTO> variants = variantRepository.findByProductIdAndActiveTrue(product.getId())
            .stream().map(variant -> new ProductVariantDTO(variant.getId(), variant.getName(), variant.getPriceDelta(), variant.getActive(), null)).toList();
        List<ProductExtraDTO> extras = extraRepository.findByProductIdAndActiveTrue(product.getId())
            .stream().map(extra -> new ProductExtraDTO(extra.getId(), extra.getName(), extra.getPrice(), extra.getActive())).toList();
        List<ProductComponentGroupDTO> groups = componentGroupRepository.findByProductIdAndActiveTrue(product.getId())
            .stream().map(group -> {
                List<ProductComponentOptionDTO> opts = componentOptionRepository.findByGroupId(group.getId())
                    .stream().map(o -> {
                        java.math.BigDecimal qty = componentOptionStockMap != null
                            ? componentOptionStockMap.get(o.getId()) : null;
                        return new ProductComponentOptionDTO(o.getId(), o.getName(), qty);
                    }).toList();
                return new ProductComponentGroupDTO(group.getId(), group.getName(), group.getMaxSelections(),
                    group.isRequired(), group.getActive(), opts);
            }).toList();

        return new ProductResponseDTO(
            product.getId(), product.getName(), product.getCategory(), product.getUnit(),
            product.getCostPrice(), product.getSellPrice(), product.getActive(), product.getImageUrl(), product.getCreatedAt(),
            variants, extras, groups, null
        );
    }
}
