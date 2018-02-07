package br.ufsc.bridge.soap.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import br.ufsc.bridge.soap.http.exception.SoapInvalidHeaderException;
import br.ufsc.bridge.soap.http.exception.SoapReadMessageException;
import br.ufsc.bridge.soap.http.multipart.MultipartHeader;
import br.ufsc.bridge.soap.http.multipart.SoapXopPart;
import br.ufsc.bridge.soap.http.util.ByteArrayOutputStreamNoCopy;
import br.ufsc.bridge.soap.xpath.XPathFactoryAssist;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SoapHttpResponse {

	private MultipartHeader header;
	private SoapXopPart body;
	private List<SoapXopPart> parts;

	public SoapHttpResponse(InputStream is, Header[] headers) throws IOException, SoapInvalidHeaderException {
		this.header = new MultipartHeader(headers);
		this.parts = new ArrayList<>();
		if (this.header.isMultipart()) {
			MultipartStream stream = new MultipartStream(is, this.header.boundaryToByteArray(), 1024, null);

			boolean hasNextPart = stream.skipPreamble();
			while (hasNextPart) {
				ByteArrayOutputStreamNoCopy baos = new ByteArrayOutputStreamNoCopy();
				try {
					String partHeaders = stream.readHeaders();
					stream.readBodyData(baos);
					this.parts.add(new SoapXopPart(partHeaders, baos.inputStream()));
				} finally {
					IOUtils.closeQuietly(baos);
				}
				hasNextPart = stream.readBoundary();
			}
		} else {
			this.body = new SoapXopPart(is);
		}
	}

	public InputStream getBody() throws IOException {
		return this.body != null ? this.body.getBody() : null;
	}

	public Document getSoap() throws SoapReadMessageException {
		SoapXopPart soapPart = null;
		if (!this.header.isMultipart()) {
			soapPart = this.body;
		} else if (this.header.hasStart()) {
			for (SoapXopPart soapXopPart : this.parts) {
				if (StringUtils.equals(this.header.getStart(), soapXopPart.getID())) {
					soapPart = soapXopPart;
					break;
				}
			}
		} else {
			soapPart = this.parts.get(0);
		}
		if (soapPart == null) {
			throw new SoapReadMessageException("Error parsing multipart, soap file not found");
		}
		try {
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(soapPart.getBody());
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new SoapReadMessageException("Error parsing XML response", e);
		} catch (Exception e) {
			throw new SoapReadMessageException("Error parsing multipart response", e);
		}
	}

	public InputStream getPart(XPathFactoryAssist xPath, String docExpression) throws SoapReadMessageException {
		try {
			if (this.header.isMultipart()) {
				String id = StringUtils.removeStart(xPath.getString(docExpression + "/Include/@href"), "cid:");
				for (SoapXopPart part : this.parts) {
					if (StringUtils.equals(part.getID(), id)) {
						return part.getBody();
					}
				}
				return null;
			} else {
				return new ByteArrayInputStream(Base64.decodeBase64(xPath.getString(docExpression)));
			}
		} catch (XPathExpressionException e) {
			throw new SoapReadMessageException("Invalid document in response", e);
		} catch (IOException e) {
			throw new SoapReadMessageException("Invalid inputStream in body part", e);
		}
	}
}
