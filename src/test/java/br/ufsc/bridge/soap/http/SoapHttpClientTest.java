package br.ufsc.bridge.soap.http;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ConnectException;

import org.apache.commons.io.IOUtils;
import org.apache.http.ConnectionClosedException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import br.ufsc.bridge.soap.http.exception.SoapHttpConnectionException;
import br.ufsc.bridge.soap.http.exception.SoapHttpResponseException;

public class SoapHttpClientTest {
	private static final String URL = "http://localhost";

	@Mock
	CloseableHttpClient httpClient;

	@Mock
	CloseableHttpResponse response;

	@Mock
	StatusLine status;

	@Mock
	HttpEntity entity;

	Header[] headers;

	private byte[] bodyResponse;

	private String bodyResponseValue;

	private SoapHttpClient soapClient;

	private byte[] bodyRequest;

	@Before
	public void init() throws ClientProtocolException, IOException {
		MockitoAnnotations.initMocks(this);

		this.headers = new Header[2];
		this.headers[0] = new BasicHeader(HttpHeaders.CONTENT_TYPE, " application/soap+xml");
		this.headers[1] = new BasicHeader("MIME-Version", "1.0");

		when(this.httpClient.execute(Matchers.any(HttpPost.class))).thenReturn(this.response);

		when(this.response.getStatusLine()).thenReturn(this.status);
		when(this.response.getEntity()).thenReturn(this.entity);
		when(this.response.getAllHeaders()).thenReturn(this.headers);

		when(this.status.getStatusCode()).thenReturn(200);

		when(this.entity.isStreaming()).thenReturn(false);
		this.bodyResponseValue = "response test";
		this.bodyResponse = this.bodyResponseValue.getBytes("UTF-8");
		ByteArrayInputStream value = new ByteArrayInputStream(this.bodyResponse);
		when(this.entity.getContent()).thenReturn(value);
		when(this.entity.getContentType()).thenReturn(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/soap+xml"));

		this.soapClient = new SoapHttpClient(this.httpClient);

		this.bodyRequest = "request test".getBytes("UTF-8");
	}

	@Test
	public void response200() throws ClientProtocolException, IOException, SoapHttpResponseException, SoapHttpConnectionException {
		SoapHttpResponse send = this.soapClient.request(new SoapHttpRequest(URL, null, this.bodyRequest));
		Assert.assertEquals(this.bodyResponseValue, IOUtils.toString(send.getBody()));
	}

	@Test
	public void responseServerError() throws ClientProtocolException, IOException, SoapHttpConnectionException {
		when(this.status.getStatusCode()).thenReturn(500);

		try {
			this.soapClient.request(new SoapHttpRequest(URL, null, this.bodyRequest));
		} catch (SoapHttpResponseException e) {
			Assert.assertEquals("HTTP Response code: 500 | error: " + this.bodyResponseValue, e.getMessage());
		}
	}

	@Test
	public void responseNotOk() throws ClientProtocolException, IOException, SoapHttpConnectionException {
		when(this.status.getStatusCode()).thenReturn(400);

		try {
			this.soapClient.request(new SoapHttpRequest(URL, null, this.bodyRequest));
		} catch (SoapHttpResponseException e) {
			Assert.assertEquals("HTTP Response code: 400", e.getMessage());
		}
	}

	@Test
	public void responseClosedOnSuccess() throws ClientProtocolException, IOException, SoapHttpResponseException, SoapHttpConnectionException {
		this.soapClient.request(new SoapHttpRequest(URL, null, this.bodyRequest));

		verify(this.response).close();
	}

	@Test
	public void responseClosedOnServerError() throws ClientProtocolException, IOException, SoapHttpConnectionException {
		when(this.status.getStatusCode()).thenReturn(500);

		try {
			this.soapClient.request(new SoapHttpRequest(URL, null, this.bodyRequest));
			Assert.fail("SoapHttpResponseException esperada");
		} catch (SoapHttpResponseException e) {
			// esperado
		}

		verify(this.response).close();
	}

	/**
	 * Sem consumir a entity e fechar o response, a conexão nunca volta para o pool
	 * e uma janela de instabilidade do servidor esgota o PoolingHttpClientConnectionManager.
	 */
	@Test
	public void responseClosedAndEntityConsumedOnOtherErrors() throws ClientProtocolException, IOException, SoapHttpConnectionException {
		when(this.entity.isStreaming()).thenReturn(true);

		for (int statusCode : new int[] { 401, 403, 404, 429, 502, 503, 504 }) {
			when(this.status.getStatusCode()).thenReturn(statusCode);

			try {
				this.soapClient.request(new SoapHttpRequest(URL, null, this.bodyRequest));
				Assert.fail("SoapHttpResponseException esperada para o status " + statusCode);
			} catch (SoapHttpResponseException e) {
				Assert.assertEquals("HTTP Response code: " + statusCode, e.getMessage());
			}
		}

		verify(this.entity, times(7)).getContent();
		verify(this.response, times(7)).close();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void ioExcpetion() throws ClientProtocolException, IOException, SoapHttpResponseException {
		when(this.httpClient.execute(Matchers.any(HttpHost.class), Matchers.any(HttpPost.class)))
				.thenThrow(ConnectionClosedException.class,
						ConnectTimeoutException.class,
						ConnectException.class);

		try {
			this.soapClient.request(new SoapHttpRequest(URL, null, this.bodyRequest));
		} catch (SoapHttpConnectionException e) {
			Assert.assertEquals("Error in connection", e.getMessage());
		}
		try {
			this.soapClient.request(new SoapHttpRequest(URL, null, this.bodyRequest));
		} catch (SoapHttpConnectionException e) {
			Assert.assertEquals("Error in connection", e.getMessage());
		}
		try {
			this.soapClient.request(new SoapHttpRequest(URL, null, this.bodyRequest));
		} catch (SoapHttpConnectionException e) {
			Assert.assertEquals("Error in connection", e.getMessage());
		}
	}
}
