package com.minis.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface HandlerAdapter {
	void handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;
}
