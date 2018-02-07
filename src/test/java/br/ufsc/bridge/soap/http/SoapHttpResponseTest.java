package br.ufsc.bridge.soap.http;

import static br.ufsc.bridge.soap.http.utils.SoapTestFileUtils.inputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import br.ufsc.bridge.soap.http.exception.SoapInvalidHeaderException;
import br.ufsc.bridge.soap.http.exception.SoapReadMessageException;
import br.ufsc.bridge.soap.http.multipart.MultipartHeader;
import br.ufsc.bridge.soap.http.multipart.SoapXopPart;
import br.ufsc.bridge.soap.http.utils.SoapTestFileUtils;
import br.ufsc.bridge.soap.xpath.XPathFactoryAssist;

public class SoapHttpResponseTest {

	private static final String SOAP_PART = "/multipart/docs/soap-part.xml";
	private static final String DOC_PART = "/multipart/docs/doc-part.xml";

	Header[] headers;

	@Before
	public void init() {
		this.headers = new Header[2];
		this.headers[0] = new BasicHeader(HttpHeaders.CONTENT_TYPE, " Multipart/Related;boundary=MIME_boundary;\n" +
				"    type=\"application/xop+xml\";\n" +
				"    start=\"<mymessage.xml@example.org>\";\n" +
				"    startinfo=\"application/soap+xml; action=\"ProcessData\"");

		this.headers[1] = new BasicHeader("MIME-Version", "1.0");
	}

	@Test
	public void simpleMultipart() throws IOException, SoapInvalidHeaderException {
		SoapHttpResponse response = new SoapHttpResponse(inputStream("/multipart/simple/multipart.txt"), this.headers);
		Assert.assertEquals(1, response.getParts().size());

		MultipartHeader header = response.getHeader();
		Assert.assertTrue(header.isMultipart());
		Assert.assertEquals("MIME_boundary", new String(header.boundaryToByteArray(), "UTF-8"));
		Assert.assertTrue(header.hasStart());
		Assert.assertEquals("mymessage.xml@example.org", header.getStart());

		SoapXopPart soapXopPart = response.getParts().get(0);
		Assert.assertEquals("0.urn:uuid:A62217736BFD4F63631516388208911@apache.org", soapXopPart.getID());
		Assert.assertEquals("teste", soapXopPart.getBodyAsString());
	}

	@Test
	public void simpleMultipartSoapFileNotFound() throws IOException, SoapInvalidHeaderException {
		SoapHttpResponse response = new SoapHttpResponse(inputStream("/multipart/simple/multipart.txt"), this.headers);
		Assert.assertEquals(1, response.getParts().size());

		try {
			response.getSoap();
		} catch (SoapReadMessageException e) {
			Assert.assertEquals("Error parsing multipart, soap file not found", e.getMessage());
		}
	}

	@Test
	public void soapXopMultipartDocFirst()
			throws IOException, SoapInvalidHeaderException, SoapReadMessageException, XPathExpressionException, SAXException, ParserConfigurationException {
		this.headers[0] = new BasicHeader(HttpHeaders.CONTENT_TYPE, " multipart/related; \n" +
				"	  boundary=MIMEBoundaryurn_uuid_A62217736BFD4F63631516388993169; \n" +
				"	  type=\"application/xop+xml\"; \n" +
				"	  start=\"<0.urn:uuid:A62217736BFD4F63631516388993170@apache.org>\"; \n" +
				"	  start-info=\"application/soap+xml\"; \n" +
				"	  action=\"urn:ihe:iti:2007:RetrieveDocumentSetResponse\"");

		SoapHttpResponse response = new SoapHttpResponse(inputStream("/multipart/soapxop-doc-first/soap-xop.txt"), this.headers);
		Assert.assertEquals(2, response.getParts().size());

		String soapPartExpected = SoapTestFileUtils.toString(SOAP_PART);

		Assert.assertEquals(soapPartExpected, documentToString(response.getSoap()));

		SoapXopPart soapXopPart = response.getParts().get(0);
		Assert.assertEquals("1.urn:uuid:A62217736BFD4F63631516388993171@apache.org", soapXopPart.getID());
		String docExpected = SoapTestFileUtils.toString(DOC_PART);
		Assert.assertEquals(docExpected, soapXopPart.getBodyAsString());

		soapXopPart = response.getParts().get(1);
		Assert.assertEquals("0.urn:uuid:A62217736BFD4F63631516388993170@apache.org", soapXopPart.getID());
		Assert.assertEquals(soapPartExpected, soapXopPart.getBodyAsString());

		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(IOUtils.toInputStream(soapPartExpected), "UTF-8");
		XPathFactoryAssist xPathAssist = new XPathFactoryAssist(document).getXPathAssist("//Body//DocumentResponse");
		InputStream part = response.getPart(xPathAssist, "./Document");
		Assert.assertEquals(docExpected, IOUtils.toString(part, "UTF-8"));
	}

