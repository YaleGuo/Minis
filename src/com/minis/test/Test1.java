package com.minis.test;

import com.minis.beans.BeansException;
import com.minis.beans.factory.NoSuchBeanDefinitionException;
import com.minis.context.ClassPathXmlApplicationContext;

public class Test1 {

	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("beans.xml");
	    AService aService;
	    BaseService bService;
		try {
			//aService = (AService)ctx.getBean("aservice");
		    //aService.sayHello();
		    
		    bService = (BaseService)ctx.getBean("baseservice");
		    bService.sayHello();
		} catch (BeansException e) {
			e.printStackTrace();
		}
	}

}
