package com.minis.web.servlet;

import javax.servlet.http.HttpServletRequest;

import com.minis.web.method.HandlerMethod;

public interface HandlerMapping {
	HandlerMethod getHandler(HttpServletRequest request) throws Exception;
}
