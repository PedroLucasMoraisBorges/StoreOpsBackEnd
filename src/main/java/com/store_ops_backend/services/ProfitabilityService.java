package com.store_ops_backend.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.store_ops_backend.models.dtos.ProductProfitabilityDTO;
import com.store_ops_backend.models.entities.Product;
import com.store_ops_backend.models.entities.TableSessionItem;
import com.store_ops_backend.repositories.TableSessionItemRepository;

@Service
public class ProfitabilityService {

    @Autowired
    private TableSessionItemRepository tableSessionItemRepository;

    public List<ProductProfitabilityDTO> getProductProfitability(
            String companyId, LocalDate from, LocalDate to) {

        OffsetDateTime start = from.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime end = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime().minusNanos(1);

        List<TableSessionItem> items = tableSessionItemRepository
            .findSoldItemsWithProductByCompanyAndPeriod(companyId, start, end);

        // Group by product
        Map<String, List<TableSessionItem>> byProduct = new LinkedHashMap<>();
        for (TableSessionItem item : items) {
            String productId = item.getProduct().getId();
            byProduct.computeIfAbsent(productId, k -> new ArrayList<>()).add(item);
        }

        List<ProductProfitabilityDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<TableSessionItem>> entry : byProduct.entrySet()) {
            List<TableSessionItem> productItems = entry.getValue();
            Product product = productItems.get(0).getProduct();

            BigDecimal totalQty = productItems.stream()
                .map(TableSessionItem::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalRevenue = productItems.stream()
                .map(i -> i.getQuantity().multiply(i.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal costPrice = product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO;
            BigDecimal totalCost = totalQty.multiply(costPrice);
            BigDecimal grossMargin = totalRevenue.subtract(totalCost);

            BigDecimal marginPercent = BigDecimal.ZERO;
            if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                marginPercent = grossMargin
                    .divide(totalRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            }

            result.add(new ProductProfitabilityDTO(
                product.getId(),
                product.getName(),
                product.getCategory(),
                totalQty,
                totalRevenue.setScale(2, RoundingMode.HALF_UP),
                totalCost.setScale(2, RoundingMode.HALF_UP),
                grossMargin.setScale(2, RoundingMode.HALF_UP),
                marginPercent
            ));
        }

        result.sort(Comparator.comparing(ProductProfitabilityDTO::grossMargin).reversed());
        return result;
    }
}
