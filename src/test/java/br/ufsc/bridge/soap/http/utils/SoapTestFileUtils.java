package br.ufsc.bridge.soap.http.utils;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class SoapTestFileUtils {

	public static final String CR = "\n";

	public static String toString(String file) {
		try {
			FileInputStream stream = new FileInputStream(System.getProperty("user.dir") + "/src/test/resources" + file);
			return IOUtils.toString(stream, "UTF-8").replaceAll("\\\\r", "\r");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static InputStream inputStream(String file) {
		try {
			return new FileInputStream(System.getProperty("user.dir") + "/src/test/resources" + file);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
