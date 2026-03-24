package com.samdev.gitops.controller;

import static org.junit.jupiter.api.Assertions.*;
import com.samdev.gitops.dto.ProductDto;
import com.samdev.gitops.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockitoBean
    ProductService productService;

    private final ProductDto.Response sampleResponse = ProductDto.Response.builder()
            .id(1L).name("Widget A").price(BigDecimal.valueOf(9.99)).stock(100).build();

    @Test
    void listProducts_returns200() throws Exception {
        when(productService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse)));

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Widget A"));
    }

    @Test
    void createProduct_withValidBody_returns201() throws Exception {
        ProductDto.Request request = ProductDto.Request.builder()
                .name("New Item").price(BigDecimal.valueOf(5.00)).stock(10).build();

        when(productService.create(any())).thenReturn(sampleResponse);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createProduct_withMissingName_returns400() throws Exception {
        ProductDto.Request invalid = ProductDto.Request.builder()
                .price(BigDecimal.valueOf(5.00)).stock(10).build(); // no name

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }
}