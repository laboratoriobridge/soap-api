package br.ufsc.bridge.soap.http.multipart;

import static br.ufsc.bridge.soap.http.multipart.SoapMultipartConstants.BOUNDARY_KEY;
import static br.ufsc.bridge.soap.http.multipart.SoapMultipartConstants.START_KEY;

import java.io.UnsupportedEncodingException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;

import br.ufsc.bridge.soap.http.exception.SoapInvalidHeaderException;

@AllArgsConstructor
@NoArgsConstructor
public class MultipartHeader {
	private String start;
	private String boundary;
	@Getter
	private boolean multipart = false;

	public MultipartHeader(@NonNull Header[] headers) throws SoapInvalidHeaderException {
		for (Header header : headers) {
			if (StringUtils.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE, header.getName())) {

				if (StringUtils.startsWithIgnoreCase(StringUtils.trim(header.getValue()), "multipart/")) {
					this.multipart = true;
				}

				if (StringUtils.contains(header.getValue(), BOUNDARY_KEY)) {
					this.boundary = this.extractKey(header, BOUNDARY_KEY);
				}

				if (StringUtils.contains(header.getValue(), START_KEY)) {
					this.start = this.extractKey(header, START_KEY);
				}
			}
		}
		if (this.multipart && StringUtils.isBlank(this.boundary)) {
			throw new SoapInvalidHeaderException("Boundary can't be null in Multipart response");
		}
	}

	private String extractKey(Header header, String key) {
		return StringUtils.substringBetween(
				StringUtils.appendIfMissing(header.getValue(), ";"),
				key + "=",
				";");
	}

	public boolean hasStart() {
		return StringUtils.isNotBlank(this.start);
	}

	public String getStart() {
		return StringUtils.substringBetween(this.start, "<", ">");
	}

	public byte[] boundaryToByteArray() {
		if (this.multipart) {
			byte[] bytes;
			try {
				bytes = this.boundary.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				bytes = this.boundary.getBytes();
			}
			return bytes;
		} else {
			return null;
		}
	}
}
