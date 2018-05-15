package br.ufsc.bridge.soap;

import org.apache.http.HttpHeaders;

import br.ufsc.bridge.soap.http.SoapHttpClient;
import br.ufsc.bridge.soap.http.SoapHttpRequest;
import br.ufsc.bridge.soap.http.SoapHttpResponse;
import br.ufsc.bridge.soap.http.exception.SoapHttpConnectionException;
import br.ufsc.bridge.soap.http.exception.SoapHttpResponseException;

public class SoapClient {

	static {
		client = new SoapHttpClient();
	}

	private static SoapHttpClient client;

	private SoapClient() {
		// utility class
	}

	public static SoapHttpResponse request(SoapHttpRequest request) throws SoapHttpResponseException, SoapHttpConnectionException {
		request.addHeader(HttpHeaders.ACCEPT_ENCODING, "gzip,deflate");
		return client.request(request);
	}

}
