package pl.mbobowski.sales.product;

import java.util.Optional;

public interface ProductDetailsProvider {
    Optional<ProductDetails> getById(String productId);
}
