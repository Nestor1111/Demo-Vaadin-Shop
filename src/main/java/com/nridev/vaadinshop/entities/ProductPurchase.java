package com.nridev.vaadinshop.entities;

public class ProductPurchase {

	private Product product;

	private Integer quantity;

	public ProductPurchase() {
	}

	public ProductPurchase(Product product, Integer quantity) {
		super();
		this.product = product;
		this.quantity = quantity;
	}

	public Product getProduct() {
		return product;
	}

	public Integer getQuantity() {
		return quantity;
	}

}
