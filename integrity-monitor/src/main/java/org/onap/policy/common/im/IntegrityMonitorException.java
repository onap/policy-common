package org.onap.policy.common.im;

public class IntegrityMonitorException extends Exception{
	private static final long serialVersionUID = 1L;
	public IntegrityMonitorException() {
	}
	public IntegrityMonitorException(String message) {
		super(message);
	}

	public IntegrityMonitorException(Throwable cause) {
		super(cause);
	}
	public IntegrityMonitorException(String message, Throwable cause) {
		super(message, cause);
	}

}
