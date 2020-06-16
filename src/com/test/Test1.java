package com.test;

import com.minis.web.servlet.ModelAndView;

public class Test1 {

	public static void main(String[] args) {
		//Object obj = new ModelAndView();
		Object obj = "error";
		
		if (obj instanceof ModelAndView) {
			System.out.println("Yes");
		}
		else {
			System.out.println("No");
			
		}

	}

}