	@Test
	public void soapXopMultipartFull()
			throws IOException, SoapInvalidHeaderException, SoapReadMessageException, XPathExpressionException, SAXException, ParserConfigurationException {
		this.headers[0] = new BasicHeader(HttpHeaders.CONTENT_TYPE, " multipart/related; \n" +
				"	  boundary=MIMEBoundaryurn_uuid_A62217736BFD4F63631516388993169; \n" +
				"	  type=\"application/xop+xml\"; \n" +
				"	  start=\"<0.urn:uuid:A62217736BFD4F63631516388993170@apache.org>\"; \n" +
				"	  start-info=\"application/soap+xml\"; \n" +
				"	  action=\"urn:ihe:iti:2007:RetrieveDocumentSetResponse\"");

		SoapHttpResponse response = new SoapHttpResponse(inputStream("/multipart/soapxop-full/soap-xop.txt"), this.headers);
		Assert.assertEquals(2, response.getParts().size());

		String soapPartExpected = SoapTestFileUtils.toString(SOAP_PART);

		Assert.assertEquals(soapPartExpected, documentToString(response.getSoap()));

		SoapXopPart soapXopPart = response.getParts().get(0);
		Assert.assertEquals("0.urn:uuid:A62217736BFD4F63631516388993170@apache.org", soapXopPart.getID());
		Assert.assertEquals(soapPartExpected, soapXopPart.getBodyAsString());

		soapXopPart = response.getParts().get(1);
		Assert.assertEquals("1.urn:uuid:A62217736BFD4F63631516388993171@apache.org", soapXopPart.getID());
		String docExpected = SoapTestFileUtils.toString(DOC_PART);
		Assert.assertEquals(docExpected, soapXopPart.getBodyAsString());

		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(IOUtils.toInputStream(soapPartExpected), "UTF-8");
		XPathFactoryAssist xPathAssist = new XPathFactoryAssist(document).getXPathAssist("//Body//DocumentResponse");
		InputStream part = response.getPart(xPathAssist, "./Document");
		Assert.assertEquals(docExpected, IOUtils.toString(part, "UTF-8"));
	}

	@Test
	public void soapXopMultipartNoStart()
			throws IOException, SoapInvalidHeaderException, SoapReadMessageException, XPathExpressionException, SAXException, ParserConfigurationException {
		this.headers[0] = new BasicHeader(HttpHeaders.CONTENT_TYPE, " multipart/related; \n" +
				"	  boundary=MIMEBoundaryurn_uuid_A62217736BFD4F63631516388993169; \n" +
				"	  type=\"application/xop+xml\"; \n" +
				"	  action=\"urn:ihe:iti:2007:RetrieveDocumentSetResponse\"");

		SoapHttpResponse response = new SoapHttpResponse(inputStream("/multipart/soapxop-noheader/soap-xop.txt"), this.headers);
		Assert.assertEquals(2, response.getParts().size());

		String soapPartExpected = SoapTestFileUtils.toString(SOAP_PART);

		Assert.assertEquals(soapPartExpected, documentToString(response.getSoap()));

		SoapXopPart soapXopPart = response.getParts().get(0);
		Assert.assertEquals("0.urn:uuid:A62217736BFD4F63631516388993170@apache.org", soapXopPart.getID());
		Assert.assertEquals(soapPartExpected, soapXopPart.getBodyAsString());

		soapXopPart = response.getParts().get(1);
		Assert.assertEquals("1.urn:uuid:A62217736BFD4F63631516388993171@apache.org", soapXopPart.getID());
		String docExpected = SoapTestFileUtils.toString(DOC_PART);
		Assert.assertEquals(docExpected, soapXopPart.getBodyAsString());

		XPathFactoryAssist xPathAssist = new XPathFactoryAssist(response.getSoap()).getXPathAssist("//Body//DocumentResponse");
		InputStream part = response.getPart(xPathAssist, "./Document");
		Assert.assertEquals(docExpected, IOUtils.toString(part, "UTF-8"));
	}

	@Test
	public void applicationSoapXml() throws IOException, SoapInvalidHeaderException, SoapReadMessageException, XPathExpressionException {
		this.headers[0] = new BasicHeader(HttpHeaders.CONTENT_TYPE, "HTTP/1.1 200 OK\n" +
				"Content-Type: application/soap+xml; charset=UTF-8\n" +
				"Date: Mon, 05 Feb 2018 11:29:47 GMT\n" +
				"Transfer-Encoding: chunked\n" +
				"Connection: Keep-Alive\n");

		String appSoap = SoapTestFileUtils.toString("/application-soap/application-soap.txt");
		SoapHttpResponse response = new SoapHttpResponse(IOUtils.toInputStream(appSoap, "UTF-8"), this.headers);
		Assert.assertEquals(0, response.getParts().size());

		String request = SoapTestFileUtils.toString("/application-soap/application-soap.txt");
		Assert.assertEquals(request, documentToString(response.getSoap()));

		XPathFactoryAssist xPathAssist = new XPathFactoryAssist(response.getSoap()).getXPathAssist("//Body//DocumentResponse");
		InputStream part = response.getPart(xPathAssist, "./Document");
		String docExpected = SoapTestFileUtils.toString(DOC_PART);
		Assert.assertEquals(docExpected, IOUtils.toString(part, "UTF-8"));
	}

	public static String documentToString(Document doc) {
		try {
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString().replace(" standalone=\"no\"", "");
		} catch (Exception ex) {
			throw new RuntimeException("Error converting to String", ex);
		}
	}
}
