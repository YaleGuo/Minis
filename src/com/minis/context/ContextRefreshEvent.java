package com.minis.context;

public class ContextRefreshEvent extends ApplicationEvent{

	private static final long serialVersionUID = 1L;

	public ContextRefreshEvent(Object arg0) {
		super(arg0);
	}
	
	public String toString() {
		return this.msg;
	}

}
