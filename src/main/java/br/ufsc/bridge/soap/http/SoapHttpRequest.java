package br.ufsc.bridge.soap.http;

import static br.ufsc.bridge.soap.http.multipart.SoapMultipartConstants.ACTION_KEY;
import static br.ufsc.bridge.soap.http.multipart.SoapMultipartConstants.CONTENT_ID;
import static br.ufsc.bridge.soap.http.multipart.SoapMultipartConstants.MULTIPART_RELATED_TYPE;
import static br.ufsc.bridge.soap.http.multipart.SoapMultipartConstants.SOAP_ACTION_KEY;
import static br.ufsc.bridge.soap.http.multipart.SoapMultipartConstants.SOAP_TYPE;
import static br.ufsc.bridge.soap.http.multipart.SoapMultipartConstants.SOAP_XOP_TYPE;
import static br.ufsc.bridge.soap.http.multipart.SoapMultipartConstants.START_KEY;
import static br.ufsc.bridge.soap.http.multipart.SoapMultipartConstants.TEXT_XML_UTF8_TYPE;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.message.BasicNameValuePair;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class SoapHttpRequest {

	private String url;
	private boolean multipart = false;
	private String action;
	private Map<String, String> headers = new HashMap<>();
	private String soapId;
	private InputStream body;
	private Map<String, InputStream> parts;

	public SoapHttpRequest(String url, String action, InputStream body) {
		this(url, action, null, body);
		this.multipart = false;
	}

	public SoapHttpRequest(String url, String action, String soapId, InputStream soap) {
		this(url, action, soapId, soap, null);
	}

	public SoapHttpRequest(String url, String action, String soapId, InputStream soap, Map<String, InputStream> parts) {
		this.url = url;
		this.action = action;
		this.soapId = soapId;
		this.headers.put(SOAP_ACTION_KEY, action);
		this.body = soap;
		this.parts = parts;
		this.multipart = true;
	}

	public HttpRequestBase httpRequest(Map<String, String> customHeaders) {
		HttpPost httpPost = new HttpPost(this.url);
		httpPost.setEntity(this.httpEntity());
		for (Map.Entry<String, String> header : customHeaders.entrySet()) {
			httpPost.setHeader(header.getKey(), header.getValue());
		}
		for (Entry<String, String> header : this.headers.entrySet()) {
			httpPost.setHeader(header.getKey(), header.getValue());
		}
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, httpPost.getEntity().getContentType().getValue());
		if (log.isDebugEnabled()) {
			this.logRequest(httpPost);
		}
		return httpPost;
	}

	public HttpEntity httpEntity() {
		HttpEntity httpEntity;
		if (this.multipart) {
			httpEntity = this.multipartEntity();
		} else {
			httpEntity = EntityBuilder.create()
					.setContentType(StringUtils.isBlank(this.action)
							? SOAP_TYPE
							: SOAP_TYPE.withParameters(new BasicNameValuePair(ACTION_KEY, this.action)))
					.setStream(this.body).build();
		}
		return httpEntity;
	}

	public HttpEntity multipartEntity() {
		String id = this.addPointyBrackets(this.soapId);
		ContentType multipartType = StringUtils.isBlank(this.action)
				? MULTIPART_RELATED_TYPE.withParameters(
						new BasicNameValuePair(START_KEY, id))
				: MULTIPART_RELATED_TYPE.withParameters(
						new BasicNameValuePair(START_KEY, id),
						new BasicNameValuePair(ACTION_KEY, this.action));

		MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
				.setMode(HttpMultipartMode.STRICT)
				.seContentType(multipartType);

		entityBuilder.addPart(FormBodyPartBuilder.create()
				.setName(id)
				.addField(CONTENT_ID, id)
				.setBody(new InputStreamBody(this.body, SOAP_XOP_TYPE))
				.build());

		if (this.parts != null) {
			for (Entry<String, InputStream> part : this.parts.entrySet()) {
				String partId = StringUtils.removeStart(part.getKey(), "cid:");
				entityBuilder.addPart(FormBodyPartBuilder.create()
						.setName(partId)
						.addField(CONTENT_ID, this.addPointyBrackets(partId))
						.setBody(new InputStreamBody(part.getValue(), TEXT_XML_UTF8_TYPE))
						.build());
			}
		}
		return entityBuilder.build();
	}

	private String addPointyBrackets(String id) {
		return StringUtils.appendIfMissing(StringUtils.prependIfMissing(id, "<"), ">");
	}

	private void logRequest(HttpPost httpPost) {
		StringBuilder builder = new StringBuilder();
		for (Header h : httpPost.getAllHeaders()) {
			builder.append(h.toString() + "\n");
		}
		try {
			ByteArrayOutputStream writer = new ByteArrayOutputStream();
			this.httpEntity().writeTo(writer);
			builder.append("\n" + IOUtils.toString(writer.toByteArray(), "UTF-8"));
			this.body.reset();
			if (this.parts != null) {
				for (InputStream parts : this.parts.values()) {
					parts.reset();
				}
			}
		} catch (Exception e) {
			log.debug("error reading request for debug", e);
		}
		log.debug("HTTP request:\n" + builder.toString() + "\n");
	}
}
