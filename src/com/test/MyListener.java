package com.test;

import com.minis.context.ApplicationListener;
import com.minis.context.ContextRefreshedEvent;

public class MyListener implements ApplicationListener<ContextRefreshedEvent> {
	   @Override
	   public void onApplicationEvent(ContextRefreshedEvent event) {
	      System.out.println(".........refreshed.........beans count : " + event.getApplicationContext().getBeanDefinitionCount());
	   }

}

