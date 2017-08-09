package org.onap.policy.common.ia;

public class IntegrityAuditException extends Exception{
	private static final long serialVersionUID = 1L;
	public IntegrityAuditException() {
	}
	public IntegrityAuditException(String message) {
		super(message);
	}

	public IntegrityAuditException(Throwable cause) {
		super(cause);
	}
	public IntegrityAuditException(String message, Throwable cause) {
		super(message, cause);
	}
}
