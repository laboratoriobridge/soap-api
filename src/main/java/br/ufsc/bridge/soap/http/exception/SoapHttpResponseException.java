package br.ufsc.bridge.soap.http.exception;

public class SoapHttpResponseException extends SoapException {

	private static final long serialVersionUID = 1L;

	public SoapHttpResponseException(String message) {
		super(message);
	}

	public SoapHttpResponseException(String message, SoapInvalidHeaderException e) {
		super(message, e);
	}
}
