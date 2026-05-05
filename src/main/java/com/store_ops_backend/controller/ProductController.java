package com.store_ops_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.store_ops_backend.models.dtos.AddComponentGroupDTO;
import com.store_ops_backend.models.dtos.AddComponentOptionDTO;
import com.store_ops_backend.models.dtos.AddExtraDTO;
import com.store_ops_backend.models.dtos.AddVariantDTO;
import com.store_ops_backend.models.dtos.CreateProductDTO;
import com.store_ops_backend.models.dtos.ProductComponentGroupDTO;
import com.store_ops_backend.models.dtos.ProductComponentOptionDTO;
import com.store_ops_backend.models.dtos.ProductExtraDTO;
import com.store_ops_backend.models.dtos.ProductResponseDTO;
import com.store_ops_backend.models.dtos.ProductVariantDTO;
import com.store_ops_backend.models.dtos.UpdateProductDTO;
import com.store_ops_backend.services.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/create/{companyId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDTO create(
        @PathVariable("companyId") String companyId,
        @RequestBody @Valid CreateProductDTO data
    ) {
        return productService.create(companyId, data);
    }

    @GetMapping("/getAll/{companyId}")
    public List<ProductResponseDTO> getAll(
        @PathVariable("companyId") String companyId,
        @RequestParam(value = "activeOnly", defaultValue = "false") boolean activeOnly
    ) {
        return activeOnly
            ? productService.listActive(companyId)
            : productService.listAll(companyId);
    }

    @GetMapping("/get/{companyId}/{productId}")
    public ProductResponseDTO getById(
        @PathVariable("companyId") String companyId,
        @PathVariable("productId") String productId
    ) {
        return productService.getById(companyId, productId);
    }

    @PutMapping("/update/{companyId}/{productId}")
    public ProductResponseDTO update(
        @PathVariable("companyId") String companyId,
        @PathVariable("productId") String productId,
        @RequestBody @Valid UpdateProductDTO data
    ) {
        return productService.update(companyId, productId, data);
    }

    @PostMapping("/image/{companyId}/{productId}")
    public ProductResponseDTO uploadImage(
        @PathVariable("companyId") String companyId,
        @PathVariable("productId") String productId,
        @RequestParam("file") MultipartFile file
    ) {
        return productService.uploadImage(companyId, productId, file);
    }

    @DeleteMapping("/delete/{companyId}/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
        @PathVariable("companyId") String companyId,
        @PathVariable("productId") String productId
    ) {
        productService.delete(companyId, productId);
    }

    // ── Variants ──────────────────────────────────────────────────────────────

    @PostMapping("/variants/{companyId}/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductVariantDTO addVariant(
        @PathVariable String companyId,
        @PathVariable String productId,
        @RequestBody @Valid AddVariantDTO data
    ) {
        return productService.addVariant(companyId, productId, data.name(),
            data.priceDelta() != null ? data.priceDelta() : java.math.BigDecimal.ZERO);
    }

    @DeleteMapping("/variants/{companyId}/{productId}/{variantId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVariant(
        @PathVariable String companyId,
        @PathVariable String productId,
        @PathVariable String variantId
    ) {
        productService.deleteVariant(companyId, productId, variantId);
    }

    // ── Extras ────────────────────────────────────────────────────────────────

    @PostMapping("/extras/{companyId}/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductExtraDTO addExtra(
        @PathVariable String companyId,
        @PathVariable String productId,
        @RequestBody @Valid AddExtraDTO data
    ) {
        return productService.addExtra(companyId, productId, data.name(),
            data.price() != null ? data.price() : java.math.BigDecimal.ZERO);
    }

    @DeleteMapping("/extras/{companyId}/{productId}/{extraId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExtra(
        @PathVariable String companyId,
        @PathVariable String productId,
        @PathVariable String extraId
    ) {
        productService.deleteExtra(companyId, productId, extraId);
    }

    // ── Component Groups ──────────────────────────────────────────────────────

    @PostMapping("/components/{companyId}/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductComponentGroupDTO addComponentGroup(
        @PathVariable String companyId,
        @PathVariable String productId,
        @RequestBody @Valid AddComponentGroupDTO data
    ) {
        return productService.addComponentGroup(companyId, productId, data.name(),
            data.maxSelections() > 0 ? data.maxSelections() : 1, data.required());
    }

    @DeleteMapping("/components/{companyId}/{productId}/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComponentGroup(
        @PathVariable String companyId,
        @PathVariable String productId,
        @PathVariable String groupId
    ) {
        productService.deleteComponentGroup(companyId, productId, groupId);
    }

    // ── Component Options ─────────────────────────────────────────────────────

    @PostMapping("/components/{companyId}/{productId}/{groupId}/options")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductComponentOptionDTO addComponentOption(
        @PathVariable String companyId,
        @PathVariable String productId,
        @PathVariable String groupId,
        @RequestBody @Valid AddComponentOptionDTO data
    ) {
        return productService.addComponentOption(companyId, productId, groupId, data.name());
    }

    @DeleteMapping("/components/{companyId}/{productId}/{groupId}/options/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComponentOption(
        @PathVariable String companyId,
        @PathVariable String productId,
        @PathVariable String groupId,
        @PathVariable String optionId
    ) {
        productService.deleteComponentOption(companyId, productId, groupId, optionId);
    }
}
