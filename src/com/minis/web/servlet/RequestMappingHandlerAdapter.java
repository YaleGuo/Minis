package com.minis.web.servlet;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.minis.beans.BeansException;
import com.minis.web.WebApplicationContext;
import com.minis.web.WebBindingInitializer;
import com.minis.web.WebDataBinder;
import com.minis.web.WebDataBinderFactory;

public class RequestMappingHandlerAdapter implements HandlerAdapter {
	WebApplicationContext wac = null;
	private WebBindingInitializer webBindingInitializer = null;

	public RequestMappingHandlerAdapter(WebApplicationContext wac) {
		this.wac = wac;
		try {
			this.webBindingInitializer = (WebBindingInitializer) this.wac.getBean("webBindingInitializer");
		} catch (BeansException e) {
			e.printStackTrace();
		}
		
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
			 invokeHandlerMethod(request, response, handler);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return mv;

	}
	
	protected void invokeHandlerMethod(HttpServletRequest request,
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
			Object returnobj = invocableMethod.invoke(handlerMethod.getBean(), methodParamObjs);
			
			response.getWriter().append(returnobj.toString());
			//ModelFactory modelFactory = getModelFactory(handlerMethod, binderFactory);

//			ServletInvocableHandlerMethod invocableMethod = handlerMethod.getMethod();
//			if (this.argumentResolvers != null) {
//				invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
//			}
//			if (this.returnValueHandlers != null) {
//				invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
//			}
//			invocableMethod.setDataBinderFactory(binderFactory);
//			invocableMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
//
//			ModelAndViewContainer mavContainer = new ModelAndViewContainer();
//			mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
//			modelFactory.initModel(webRequest, mavContainer, invocableMethod);
//			mavContainer.setIgnoreDefaultModelOnRedirect(this.ignoreDefaultModelOnRedirect);


//			invocableMethod.invokeAndHandle(webRequest, mavContainer);

//			return getModelAndView(mavContainer, modelFactory, webRequest);


	}

	public WebBindingInitializer getWebBindingInitializer() {
		return webBindingInitializer;
	}

	public void setWebBindingInitializer(WebBindingInitializer webBindingInitializer) {
		this.webBindingInitializer = webBindingInitializer;
	}


}
