package org.onap.policy.common.im;

public class StateTransitionException extends Exception{
	private static final long serialVersionUID = 1L;
	public StateTransitionException() {
	}
	public StateTransitionException(String message) {
		super(message);
	}

	public StateTransitionException(Throwable cause) {
		super(cause);
	}
	public StateTransitionException(String message, Throwable cause) {
		super(message, cause);
	}

}
