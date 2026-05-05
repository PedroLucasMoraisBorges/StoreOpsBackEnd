package com.store_ops_backend.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.store_ops_backend.models.entities.StockItem;

public interface StockItemRepository extends JpaRepository<StockItem, String> {

    @Query("SELECT s FROM stock_items s WHERE s.company.id = :companyId ORDER BY s.product.name ASC")
    List<StockItem> findByCompanyIdOrderByProductNameAsc(@Param("companyId") String companyId);

    @Query("SELECT s FROM stock_items s WHERE s.company.id = :companyId AND s.quantity < s.minQuantity")
    List<StockItem> findBelowMinimum(@Param("companyId") String companyId);

    Optional<StockItem> findByIdAndCompanyId(String id, String companyId);

    @Query("SELECT s FROM stock_items s WHERE s.product.id = :productId AND s.company.id = :companyId AND s.variant IS NULL AND s.componentOption IS NULL")
    Optional<StockItem> findProductLevelStock(@Param("productId") String productId, @Param("companyId") String companyId);

    @Query("SELECT s FROM stock_items s WHERE s.product.id = :productId AND s.variant.id = :variantId AND s.company.id = :companyId AND s.componentOption IS NULL")
    Optional<StockItem> findVariantLevelStock(@Param("productId") String productId, @Param("variantId") String variantId, @Param("companyId") String companyId);

    @Query("SELECT s FROM stock_items s WHERE s.componentOption.id = :optionId AND s.company.id = :companyId")
    Optional<StockItem> findComponentLevelStock(@Param("optionId") String optionId, @Param("companyId") String companyId);

    @Query("SELECT s FROM stock_items s WHERE s.product.id = :productId AND s.company.id = :companyId")
    List<StockItem> findAllByProductIdAndCompanyId(@Param("productId") String productId, @Param("companyId") String companyId);

    @Query("SELECT s FROM stock_items s WHERE s.componentOption.id IN :optionIds AND s.company.id = :companyId")
    List<StockItem> findByComponentOptionIds(@Param("optionIds") List<String> optionIds, @Param("companyId") String companyId);
}
