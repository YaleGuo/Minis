package com.minis.test;

public class AServiceImpl implements AService {
	private String name;
	private int level;
	private String property1;
	private String property2;

	public String getProperty1() {
		return property1;
	}

	public void setProperty1(String property1) {
		this.property1 = property1;
	}
	
	public String getProperty2() {
		return property2;
	}

	public void setProperty2(String property2) {
		this.property2 = property2;
	}

	public AServiceImpl() {
	}
	
	public AServiceImpl(String name, int level) {
		this.name = name;
		this.level = level;		
		System.out.println(this.name + "," + this.level);
	}

	@Override
	public void sayHello() {
		System.out.println(this.property1 + "," + this.property2);
	}
}
