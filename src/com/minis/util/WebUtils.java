package com.minis.util;

import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

public class WebUtils {
	public static Map<String, Object> getParametersStartingWith(HttpServletRequest request, String prefix) {
		Enumeration<String> paramNames = request.getParameterNames();
		Map<String, Object> params = new TreeMap<>();
		if (prefix == null) {
			prefix = "";
		}
		while (paramNames != null && paramNames.hasMoreElements()) {
			String paramName = paramNames.nextElement();
			if (prefix.isEmpty() || paramName.startsWith(prefix)) {
				String unprefixed = paramName.substring(prefix.length());
				String value = request.getParameter(paramName);

				params.put(unprefixed, value);
			}
		}
		return params;
	}
}
