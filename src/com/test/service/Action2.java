package com.test.service;

public class Action2 implements IAction {

	@Override
	public void doAction() {
		System.out.println("really do action2");
		
	}

	@Override
	public void doSomething() {
		System.out.println("really do something action2");
	}

}
