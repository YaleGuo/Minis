package com.minis.context;

public interface ApplicationEventPublisher {
	void publishEvent(ApplicationEvent event);
	void addApplicationListener(ApplicationListener listener);
}
