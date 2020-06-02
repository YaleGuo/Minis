package com.minis.web;

public class MappingValue {
	String uri;
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	String clz;
	public String getClz() {
		return clz;
	}
	public void setClz(String clz) {
		this.clz = clz;
	}
	String method;
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	
	public MappingValue(String uri, String clz, String method) {
		this.uri = uri;
		this.clz = clz;
		this.method = method;
	}
}
