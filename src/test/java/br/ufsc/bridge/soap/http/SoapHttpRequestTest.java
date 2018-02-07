package br.ufsc.bridge.soap.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpPost;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.ufsc.bridge.soap.http.utils.SoapTestFileUtils;

public class SoapHttpRequestTest {
	public static final String ACTION = "Teste";

	public static final String BODY = "body";
	public static final String PART1 = "part1";
	public static final String PART2 = "part2";

	public static ByteArrayInputStream BODY_BYTE;
	public static ByteArrayInputStream PART1_BYTE;
	public static ByteArrayInputStream PART2_BYTE;

	private SoapHttpClient soapHttpClient;

	@Before
	public void init() throws Exception {
		BODY_BYTE = new ByteArrayInputStream(BODY.getBytes("UTF-8"));
		PART1_BYTE = new ByteArrayInputStream(PART1.getBytes("UTF-8"));
		PART2_BYTE = new ByteArrayInputStream(PART2.getBytes("UTF-8"));

		this.soapHttpClient = new SoapHttpClient();
		this.soapHttpClient.setUrl("http://localhost");
	}

	@Test
	public void applicationSoap() throws IOException {
		SoapHttpRequest request = new SoapHttpRequest(ACTION, BODY_BYTE);

		HttpPost httpPost = this.soapHttpClient.httpPost(request);
		Assert.assertEquals(SoapTestFileUtils.toString("/http-post/app-soap.txt"), httpPostToString(httpPost));
	}

	@Test
	public void applicationSoapNoAction() throws IOException {
		SoapHttpRequest request = new SoapHttpRequest(null, BODY_BYTE);

		HttpPost httpPost = this.soapHttpClient.httpPost(request);
		Assert.assertEquals(SoapTestFileUtils.toString("/http-post/app-soap-noaction.txt"), httpPostToString(httpPost));
	}

	@Test
	public void simpleMultipart() throws IOException {
		SoapHttpRequest request = new SoapHttpRequest(ACTION, BODY, BODY_BYTE);

		HttpPost httpPost = this.soapHttpClient.httpPost(request);
		Assert.assertEquals(SoapTestFileUtils.toString("/http-post/simple-multipart.txt"), httpPostToString(httpPost));
	}

	@Test
	public void simpleMultipartNoAction() throws IOException {
		SoapHttpRequest request = new SoapHttpRequest(null, BODY, BODY_BYTE);

		HttpPost httpPost = this.soapHttpClient.httpPost(request);
		Assert.assertEquals(SoapTestFileUtils.toString("/http-post/simple-multipart-noaction.txt"), httpPostToString(httpPost));
	}

	@Test
	public void multipart() throws IOException {
		HashMap<String, InputStream> parts = new LinkedHashMap<>();
		parts.put(PART2, PART2_BYTE);
		parts.put(PART1, PART1_BYTE);
		SoapHttpRequest request = new SoapHttpRequest(ACTION, BODY, BODY_BYTE, parts);

		HttpPost httpPost = this.soapHttpClient.httpPost(request);
		Assert.assertEquals(SoapTestFileUtils.toString("/http-post/multipart.txt"), httpPostToString(httpPost));
	}

	public static String httpPostToString(HttpPost post) throws IOException {
		ByteArrayOutputStream writer = new ByteArrayOutputStream();
		writer.write(post.toString().getBytes("UTF-8"));
		writer.write(10);
		for (Header header : post.getAllHeaders()) {
			writer.write(header.toString().getBytes("UTF-8"));
			writer.write(10);
		}
		writer.write(10);
		post.getEntity().writeTo(writer);
		return IOUtils.toString(writer.toByteArray(), "UTF-8");
	}
}
