package br.ufsc.bridge.soap.http.exception;

public class SoapReadMessageException extends SoapException {

	private static final long serialVersionUID = 1L;

	public SoapReadMessageException(String message) {
		super(message);
	}

	public SoapReadMessageException(String message, Throwable cause) {
		super(message, cause);
	}
}
