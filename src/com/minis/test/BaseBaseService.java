package com.minis.test;

public class BaseBaseService {
	private AServiceImpl as;
	
	public AServiceImpl getAs() {
		return as;
	}
	public void setAs(AServiceImpl as) {
		this.as = as;
	}
	public BaseBaseService() {
	}
	public void sayHello() {
		System.out.println("Base Base Service says hello");

	}
	public void init() {
		System.out.println("Base Base Service init method.");

	}
}
