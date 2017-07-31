package br.ufsc.bridge.soap.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import br.ufsc.bridge.soap.http.exception.SoapCreateMessageException;
import br.ufsc.bridge.soap.http.exception.SoapHttpConnectionException;
import br.ufsc.bridge.soap.http.exception.SoapHttpResponseException;
import br.ufsc.bridge.soap.http.exception.SoapReadMessageException;

public class SoapHttpClient<T> {

	private static Logger logger = Logger.getLogger(SoapHttpClient.class.getName());

	private CloseableHttpClient httpClient;

	private Map<String, String> customHeaders;
	private URL url;
	private HttpHost host;
	private SOAPMessageInterpreter<T> soapMessage;

	public SoapHttpClient(SOAPMessageInterpreter<T> soapMessage) {
		this.soapMessage = soapMessage;

		RequestConfig requestConfig = RequestConfig.custom()
				.setSocketTimeout(60000)
				.setConnectTimeout(30000)
				.setExpectContinueEnabled(false)
				.setRedirectsEnabled(true)
				.build();

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		cm.setValidateAfterInactivity(10000);
		cm.setDefaultSocketConfig(SocketConfig.custom().setTcpNoDelay(true).build());

		this.httpClient = HttpClients.custom()
				.setDefaultRequestConfig(requestConfig)
				.setConnectionManager(cm)
				.build();

		this.customHeaders = new HashMap<>();
		this.putHeader("Content-Type", "application/soap+xml;charset=UTF-8");
		this.putHeader("Accept-Encoding", "gzip,deflate");
	}

	public void putHeader(String key, String value) {
		this.customHeaders.put(key, value);
	}

	public void setUrl(String url) throws MalformedURLException {
		this.url = new URL(url);
		this.host = new HttpHost(this.url.getHost(), this.url.getPort(), this.url.getProtocol());
	}

	public T send(Object jaxbObject) throws SoapHttpConnectionException, SoapHttpResponseException, SoapCreateMessageException, SoapReadMessageException {
		HttpPost httpPost = null;
		InputStream is = null;
		try {
			httpPost = new HttpPost(this.url.getFile());

			httpPost.setEntity(this.createMessage(jaxbObject));

			for (Map.Entry<String, String> header : this.customHeaders.entrySet()) {
				httpPost.setHeader(header.getKey(), header.getValue());
			}

			HttpResponse response = this.httpClient.execute(this.host, httpPost);
			int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
				String error = IOUtils.toString(is = response.getEntity().getContent());
				throw new SoapHttpResponseException("HTTP Response code: " + responseCode + " | error:" + error);
			} else if (responseCode != HttpStatus.SC_OK) {
				throw new SoapHttpResponseException("HTTP Response code: " + responseCode);
			}

			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is = response.getEntity().getContent());
			return this.soapMessage.readMessage(document);
		} catch (IOException e) {
			if (null != httpPost) {
				httpPost.abort();
			}
			throw new SoapHttpConnectionException("Error in connection", e);
		} catch (SOAPException e) {
			throw new SoapCreateMessageException("Error generating SOAP message", e);
		} catch (SAXException | ParserConfigurationException e) {
			throw new SoapReadMessageException("Error parsing XML response", e);
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException ioe) {
					logger.log(Level.SEVERE, "Error while closing response inputStream", ioe);
				}
			}
		}
	}

	private ByteArrayEntity createMessage(Object jaxbObject) throws SOAPException, SoapCreateMessageException, IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		this.soapMessage.createMessage(jaxbObject).writeTo(outputStream);
		return new ByteArrayEntity(outputStream.toByteArray());
	}
}
