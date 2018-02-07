package br.ufsc.bridge.soap.http.exception;

public class SoapInvalidHeaderException extends Exception {
	private static final long serialVersionUID = 1L;

	public SoapInvalidHeaderException(String message) {
		super(message);
	}
}