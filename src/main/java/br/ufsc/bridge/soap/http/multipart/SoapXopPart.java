package br.ufsc.bridge.soap.http.multipart;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.mime.MIME;

import br.ufsc.bridge.soap.http.exception.SoapInvalidHeaderException;

@Getter
public class SoapXopPart {

	private Map<String, String> headers;
	private InputStream body;

	public SoapXopPart(InputStream body) throws SoapInvalidHeaderException {
		this.body = body;
	}

	public SoapXopPart(String rawHeaders, InputStream body) throws SoapInvalidHeaderException {
		if (StringUtils.isBlank(rawHeaders)) {
			throw new SoapInvalidHeaderException("Header não pode ser vazio");
		}
		try {
			this.parseHeaders(rawHeaders);
		} catch (IOException e) {
			// stringreader, string em memória
		}
		this.body = body;
	}

	public String getHeader(String headerName) {
		return this.headers.get(headerName);
	}

	public String getType() {
		return this.getHeader(HttpHeaders.CONTENT_TYPE.toLowerCase());
	}

	public String getTransferEncoding() {
		return this.getHeader(MIME.CONTENT_TRANSFER_ENC.toLowerCase());
	}

	public String getID() {
		return StringUtils.substringBetween(this.getHeader(SoapMultipartConstants.CONTENT_ID.toLowerCase()), "<", ">");
	}

	public String getBodyAsString() throws IOException {
		return IOUtils.toString(this.getBody(), "UTF-8");
	}

	public InputStream getBody() throws IOException {
		if (this.body.markSupported()) {
			this.body.reset();
		} else if (this.body.available() <= 0) {
			throw new IllegalStateException("InputStream has already been read");
		}
		return this.body;
	}

	private void parseHeaders(String rawHeaders) throws IOException {
		this.headers = new HashMap<>();
		StringReader stream = new StringReader(rawHeaders);
		StringBuilder sb = new StringBuilder();
		String key = null;
		boolean inKey = true;
		int c = stream.read();

		mainloop: while (c >= 0) {
			switch (c) {
			case ':':
				if (inKey) {
					key = sb.toString().toLowerCase();
					sb.setLength(0);
					inKey = false;
				} else {
					sb.append((char) c);
				}
				break;
			case '\t':
			case ' ':
				sb.append(' ');
				break;
			case '\n':
			case '\r':
				// We need to check two at least character to detect end of headers and line folding
				int pc = c;
				c = stream.read();
				if (pc == '\r' && c == '\n') {
					// Got CRLF (correct newline sequence), need to check more...
					c = stream.read();
					if (c == '\r') {
						// Got CRLF + CR need one more...
						c = stream.read();
					}
				}
				if (c == ' ' || c == '\t') {
					// CRWS or LFWS or CRLFWS or CRLFCRWS
					// line folding
					sb.append(' ');
				} else {
					// header separator
					if (key != null) {
						this.headers.put(key, sb.toString().trim());
					}
					sb.setLength(0);
					if (c == '\r' || c == '\n') {
						// CRCR or LFLF or LFCR or CRLFLF or CRLFCRLF or CRLFCRCR
						// end of headers
						key = null;
						break mainloop;
					}
					inKey = true;
					sb.append((char) c);
				}
				break;
			default:
				sb.append((char) c);
				break;
			}
			c = stream.read();
		}
		if (key != null) {
			this.headers.put(key, sb.toString().trim());
		}
		sb.setLength(0);
	}
}
