package com.minis.aop;

public class NameMatchMethodPointcutAdvisor implements PointcutAdvisor{
	private Advice advice = null;
	private MethodInterceptor methodInterceptor;
	private String mappedName;
	private final NameMatchMethodPointcut pointcut = new NameMatchMethodPointcut();

	public NameMatchMethodPointcutAdvisor() {
	}

	public NameMatchMethodPointcutAdvisor(Advice advice) {
		this.advice = advice;
	}
	
	public void setMethodInterceptor(MethodInterceptor methodInterceptor) {
		this.methodInterceptor = methodInterceptor;
	}

	public MethodInterceptor getMethodInterceptor() {
		return this.methodInterceptor;
	}


	public void setAdvice(Advice advice) {
		this.advice = advice;

		MethodInterceptor mi = null;

		if (advice instanceof BeforeAdvice) {
			mi = new MethodBeforeAdviceInterceptor((MethodBeforeAdvice)advice);
		}
		else if (advice instanceof AfterAdvice){
			mi = new AfterReturningAdviceInterceptor((AfterReturningAdvice)advice);
		}
		else if (advice instanceof MethodInterceptor) {
			mi = (MethodInterceptor)advice;
		}
		
		setMethodInterceptor(mi);
	}

	@Override
	public Advice getAdvice() {
		return this.advice;
	}

	@Override
	public Pointcut getPointcut() {
		return pointcut;
	}
	
	public void setMappedName(String mappedName) {
		this.mappedName = mappedName;
		this.pointcut.setMappedName(this.mappedName);
	}

}
