package com.samdev.gitops.controller;

import com.samdev.gitops.dto.ProductDto;
import com.samdev.gitops.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // GET /api/v1/products?page=0&size=20&sort=name,asc
    @GetMapping
    public ResponseEntity<Page<ProductDto.Response>> list(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ProductDto.Response> page = (search != null && !search.isBlank())
                ? productService.search(search, pageable)
                : productService.findAll(pageable);

        return ResponseEntity.ok(page);
    }

    // GET /api/v1/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    // POST /api/v1/products
    @PostMapping
    public ResponseEntity<ProductDto.Response> create(
            @Valid @RequestBody ProductDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.create(request));
    }

    // PUT /api/v1/products/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductDto.Request request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    // DELETE /api/v1/products/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}