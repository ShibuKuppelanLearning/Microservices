package com.shibu.microservices.supermarket.composite.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shibu.supermarket.api.core.product.Product;
import com.shibu.supermarket.api.core.product.ProductService;
import com.shibu.supermarket.api.core.recommendation.Recommendation;
import com.shibu.supermarket.api.core.recommendation.RecommendationService;
import com.shibu.supermarket.api.core.review.Review;
import com.shibu.supermarket.api.core.review.ReviewService;
import com.shibu.supermarket.util.exceptions.InvalidInputException;
import com.shibu.supermarket.util.exceptions.NotFoundException;
import com.shibu.supermarket.util.http.HttpErrorInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class SupermarketCompositeIntegrationService implements ProductService, ReviewService, RecommendationService {

    private static final Logger LOG = LoggerFactory.getLogger(SupermarketCompositeIntegrationService.class);

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    private String productServiceUrl;
    private String reviewServiceUrl;
    private String recommendationServiceUrl;

    @Autowired
    public SupermarketCompositeIntegrationService(RestTemplate restTemplate,
                                                  ObjectMapper objectMapper,
                                                  @Value("${app.product-service.host}") String productServiceHost,
                                                  @Value("${app.product-service.port}") String productServicePort,
                                                  @Value("${app.product-service.context}") String productServiceContext,
                                                  @Value("${app.review-service.host}") String reviewServiceHost,
                                                  @Value("${app.review-service.port}") String reviewServicePort,
                                                  @Value("${app.review-service.context}") String reviewServiceContext,
                                                  @Value("${app.recommendation-service.host}") String recommendationServiceHost,
                                                  @Value("${app.recommendation-service.port}") String recommendationServicePort,
                                                  @Value("${app.recommendation-service.context}") String recommendationServiceContext) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.productServiceUrl = StringUtils.join("http://", productServiceHost, ":", productServicePort, productServiceContext, "/product/");
        this.reviewServiceUrl =  StringUtils.join("http://", reviewServiceHost, ":", reviewServicePort, reviewServiceContext, "/review/");
        this.recommendationServiceUrl = StringUtils.join("http://", recommendationServiceHost, ":", recommendationServicePort, recommendationServiceContext, "/recommendation/");
    }

    @Override
    public Product getProduct(int productId) {
        try {
            String url = productServiceUrl + productId;
            LOG.debug("Will call getProduct API on URL: {}", url);

            Product product = restTemplate.getForObject(url, Product.class);
            LOG.debug("Found a product with id: {}", product.getProductId());

            return product;

        } catch (HttpClientErrorException ex) {

            switch (ex.getStatusCode()) {
                case NOT_FOUND:
                    throw new NotFoundException(getErrorMessage(ex));
                case UNPROCESSABLE_ENTITY:
                    throw new InvalidInputException(getErrorMessage(ex));
                default:
                    LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                    LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                    throw ex;
            }
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        try {
            String url = reviewServiceUrl + productId;

            LOG.debug("Will call getReviews API on URL: {}", url);
            List<Review> reviews = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Review>>() {}).getBody();

            LOG.debug("Found {} reviews for a product with id: {}", reviews.size(), productId);
            return reviews;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Recommendation> getRecommendation(int productId) {
        try {
            String url = recommendationServiceUrl + productId;

            LOG.debug("Will call getRecommendations API on URL: {}", url);
            List<Recommendation> recommendations = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Recommendation>>() {}).getBody();

            LOG.debug("Found {} recommendations for a product with id: {}", recommendations.size(), productId);
            return recommendations;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }
}
