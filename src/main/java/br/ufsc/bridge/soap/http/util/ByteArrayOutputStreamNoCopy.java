package br.ufsc.bridge.soap.http.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.NoArgsConstructor;

import br.ufsc.bridge.soap.http.SoapHttpClient;

@NoArgsConstructor
public class ByteArrayOutputStreamNoCopy extends ByteArrayOutputStream {
	private static Logger logger = Logger.getLogger(SoapHttpClient.class.getName());

	public ByteArrayOutputStreamNoCopy(InputStream is) throws IOException {
		try {
			int n = 0;
			byte[] readBuf = new byte[4 * 1024];
			while (-1 != (n = is.read(readBuf))) {
				this.write(readBuf, 0, n);
			}
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException ioe) {
					logger.log(Level.SEVERE, "Error while closing response inputStream", ioe);
				}
			}
		}
	}

	public InputStream inputStream() {
		return new ByteArrayInputStream(this.buf, 0, this.count);
	}
}