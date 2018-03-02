package br.ufsc.bridge.soap.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.ufsc.bridge.soap.http.utils.SoapTestFileUtils;

public class SoapHttpRequestTest {
	public static final String ACTION = "Teste";
	public static final String URL = "http://localhost";

	public static final String BODY = "body";
	public static final String PART1 = "part1";
	public static final String PART2 = "part2";

	public static byte[] BODY_BYTE;
	public static byte[] PART1_BYTE;
	public static byte[] PART2_BYTE;

	private HashMap<String, String> headers;

	@Before
	public void init() throws Exception {
		BODY_BYTE = BODY.getBytes("UTF-8");
		PART1_BYTE = PART1.getBytes("UTF-8");
		PART2_BYTE = PART2.getBytes("UTF-8");

		this.headers = new LinkedHashMap<>();
		this.headers.put(HttpHeaders.CONTENT_ENCODING, "gzip,deflate");
		this.headers.put("SOAPAction", ACTION);
	}

	@Test
	public void applicationSoap() throws IOException {
		SoapHttpRequest request = new SoapHttpRequest(URL, ACTION, BODY_BYTE);

		HttpRequestBase httpPost = request.httpRequest(this.headers);
		Assert.assertEquals(SoapTestFileUtils.toString("/http-post/app-soap.txt"), httpPostToString(httpPost));
	}

	@Test
	public void applicationSoapNoAction() throws IOException {
		SoapHttpRequest request = new SoapHttpRequest(URL, null, BODY_BYTE);

		HttpRequestBase httpPost = request.httpRequest(this.headers);
		Assert.assertEquals(SoapTestFileUtils.toString("/http-post/app-soap-noaction.txt"), httpPostToString(httpPost));
	}

	@Test
	public void simpleMultipart() throws IOException {
		SoapHttpRequest request = new SoapHttpRequest(URL, ACTION, BODY, BODY_BYTE);

		HttpRequestBase httpPost = request.httpRequest(this.headers);
		Assert.assertEquals(SoapTestFileUtils.toString("/http-post/simple-multipart.txt"), httpPostToString(httpPost));
	}

	@Test
	public void simpleMultipartNoAction() throws IOException {
		SoapHttpRequest request = new SoapHttpRequest(URL, null, BODY, BODY_BYTE);

		HttpRequestBase httpPost = request.httpRequest(this.headers);
		Assert.assertEquals(SoapTestFileUtils.toString("/http-post/simple-multipart-noaction.txt"), httpPostToString(httpPost));
	}

	@Test
	public void multipart() throws IOException {
		HashMap<String, byte[]> parts = new LinkedHashMap<>();
		parts.put(PART2, PART2_BYTE);
		parts.put(PART1, PART1_BYTE);
		SoapHttpRequest request = new SoapHttpRequest(URL, ACTION, BODY, BODY_BYTE, parts);

		HttpRequestBase httpPost = request.httpRequest(this.headers);
		Assert.assertEquals(SoapTestFileUtils.toString("/http-post/multipart.txt"), httpPostToString(httpPost));
	}

	public static String httpPostToString(HttpRequestBase post) throws IOException {
		ByteArrayOutputStream writer = new ByteArrayOutputStream();
		writer.write(post.toString().getBytes("UTF-8"));
		writer.write(10);
		for (Header header : post.getAllHeaders()) {
			writer.write(header.toString().getBytes("UTF-8"));
			writer.write(10);
		}
		writer.write(10);
		if (post instanceof HttpPost) {
			((HttpPost) post).getEntity().writeTo(writer);
		}
		return IOUtils.toString(writer.toByteArray(), "UTF-8");
	}
}
