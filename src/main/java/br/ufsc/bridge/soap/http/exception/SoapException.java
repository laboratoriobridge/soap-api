package br.ufsc.bridge.soap.http.exception;

public class SoapException extends Exception {
	private static final long serialVersionUID = 1L;

	public SoapException(String message, Throwable cause) {
		super(message, cause);
	}

	public SoapException(String message) {
		super(message);
	}

	public SoapException(Throwable cause) {
		super(cause);
	}
}
