package com.minis.util.concurrent;

public interface FailureCallback {
	void onFailure(Throwable ex);
}