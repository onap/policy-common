package org.onap.policy.common.im;

public class StateManagementException extends Exception{
	private static final long serialVersionUID = 1L;
	public StateManagementException() {
	}
	public StateManagementException(String message) {
		super(message);
	}

	public StateManagementException(Throwable cause) {
		super(cause);
	}
	public StateManagementException(String message, Throwable cause) {
		super(message, cause);
	}

}
