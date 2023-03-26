package com.minis.http.converter;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

public interface HttpMessageConverter {
	void write(Object obj, HttpServletResponse response) throws IOException;
}
