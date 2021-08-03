package com.shibu.microservices.core.product.services;

import com.shibu.supermarket.api.core.product.Product;
import com.shibu.supermarket.api.core.product.ProductService;
import com.shibu.supermarket.util.http.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {

    private ServiceUtil serviceUtil;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Product getProduct(int productId) {
        return new Product(productId, "name-" + productId, 123,serviceUtil.getServiceAddress());
    }
}
