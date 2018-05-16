package br.ufsc.bridge.soap.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

public class JAXBContextWrapperTest {

	@Test
	public void shouldReturnTheSameInstance() throws JAXBException {
		JAXBContext instance1 = JAXBContextWrapper.newInstance(this.getClass());
		JAXBContext instance2 = JAXBContextWrapper.newInstance(this.getClass());

		Assert.assertSame(instance1, instance2);
	}

}
