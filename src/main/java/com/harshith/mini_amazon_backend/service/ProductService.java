package com.harshith.mini_amazon_backend.service;

import com.harshith.mini_amazon_backend.dto.ProductRequestDto;
import com.harshith.mini_amazon_backend.dto.ProductResponseDto;
import com.harshith.mini_amazon_backend.entity.Product;
import com.harshith.mini_amazon_backend.exception.CategoryNotFoundException;
import com.harshith.mini_amazon_backend.exception.ProductNotFoundException;
import com.harshith.mini_amazon_backend.repository.ProductRepository;
import com.harshith.mini_amazon_backend.repository.ReviewRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "name", "cost", "rating", "stock", "discountPercent"
    );

    private final ProductRepository productRepository;

    // NEW (Day 14): needed to compute averageRating/reviewCount on every
    // product response - see buildDto below.
    private final ReviewRepository reviewRepository;

    // BUG FIX (Day 14 review): this constructor previously also had an
    // unused `import com.harshith.mini_amazon_backend.dto.CartResponseDto;`
    // at the top of the file - a leftover import with nothing in this class
    // ever referencing it. Removed as dead code/noise, same reasoning as
    // WishlistController's unused ProductService dependency fix on Day 7.
    public ProductService(ProductRepository productRepository, ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<ProductResponseDto> getAllProducts(String sort) {
        List<Product> products = (sort == null || sort.isBlank())
                ? productRepository.findAll()
                : productRepository.findAll(parseSort(sort));

        return toDtoList(products);
    }

    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return toDto(product);
    }

    // NEW (Day 14): one aggregate query for ALL products' rating summaries,
    // instead of calling reviewRepository per product inside the mapping
    // loop - looping a query per product is a classic N+1 (1 query to load
    // the products + N more queries just for their ratings). A product with
    // no reviews yet simply has no entry in the map, and getOrDefault below
    // treats that the same as an explicit "0 reviews, 0.0 average".
    private List<ProductResponseDto> toDtoList(List<Product> products) {
        Map<Long, ReviewRepository.ProductRatingSummary> ratingSummaries =
                reviewRepository.findRatingSummaryForAllProducts()
                        .stream()
                        .collect(Collectors.toMap(
                                ReviewRepository.ProductRatingSummary::getProductId,
                                summary -> summary));

        return products.stream()
                .map(product -> {
                    ReviewRepository.ProductRatingSummary summary = ratingSummaries.get(product.getId());
                    double averageRating = (summary == null || summary.getAverageRating() == null)
                            ? 0.0 : summary.getAverageRating();
                    long reviewCount = (summary == null || summary.getReviewCount() == null)
                            ? 0L : summary.getReviewCount();
                    return buildDto(product, averageRating, reviewCount);
                })
                .toList();
    }

    // Single-product path (getProductById, addProduct, updateProduct): one
    // extra query pair per call is fine here since it's only ever one
    // product, not a whole list - see ReviewRepository.findAverageRatingByProductId
    // for why this uses a different query shape than the batch version above.
    private ProductResponseDto toDto(Product product) {
        Double averageRating = reviewRepository.findAverageRatingByProductId(product.getId());
        long reviewCount = reviewRepository.countByProductId(product.getId());
        return buildDto(product, averageRating == null ? 0.0 : averageRating, reviewCount);
    }

    private ProductResponseDto buildDto(Product product, double averageRating, long reviewCount) {
        return new ProductResponseDto(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getCategory(),
                product.getDescription(),
                product.getCost(),
                product.getRating(),
                product.getStock(),
                product.getImageUrl(),
                product.isNewArrival(),
                product.isOnSale(),
                product.getDiscountPercent(),
                roundToOneDecimal(averageRating),
                reviewCount
        );
    }

    // Ratings like 4.333333 read poorly in a UI; rounding to one decimal
    // (4.3) is the same idea as currency being stored/displayed to 2
    // decimal places elsewhere in this project (see Product.cost's
    // precision = 10, scale = 2).
    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public ProductResponseDto addProduct(ProductRequestDto request) {
        Product product = toEntity(request);
        Product saved = productRepository.save(product);
        return toDto(saved);
    }

    public ProductResponseDto updateProduct(Long id, ProductRequestDto request) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        applyUpdates(existing, request);
        Product saved = productRepository.save(existing);
        return toDto(saved);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    private Product toEntity(ProductRequestDto request) {
        Product product = new Product();
        applyUpdates(product, request);
        return product;
    }

    private void applyUpdates(Product product, ProductRequestDto request) {
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setCategory(request.getCategory());
        product.setDescription(request.getDescription());
        product.setCost(request.getCost());
        product.setRating(request.getRating());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setNewArrival(request.isNewArrival());
        product.setOnSale(request.isOnSale());
        product.setDiscountPercent(request.getDiscountPercent());
    }

    public List<ProductResponseDto> searchProducts(String keyword) {
        List<Product> products = productRepository
                .findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                        keyword,
                        keyword,
                        keyword
                );
        return toDtoList(products);
    }

    public List<ProductResponseDto> categoryFilter(String category) {
        List<Product> products = productRepository.findByCategoryIgnoreCase(category);

        if (products.isEmpty()) {
            throw new CategoryNotFoundException(category);
        }

        return toDtoList(products);
    }

    private Sort parseSort(String sort) {
        String[] parts = sort.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid sort format '" + sort + "'. Expected 'field,direction', e.g. cost,asc");
        }

        String field = parts[0].trim();
        String direction = parts[1].trim();

        if (!SORTABLE_FIELDS.contains(field)) {
            throw new IllegalArgumentException(
                    "Cannot sort by '" + field + "'. Allowed fields: " + SORTABLE_FIELDS);
        }

        Sort.Direction sortDirection;
        try {
            sortDirection = Sort.Direction.fromString(direction);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid sort direction '" + direction + "'. Use 'asc' or 'desc'.");
        }

        return Sort.by(sortDirection, field);
    }

}