package com.minis.web.servlet;

public interface ViewResolver {
	View resolveViewName(String viewName) throws Exception;

}
