package com.samdev.gitops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// ── Request DTO (used for POST / PUT) ─────────────────────────────────────────
public class ProductDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        @NotBlank(message = "Name is required")
        private String name;

        private String description;

        @Positive(message = "Price must be positive")
        private BigDecimal price;

        private Integer stock;
    }

    // ── Response DTO (returned to clients) ────────────────────────────────────
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stock;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}