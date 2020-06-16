package com.minis.web.servlet;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.minis.beans.BeansException;
import com.minis.web.HttpMessageConverter;
import com.minis.web.ResponseBody;
import com.minis.web.WebApplicationContext;
import com.minis.web.WebBindingInitializer;
import com.minis.web.WebDataBinder;
import com.minis.web.WebDataBinderFactory;

public class RequestMappingHandlerAdapter implements HandlerAdapter {
	private WebBindingInitializer webBindingInitializer = null;
	private HttpMessageConverter messageConverter = null;

	public HttpMessageConverter getMessageConverter() {
		return messageConverter;
	}

	public void setMessageConverter(HttpMessageConverter messageConverter) {
		this.messageConverter = messageConverter;
	}

	public RequestMappingHandlerAdapter() {
	}

	@Override
	public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		return handleInternal(request, response, (HandlerMethod) handler);
	}

	private ModelAndView handleInternal(HttpServletRequest request, HttpServletResponse response,
			HandlerMethod handler) {
		ModelAndView mv = null;
		
		try {
			 mv = invokeHandlerMethod(request, response, handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return mv;

	}
	
	protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
			HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

			WebDataBinderFactory binderFactory = new WebDataBinderFactory();
			
			Parameter[] methodParameters = handlerMethod.getMethod().getParameters();
			Object[] methodParamObjs = new Object[methodParameters.length];
			
			int i = 0;
			for (Parameter methodParameter : methodParameters) {
				Object methodParamObj = methodParameter.getType().newInstance();
				WebDataBinder wdb = binderFactory.createBinder(request, methodParamObj, methodParameter.getName());
				webBindingInitializer.initBinder(wdb);
				wdb.bind(request);
				methodParamObjs[i] = methodParamObj;
				i++;
			}
			
			Method invocableMethod = handlerMethod.getMethod();
			Object returnObj = invocableMethod.invoke(handlerMethod.getBean(), methodParamObjs);
			Class<?> returnType = invocableMethod.getReturnType();
					
			ModelAndView mav = null;
			if (invocableMethod.isAnnotationPresent(ResponseBody.class)){ //ResponseBody
		        this.messageConverter.write(returnObj, response);
			}
			else {
				if (returnObj instanceof ModelAndView) {
					mav = (ModelAndView)returnObj;
				}
				else if(returnObj instanceof String) {
					String sTarget = (String)returnObj;
					mav = new ModelAndView();
					mav.setViewName(sTarget);
				}
			}
			
			return mav;
	}


	public WebBindingInitializer getWebBindingInitializer() {
		return webBindingInitializer;
	}

	public void setWebBindingInitializer(WebBindingInitializer webBindingInitializer) {
		this.webBindingInitializer = webBindingInitializer;
	}


}
