package com.test.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.minis.beans.factory.annotation.Autowired;
import com.minis.web.bind.annotation.RequestMapping;
import com.minis.web.bind.annotation.ResponseBody;
import com.minis.web.servlet.ModelAndView;
import com.test.entity.User;
import com.test.service.AService;
import com.test.service.BaseService;
import com.test.service.IAction;
import com.test.service.UserService;

public class HelloWorldBean {
	@Autowired
	BaseService baseservice;
	
	@Autowired
	UserService userService;
	
	
	@RequestMapping("/test2")
	public void doTest2(HttpServletRequest request, HttpServletResponse response) {
		String str = "test 2, hello world!";
		try {
			response.getWriter().write(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@RequestMapping("/test5")
	public ModelAndView doTest5(User user) {
		ModelAndView mav = new ModelAndView("test","msg",user.getName());
		return mav;
	}
	@RequestMapping("/test6")
	public String doTest6(User user) {
		return "error";
	}	
	
	@RequestMapping("/test7")
	@ResponseBody
	public User doTest7(User user) {
		user.setName(user.getName() + "---");
		user.setBirthday(new Date());
		return user;
	}	
	
	@RequestMapping("/test8")
	@ResponseBody
	public User doTest8(HttpServletRequest request, HttpServletResponse response) {
		int userid = Integer.parseInt(request.getParameter("id"));
		User user = userService.getUserInfo(userid);		
		return user;
	}	
	
	@RequestMapping("/test9")
	@ResponseBody
	public List<User> doTest9(HttpServletRequest request, HttpServletResponse response) {
		int userid = Integer.parseInt(request.getParameter("id"));
		List<User> users = userService.getUsers(userid);		
		return users;
	}
	
	@Autowired
	IAction action;
	
	@RequestMapping("/testaop")
	public void doTestAop(HttpServletRequest request, HttpServletResponse response) {
		action.doAction();
		
		String str = "test aop, hello world!";
		try {
			response.getWriter().write(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@RequestMapping("/testaop2")
	public void doTestAop2(HttpServletRequest request, HttpServletResponse response) {
		action.doSomething();
		
		String str = "test aop, hello world!";
		try {
			response.getWriter().write(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
//	@Autowired
//	IAction action2;
//	
//	@RequestMapping("/testaop2")
//	public void doTestAop2(HttpServletRequest request, HttpServletResponse response) {
//		action2.doAction();
//		
//		String str = "test aop 2, hello world!";
//		try {
//			response.getWriter().write(str);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}
