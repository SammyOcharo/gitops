package com.samdev.gitops.service;

import com.samdev.gitops.dto.ProductDto;
import com.samdev.gitops.exception.ResourceNotFoundException;
import com.samdev.gitops.model.Product;
import com.samdev.gitops.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    // ── Queries ───────────────────────────────────────────────────────────────

    public Page<ProductDto.Response> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<ProductDto.Response> search(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(this::toResponse);
    }

    public ProductDto.Response findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    // ── Commands ──────────────────────────────────────────────────────────────

    @Transactional
    public ProductDto.Response create(ProductDto.Request request) {
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductDto.Response update(Long id, ProductDto.Request request) {
        Product product = getOrThrow(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        productRepository.delete(getOrThrow(id));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Product getOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    private ProductDto.Response toResponse(Product p) {
        return ProductDto.Response.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stock(p.getStock())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}