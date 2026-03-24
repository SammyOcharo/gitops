package com.samdev.gitops.service;

import static org.junit.jupiter.api.Assertions.*;

import com.samdev.gitops.dto.ProductDto;
import com.samdev.gitops.exception.ResourceNotFoundException;
import com.samdev.gitops.model.Product;
import com.samdev.gitops.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Widget A")
                .price(BigDecimal.valueOf(9.99))
                .stock(100)
                .build();
    }

    @Test
    void findById_whenExists_returnsResponse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        ProductDto.Response response = productService.findById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Widget A");
    }

    @Test
    void findById_whenMissing_throwsNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_savesAndReturnsProduct() {
        ProductDto.Request request = ProductDto.Request.builder()
                .name("New Item")
                .price(BigDecimal.valueOf(19.99))
                .stock(50)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductDto.Response response = productService.create(request);

        verify(productRepository, times(1)).save(any(Product.class));
        assertThat(response).isNotNull();
    }

    @Test
    void delete_whenExists_callsRepository() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        productService.delete(1L);

        verify(productRepository).delete(sampleProduct);
    }
}