package com.test;

import java.util.Date;

import com.minis.web.bind.WebDataBinder;
import com.minis.web.bind.support.WebBindingInitializer;

public class DateInitializer implements WebBindingInitializer{
	@Override
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(Date.class, new CustomDateEditor(Date.class,"yyyy-MM-dd", false));
	}
}
