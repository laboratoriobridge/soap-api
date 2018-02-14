package br.ufsc.bridge.soap.http.multipart;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import br.ufsc.bridge.soap.http.exception.SoapInvalidHeaderException;

public class MultipartHeaderTest {

	Header[] headers;

	@Before
	public void init() {
		this.headers = new Header[2];
		this.headers[0] = new BasicHeader(HttpHeaders.CONTENT_TYPE, " Multipart/Related;boundary=\"MIME_boundary\";\n" +
				"    type=\"application/xop+xml\";\n" +
				"    start=\"<mymessage.xml@example.org>\";\n" +
				"    startinfo=\"application/soap+xml; action=\"ProcessData\"");

		this.headers[1] = new BasicHeader("MIME-Version", "1.0");
	}

	@Test
	public void isMultipart() throws SoapInvalidHeaderException, UnsupportedEncodingException {
		MultipartHeader header = new MultipartHeader(this.headers);
		Assert.assertTrue(header.isMultipart());
		Assert.assertEquals("MIME_boundary", new String(header.boundaryToByteArray(), "UTF-8"));
		Assert.assertTrue(header.hasStart());
		Assert.assertEquals("mymessage.xml@example.org", header.getStart());
	}

	@Test
	public void notIsMultipart() throws SoapInvalidHeaderException {
		this.headers[0] = new BasicHeader(HttpHeaders.ACCEPT_CHARSET, "UTF-8");
		MultipartHeader header = new MultipartHeader(this.headers);
		Assert.assertFalse(header.isMultipart());
		Assert.assertNull(header.boundaryToByteArray());
		Assert.assertFalse(header.hasStart());
		Assert.assertNull(header.getStart());
	}

	@Test
	public void noHeader() throws SoapInvalidHeaderException {
		MultipartHeader header = new MultipartHeader(new Header[0]);
		Assert.assertFalse(header.isMultipart());
		Assert.assertNull(header.boundaryToByteArray());
		Assert.assertFalse(header.hasStart());
		Assert.assertNull(header.getStart());
	}

	@Test
	public void boundaryEndNoStart() throws SoapInvalidHeaderException, UnsupportedEncodingException {
		this.headers[0] = new BasicHeader(HttpHeaders.CONTENT_TYPE, " Multipart/Related;" +
				"type=\"application/xop+xml\";" +
				"startinfo=\"application/soap+xml; action=\"ProcessData\";" +
				"boundary=MIME_boundary");

		MultipartHeader header = new MultipartHeader(this.headers);
		Assert.assertTrue(header.isMultipart());
		Assert.assertEquals("MIME_boundary", new String(header.boundaryToByteArray(), "UTF-8"));
		Assert.assertFalse(header.hasStart());
		Assert.assertNull(header.getStart());
	}

	@Test(expected = SoapInvalidHeaderException.class)
	public void isMultipartNoBoundary() throws UnsupportedEncodingException, SoapInvalidHeaderException {
		this.headers[0] = new BasicHeader(HttpHeaders.CONTENT_TYPE, " Multipart/Related;" +
				"type=\"application/xop+xml\";" +
				"start=\"<mymessage.xml@example.org>\";" +
				"startinfo=\"application/soap+xml; action=\"ProcessData\"");

		new MultipartHeader(this.headers);
	}
}
