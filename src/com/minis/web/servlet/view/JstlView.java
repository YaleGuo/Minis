package com.minis.web.servlet.view;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.minis.web.servlet.View;

public class JstlView implements View{
	public static final String DEFAULT_CONTENT_TYPE = "text/html;charset=ISO-8859-1";
	private String contentType = DEFAULT_CONTENT_TYPE;
	private String requestContextAttribute;
	private String beanName;
	private String url;

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getContentType() {
		return this.contentType;
	}
	public void setRequestContextAttribute(String requestContextAttribute) {
		this.requestContextAttribute = requestContextAttribute;
	}
	public String getRequestContextAttribute() {
		return this.requestContextAttribute;
	}
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getBeanName() {
		return this.beanName;
	}
	@Override
	public void setUrl(String url) {
		this.url = url;
	}
	@Override
	public String getUrl() {
		return this.url;
	}
	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		for (Entry<String, ?> e : model.entrySet()) {
			request.setAttribute(e.getKey(),e.getValue());
		}
		
		request.getRequestDispatcher(getUrl()).forward(request, response);
	}

}
