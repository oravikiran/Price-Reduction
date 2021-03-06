package com.java.app.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.java.app.entity.Product;
import com.java.app.entity.ProductResponse;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@PropertySource(value={"classpath:api-url.properties"})
public class PriceReductionRepositoryImpl implements PriceReductionRepository {

	@Value("${product-list-url}")
	private String productUrl;
	@Value("${product-list-url-key}")
	private String productUrlKey;

	@Autowired
	RestTemplate restTemplate;


	@Override
	public <T> ResponseEntity<T> hitApiUrl(String resourceEndpoint, Class<T> resourceClass) {
		
		try {
			log.info("Hitting the API url: "+resourceEndpoint);
			ResponseEntity<T> rateResponse =restTemplate.getForEntity(resourceEndpoint, resourceClass);
	
			return rateResponse;
		}catch (Exception e) {
			log.info("ERROR!!! int the API url: "+e.getMessage());
			return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
		}
	}

	@Override
	public List<Product> fetchAllProducts(){
		log.info("Inside PriceReductionRepositoryImpl Class....");
		log.info("Calling fetchAllProducts() method....");

		log.info("Fetching productUrl and productUrlKey from api-url.properties file....");
		String resourceEndpoint = productUrl+productUrlKey;

		ResponseEntity<ProductResponse> rateResponse = hitApiUrl(resourceEndpoint, ProductResponse.class);
		log.info("Got the response code: "+rateResponse.getStatusCode());

		log.info("Returning the Product response to the Service....");
		return rateResponse.getBody()!=null?
				rateResponse.getBody().getProducts().stream()
				.map(pdt->{
					if(pdt.getPrice().getNow() instanceof Map) {
						return null;
					}else {
						pdt.getPrice().setNow((String) pdt.getPrice().getNow());
					}
					return pdt;
				})
				.filter(pdt->!(pdt==null))
				.collect(Collectors.toList())
				:new ArrayList<>();
	}


}
