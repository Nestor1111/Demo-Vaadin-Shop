package com.nridev.vaadinshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nridev.vaadinshop.entities.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {


	Product findByName(String name);
	
}
