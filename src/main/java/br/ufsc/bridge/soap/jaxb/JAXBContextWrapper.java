package br.ufsc.bridge.soap.jaxb;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class JAXBContextWrapper {

	private static Map<List<Class<?>>, JAXBContext> contextByClass = new HashMap<>();

	private JAXBContextWrapper() {
		// utility class
	}

	public static JAXBContext newInstance(Class<?>... classes) throws JAXBException {
		List<Class<?>> classList = Collections.unmodifiableList(Arrays.asList(classes));
		JAXBContext context = contextByClass.get(classList);

		if (context == null) {
			context = JAXBContext.newInstance(classes);
			contextByClass.put(classList, context);
		}

		return context;
	}

}
