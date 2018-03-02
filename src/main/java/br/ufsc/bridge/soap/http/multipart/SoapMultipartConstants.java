package br.ufsc.bridge.soap.http.multipart;

import org.apache.http.Consts;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicNameValuePair;

public interface SoapMultipartConstants {

	public static final String CONTENT_ID = "Content-ID";
	public static final String BOUNDARY_KEY = "boundary";
	public static final String START_KEY = "start";
	public static final String SOAP_ACTION_KEY = "SOAPAction";
	public static final String ACTION_KEY = "action";

	public static final ContentType MULTIPART_RELATED_TYPE = ContentType.create("multipart/related",
			new BasicNameValuePair("boundary", "MIMEboundary"),
			new BasicNameValuePair("type", "application/xop+xml"),
			new BasicNameValuePair("start-info", "application/soap+xml"));

	public static final ContentType SOAP_XOP_TYPE = ContentType.create("application/xop+xml",
			new BasicNameValuePair("charset", "UTF-8"),
			new BasicNameValuePair("type", "application/soap+xml"));

	public static final ContentType SOAP_TYPE = ContentType.create("application/soap+xml",
			new BasicNameValuePair("charset", "UTF-8"));

	public static final ContentType TEXT_XML_UTF8_TYPE = ContentType.create("text/xml", Consts.UTF_8);
}
