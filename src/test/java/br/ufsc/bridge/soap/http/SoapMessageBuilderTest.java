package br.ufsc.bridge.soap.http;

import java.io.ByteArrayInputStream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import br.ufsc.bridge.soap.jaxb.JAXBMessageBuilder;
import br.ufsc.bridge.soap.string.StringMessageBuilder;
import br.ufsc.bridge.soap.xpath.XPathFactoryAssist;

/**
 * Exercita o caminho SAAJ (MessageFactory/SOAPFactory), que não é coberto pelos outros testes.
 * Até o Java 8 a implementação SAAJ vinha embutida na JDK; a partir do Java 11 ela depende do
 * saaj-impl declarado no pom, e a falha só aparece em runtime. Sem este teste o build fica verde
 * mesmo com a lib incapaz de montar um envelope.
 */
public class SoapMessageBuilderTest {

	private static final String WSSE = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

	private static final String BODY = "<t:Ping xmlns:t=\"urn:teste\"><t:valor>42</t:valor></t:Ping>";

	@XmlRootElement(name = "Ping", namespace = "urn:teste")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Ping {
		@XmlElement(namespace = "urn:teste")
		private String valor = "42";
	}

	private XPathFactoryAssist envelope(byte[] message) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(message));
		return new XPathFactoryAssist(document);
	}

	@Test
	public void stringBuilderComCredencialGeraUsernameToken() throws Exception {
		byte[] message = new StringMessageBuilder(new SoapCredential("usuario", "senha")).createMessage(BODY);

		XPathFactoryAssist x = this.envelope(message);
		Assert.assertEquals(WSSE, x.getString("namespace-uri(//*[local-name()='Security'])"));
		Assert.assertEquals("usuario", x.getString("//*[local-name()='Username']"));
		Assert.assertEquals("senha", x.getString("//*[local-name()='Password']"));
		Assert.assertEquals("UsernameToken-2", x.getString("//*[local-name()='UsernameToken']/@*[local-name()='Id']"));
		Assert.assertEquals("42", x.getString("//*[local-name()='Body']//*[local-name()='valor']"));
	}

	@Test
	public void stringBuilderSemCredencialNaoGeraSecurity() throws Exception {
		byte[] message = new StringMessageBuilder(null).createMessage(BODY);

		XPathFactoryAssist x = this.envelope(message);
		Assert.assertEquals(Long.valueOf(0), x.count("//*[local-name()='Security']"));
		Assert.assertEquals("42", x.getString("//*[local-name()='Body']//*[local-name()='valor']"));
	}

	@Test
	public void jaxbBuilderMarshallaOBody() throws Exception {
		byte[] message = new JAXBMessageBuilder<Ping>(new SoapCredential("usuario", "senha")).createMessage(new Ping());

		XPathFactoryAssist x = this.envelope(message);
		Assert.assertEquals("usuario", x.getString("//*[local-name()='Username']"));
		Assert.assertEquals("urn:teste", x.getString("namespace-uri(//*[local-name()='Body']/*)"));
		Assert.assertEquals("42", x.getString("//*[local-name()='Body']//*[local-name()='valor']"));
	}
}
