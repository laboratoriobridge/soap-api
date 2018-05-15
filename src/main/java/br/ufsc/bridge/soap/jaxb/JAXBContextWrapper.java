package br.ufsc.bridge.soap.jaxb;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class JAXBContextWrapper {

	private static Map<Class<?>, JAXBContext> contextByClass = new HashMap<>();

	private JAXBContextWrapper() {
		// utility class
	}

	public static JAXBContext newInstance(Class<?> clazz) throws JAXBException {
		JAXBContext context = contextByClass.get(clazz);

		if (context == null) {
			context = JAXBContext.newInstance(clazz);
			contextByClass.put(clazz, context);
		}

		return context;
	}

}
