package com.minis.util.concurrent;

import java.util.LinkedList;
import java.util.Queue;

public class ListenableFutureCallbackRegistry<T> {
	private enum State {NEW, SUCCESS, FAILURE}

	private final Queue<SuccessCallback<? super T>> successCallbacks = new LinkedList<>();
	private final Queue<FailureCallback> failureCallbacks = new LinkedList<>();

	private State state = State.NEW;

	private Object result;
	private final Object mutex = new Object();

	private void notifySuccess(SuccessCallback<? super T> callback) {
		try {
			callback.onSuccess((T) this.result);
		}
		catch (Throwable ex) {
		}
	}

	private void notifyFailure(FailureCallback callback) {
		try {
			callback.onFailure((Throwable) this.result);
		}
		catch (Throwable ex) {
		}
	}

	public void addSuccessCallback(SuccessCallback<? super T> callback) {
		synchronized (this.mutex) {
			switch (this.state) {
				case NEW:
					this.successCallbacks.add(callback);
					break;
//				case SUCCESS:
//					notifySuccess(callback);
//					break;
			}
		}
	}

	public void addFailureCallback(FailureCallback callback) {
		synchronized (this.mutex) {
			switch (this.state) {
				case NEW:
					this.failureCallbacks.add(callback);
					break;
//				case FAILURE:
//					notifyFailure(callback);
//					break;
			}
		}
	}

	public void success(T result) {
		synchronized (this.mutex) {
			this.state = State.SUCCESS;
			this.result = result;
			SuccessCallback<? super T> callback;
			while ((callback = this.successCallbacks.poll()) != null) {
				notifySuccess(callback);
			}
		}
	}

	public void failure(Throwable ex) {
		synchronized (this.mutex) {
			this.state = State.FAILURE;
			this.result = ex;
			FailureCallback callback;
			while ((callback = this.failureCallbacks.poll()) != null) {
				notifyFailure(callback);
			}
		}
	}

}
