package com.minis.test;

import com.minis.ClassPathXmlApplicationContext;

public class Test1 {

	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");
	    AService aService=(AService)ctx.getBean("aservice");
	    aService.sayHello();
	}

}
