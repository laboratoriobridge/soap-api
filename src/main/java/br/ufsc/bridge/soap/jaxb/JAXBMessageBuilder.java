package br.ufsc.bridge.soap.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import br.ufsc.bridge.soap.http.SoapCredential;
import br.ufsc.bridge.soap.http.SoapMessageBuilder;

public class JAXBMessageBuilder<T> extends SoapMessageBuilder<T> {
	public JAXBMessageBuilder(SoapCredential c) {
		super(c);
	}

	@Override
	protected Document toDocument(T bodyContent) throws Exception {
		JAXBContext jc = JAXBContextWrapper.newInstance(bodyContent.getClass());

		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

		Marshaller marshaller = jc.createMarshaller();
		marshaller.marshal(bodyContent, document);
		return document;
	}
}
