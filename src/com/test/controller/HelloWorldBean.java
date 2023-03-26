package com.test.controller;

import com.minis.beans.factory.annotation.Autowired;
import com.minis.web.RequestMapping;
import com.test.entity.User;
import com.test.service.BaseService;

public class HelloWorldBean {
	@Autowired
	BaseService baseservice;
	
	@RequestMapping("/test1")
	public String doTest1() {
		return "test 1, hello world!";
	}
	@RequestMapping("/test2")
	public String doTest2() {
		return "test 2, hello world!";
	}
	@RequestMapping("/test3")
	public String doTest3() {
		return baseservice.getHello();
	}
	@RequestMapping("/test4")
	public String doTest4(User user) {
		return user.getId() +" "+user.getName() + " " + user.getBirthday();
	}
	
}
