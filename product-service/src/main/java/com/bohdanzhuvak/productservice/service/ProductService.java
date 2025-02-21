package com.bohdanzhuvak.productservice.service;

import com.bohdanzhuvak.commonexceptions.exception.ProductNotFoundException;
import com.bohdanzhuvak.productservice.dto.ProductRequest;
import com.bohdanzhuvak.productservice.dto.ProductResponse;
import com.bohdanzhuvak.productservice.mapper.ProductMapper;
import com.bohdanzhuvak.productservice.model.Category;
import com.bohdanzhuvak.productservice.model.Product;
import com.bohdanzhuvak.productservice.repository.CategoryRepository;
import com.bohdanzhuvak.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final ProductMapper productMapper;

  public ProductResponse createProduct(ProductRequest productRequest) {
    Category category = categoryRepository.findById(productRequest.categoryId())
        .orElseThrow(() -> new ProductNotFoundException("Category " + productRequest.categoryId() + " not found"));
    Product product = productMapper.toProduct(productRequest, category);
    product = productRepository.save(product);
    log.info("Product {} is saved", product.getId());
    return productMapper.toProductResponse(product);
  }

  public ProductResponse getProductById(String productId) {
    ProductResponse productResponse = productRepository.findById(productId)
        .map(productMapper::toProductResponse)
        .orElseThrow(() -> new ProductNotFoundException("Product " + productId + " not found"));
    log.info("Product {} is found", productId);
    return productResponse;
  }

  public Page<ProductResponse> getProducts(Pageable pageable, Map<String, String> filterParams) {
    Page<ProductResponse> productResponses = productRepository.findProductsByFilters(filterParams, pageable)
        .map(productMapper::toProductResponse);
    log.info("List of {} products filtered by {} is found", productResponses.getTotalElements(), filterParams);
    return productResponses;
  }

  public ProductResponse updateProductById(String id, ProductRequest productRequest) {
    return productRepository.findById(id)
        .map(existingProduct -> productMapper.updateProductWithoutMutation(productRequest, existingProduct))
        .map(productRepository::save)
        .map(savedProduct -> {
          log.info("Product {} is updated", id);
          return savedProduct;
        })
        .map(productMapper::toProductResponse)
        .orElseThrow(() -> new ProductNotFoundException("Product " + id + " not found"));
  }

  public void deleteProductById(String productId) {
    if (!productRepository.existsById(productId)) {
      throw new ProductNotFoundException("Product " + productId + " not found");
    }
    productRepository.deleteById(productId);
    log.info("Product {} is deleted", productId);
  }
}
