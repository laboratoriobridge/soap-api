package br.ufsc.bridge.soap.http;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
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

@Slf4j
public class SoapHttpClient {
	private CloseableHttpClient httpClient;

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
	}

	public SoapHttpResponse request(SoapHttpRequest soapHttpRequest) throws SoapHttpResponseException, SoapHttpConnectionException {
		HttpRequestBase httpRequest = null;
		ByteArrayOutputStreamNoCopy baos = null;
		try {
			HttpResponse response = this.httpClient.execute(httpRequest = soapHttpRequest.httpRequest());

			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
				baos = new ByteArrayOutputStreamNoCopy(response.getEntity().getContent());
				throw new SoapHttpResponseException("HTTP Response code: " + responseCode + " | error: " + new String(baos.toByteArray(), "UTF-8"));
			} else if (responseCode != HttpStatus.SC_OK) {
				throw new SoapHttpResponseException("HTTP Response code: " + responseCode);
			}

			baos = new ByteArrayOutputStreamNoCopy(response.getEntity().getContent());
			if (log.isDebugEnabled()) {
				this.logRequestResponse(baos, response, httpRequest);
			}
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

	private void logRequestResponse(ByteArrayOutputStreamNoCopy baos, HttpResponse response, HttpRequestBase httpRequest) {
		StringBuilder builder = new StringBuilder();
		for (Header h : response.getAllHeaders()) {
			builder.append(h.toString() + "\n");
		}
		builder.append("\n" + new String(baos.toByteArray()));
		log.debug("HTTP response:\n" + builder.toString() + "\n");
	}
}
