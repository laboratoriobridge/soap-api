package br.ufsc.bridge.soap.http.multipart;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import br.ufsc.bridge.soap.http.exception.SoapInvalidHeaderException;

public class SoapXopPartTest {

	@Test(expected = SoapInvalidHeaderException.class)
	public void parseEmptyHeader() throws SoapInvalidHeaderException {
		new SoapXopPart("  ", null);
	}

	@Test
	public void parseHeaderLF() throws SoapInvalidHeaderException, IOException {
		String header = "Content-Type: application/xop+xml; \n" +
				"    charset=UTF-8; \n" +
				"    type=\"text/xml\"\n" +
				"Content-Transfer-Encoding: 8bit\n" +
				"Content-ID: <mymessage.xml@example.org>";

		SoapXopPart soapXopPart = new SoapXopPart(header, new ByteArrayInputStream("teste".getBytes("UTF-8")));
		assertEquals("application/xop+xml;     charset=UTF-8;     type=\"text/xml\"", soapXopPart.getType());
		assertEquals("8bit", soapXopPart.getTransferEncoding());
		assertEquals("mymessage.xml@example.org", soapXopPart.getID());
		assertEquals("<mymessage.xml@example.org>", soapXopPart.getHeader("content-id"));
		assertEquals("teste", soapXopPart.getBodyAsString());
		assertEquals(3, soapXopPart.getHeaders().size());
	}

	@Test
	public void bodyReset() throws SoapInvalidHeaderException, IOException {
		String header = "Content-Type: application/xop+xml; \n" +
				"    charset=UTF-8; \n" +
				"    type=\"text/xml\"\n" +
				"Content-Transfer-Encoding: 8bit\n" +
				"Content-ID: <mymessage.xml@example.org>";

		SoapXopPart soapXopPart = new SoapXopPart(header, new ByteArrayInputStream("teste".getBytes("UTF-8")));
		assertEquals("teste", soapXopPart.getBodyAsString());
		assertEquals("teste", soapXopPart.getBodyAsString());

		assertEquals("teste", IOUtils.toString(soapXopPart.getBody(), "UTF-8"));
		assertEquals("teste", IOUtils.toString(soapXopPart.getBody(), "UTF-8"));
	}

	@Test(expected = IllegalStateException.class)
	public void bodyResetExpection() throws SoapInvalidHeaderException, IOException {
		String header = "Content-Type: application/xop+xml; \n" +
				"    charset=UTF-8; \n" +
				"    type=\"text/xml\"\n" +
				"Content-Transfer-Encoding: 8bit\n" +
				"Content-ID: <mymessage.xml@example.org>";

		SoapXopPart soapXopPart = new SoapXopPart(header,
				new FileInputStream(System.getProperty("user.dir") + "/src/test/resources/multipart/docs/doc-part.xml"));
		soapXopPart.getBodyAsString();
		soapXopPart.getBodyAsString();
	}

	@Test
	public void parseHeaderLFWithBody() throws SoapInvalidHeaderException {
		String header = "Content-Type: application/xop+xml; \n" +
				"    charset=UTF-8; \n" +
				"    type=\"text/xml\"\n" +
				"Content-Transfer-Encoding: 8bit\n" +
				"Content-ID: <mymessage.xml@example.org>\n"
				+ "\n"
				+ "body";

		SoapXopPart soapXopPart = new SoapXopPart(header, null);
		assertEquals("application/xop+xml;     charset=UTF-8;     type=\"text/xml\"", soapXopPart.getType());
		assertEquals("8bit", soapXopPart.getTransferEncoding());
		assertEquals("mymessage.xml@example.org", soapXopPart.getID());
		assertEquals(3, soapXopPart.getHeaders().size());
	}

	@Test
	public void parseHeaderCR() throws SoapInvalidHeaderException {
		String header = "Content-Type: application/xop+xml; \r" +
				"    charset=UTF-8; \r" +
				"    type=\"text/xml\"\r" +
				"Content-Transfer-Encoding: 8bit\r" +
				"Content-ID: <mymessage.xml@example.org>";

		SoapXopPart soapXopPart = new SoapXopPart(header, null);
		assertEquals("application/xop+xml;     charset=UTF-8;     type=\"text/xml\"", soapXopPart.getType());
		assertEquals("8bit", soapXopPart.getTransferEncoding());
		assertEquals("mymessage.xml@example.org", soapXopPart.getID());
		assertEquals(3, soapXopPart.getHeaders().size());
	}

	@Test
	public void parseHeaderCRWithBody() throws SoapInvalidHeaderException {
		String header = "Content-Type: application/xop+xml; \r" +
				"    charset=UTF-8; \r" +
				"    type=\"text/xml\"\r" +
				"Content-Transfer-Encoding: 8bit\r" +
				"Content-ID: <mymessage.xml@example.org>\r"
				+ "\r"
				+ "body";

		SoapXopPart soapXopPart = new SoapXopPart(header, null);
		assertEquals("application/xop+xml;     charset=UTF-8;     type=\"text/xml\"", soapXopPart.getType());
		assertEquals("8bit", soapXopPart.getTransferEncoding());
		assertEquals("mymessage.xml@example.org", soapXopPart.getID());
		assertEquals(3, soapXopPart.getHeaders().size());
	}

	@Test
	public void parseHeaderCRLFWithBody() throws SoapInvalidHeaderException {
		String header = "Content-Type: application/xop+xml; \r\n" +
				"    charset=UTF-8; \r\n" +
				"    type=\"text/xml\"\r\n" +
				"Content-Transfer-Encoding: 8bit\r\n" +
				"Content-ID: <mymessage.xml@example.org>\r\n"
				+ "\r\n"
				+ "body";

		SoapXopPart soapXopPart = new SoapXopPart(header, null);
		assertEquals("application/xop+xml;     charset=UTF-8;     type=\"text/xml\"", soapXopPart.getType());
		assertEquals("8bit", soapXopPart.getTransferEncoding());
		assertEquals("mymessage.xml@example.org", soapXopPart.getID());
		assertEquals(3, soapXopPart.getHeaders().size());
	}
}
