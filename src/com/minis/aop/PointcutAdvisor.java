package com.minis.aop;

public interface PointcutAdvisor extends Advisor {
	Pointcut getPointcut();
}