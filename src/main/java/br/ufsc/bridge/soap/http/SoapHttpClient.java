package br.ufsc.bridge.soap.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import br.ufsc.bridge.soap.http.exception.SoapHttpConnectionException;
import br.ufsc.bridge.soap.http.exception.SoapHttpResponseException;
import br.ufsc.bridge.soap.http.exception.SoapInvalidHeaderException;
import br.ufsc.bridge.soap.http.util.ByteArrayOutputStreamNoCopy;

public class SoapHttpClient {
	private CloseableHttpClient httpClient;
	private Map<String, String> customHeaders;

	public SoapHttpClient() {
		this(RequestConfig.custom()
				.setSocketTimeout(60000)
				.setConnectTimeout(30000)
				.setExpectContinueEnabled(false)
				.setRedirectsEnabled(true)
				.build());
	}

	public SoapHttpClient(RequestConfig requestConfig) {
		this(HttpClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.setConnectionManager(connectionManager())
				.build());
	}

	private static PoolingHttpClientConnectionManager connectionManager() {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setValidateAfterInactivity(10000);
		cm.setDefaultSocketConfig(SocketConfig.custom().setTcpNoDelay(true).build());
		return cm;
	}

	public SoapHttpClient(CloseableHttpClient httpClient) {
		this.httpClient = httpClient;

		this.customHeaders = new HashMap<>();
		this.putHeader(HttpHeaders.CONTENT_ENCODING, "gzip,deflate");
	}

	public void putHeader(String key, String value) {
		if (value != null) {
			this.customHeaders.put(key, value);
		} else {
			this.customHeaders.remove(key);
		}
	}

	public SoapHttpResponse request(SoapHttpRequest soapHttpRequest) throws SoapHttpResponseException, SoapHttpConnectionException {
		HttpRequestBase httpRequest = null;
		ByteArrayOutputStreamNoCopy baos = null;
		try {
			HttpResponse response = this.httpClient.execute(httpRequest = soapHttpRequest.httpRequest(this.customHeaders));

			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
				baos = new ByteArrayOutputStreamNoCopy(response.getEntity().getContent());
				throw new SoapHttpResponseException("HTTP Response code: " + responseCode + " | error: " + new String(baos.toByteArray(), "UTF-8"));
			} else if (responseCode != HttpStatus.SC_OK) {
				throw new SoapHttpResponseException("HTTP Response code: " + responseCode);
			}

			baos = new ByteArrayOutputStreamNoCopy(response.getEntity().getContent());
			return new SoapHttpResponse(baos.inputStream(), response.getAllHeaders());
		} catch (IOException e) {
			if (null != httpRequest) {
				httpRequest.abort();
			}
			throw new SoapHttpConnectionException("Error in connection", e);
		} catch (SoapInvalidHeaderException e) {
			throw new SoapHttpResponseException("Multipart file with invalid header", e);
		} finally {
			IOUtils.closeQuietly(baos);
		}
	}
}
