package br.ufsc.bridge.soap.http.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * Classe responsavel por gerar os arquivos que simulam resquests http
 * Essa classe é necessária pelo fato que a especificação http diz que a tag CRLF (\r\n) faz a delimitação das seções.
 *
 * @author fernandobt8
 *
 */
public class SoapTestRequestWriter {

	public static void main(String[] args) throws UnsupportedEncodingException, IOException {
		writeMultipart();
		writeSoapXopStartHeader();
		writeSoapXopNoHeader();
		writeSoapBase64();
		writeSoapXop();
	}

	private static void writeSoapXop() throws IOException {
		String write = "--MIMEBoundaryurn_uuid_A62217736BFD4F63631516388993169\n" +
				"Content-Type: text/xml\n" +
				"Content-Transfer-Encoding: binary\n" +
				"Content-ID: <1.urn:uuid:A62217736BFD4F63631516388993171@apache.org>\r\n" +
				"\r\n" +
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><Encontro xmlns:oe=\"http://schemas.openehr.org/v1\" xmlns=\"http://schemas.oceanehr.com/templates\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://schemas.oceanehr.com/templates CN1.xsd\" template_id=\"Resumo de consulta ab_CN1_v1\"><name><value>Encontro</value></name><language><terminology_id><value>ISO_639-1</value></terminology_id><code_string>pt</code_string></language><territory><terminology_id><value>ISO_3166-1</value></terminology_id><code_string>BR</code_string></territory><category><value>event</value><defining_code><terminology_id><value>openehr</value></terminology_id><code_string>433</code_string></defining_code></category><composer xsi:type=\"oe:PARTY_SELF\"></composer><context><start_time><oe:value>2016-07-09T00:00:00.000-03:00</oe:value></start_time><setting><oe:value>other care</oe:value><oe:defining_code><oe:terminology_id><oe:value>openehr</oe:value></oe:terminology_id><oe:code_string>238</oe:code_string></oe:defining_code></setting></context><Caracterização_da_consulta><name><value>Caracterização da consulta</value></name><Admissão_do_paciente><name><value>Admissão do paciente</value></name><language><terminology_id><value>ISO_639-1</value></terminology_id><code_string>pt</code_string></language><encoding><terminology_id><value>IANA_character-sets</value></terminology_id><code_string>UTF-8</code_string></encoding><subject></subject><data><Tipo_de_atendimento><name><value>Tipo de atendimento</value></name><value><defining_code><terminology_id><value>local</value></terminology_id><code_string>at0.143</code_string></defining_code></value></Tipo_de_atendimento><Identificação_do_profissional><name><value>Identificação do profissional</value></name><CNS><name><value>CNS</value></name><value><oe:value>123456</oe:value></value></CNS><CBO><name><value>CBO</value></name><value><oe:value>654321</oe:value></value></CBO><É_o_responsável_pelo_atendimento_quest_><name><value>É o responsável pelo atendimento</value></name><value><oe:value>true</oe:value></value></É_o_responsável_pelo_atendimento_quest_></Identificação_do_profissional><Data_fslash_hora_da_admissão><name><value>Data/hora da admissão</value></name><value><oe:value>2016-07-09T00:00:00.000-03:00</oe:value></value></Data_fslash_hora_da_admissão><Turno_de_atendimento><name><value>Turno de atendimento</value></name><value><defining_code><terminology_id><value>local</value></terminology_id><code_string>at0.168</code_string></defining_code></value></Turno_de_atendimento></data></Admissão_do_paciente></Caracterização_da_consulta></Encontro>\r\n"
				+ "--MIMEBoundaryurn_uuid_A62217736BFD4F63631516388993169\n" +
				"Content-Type: application/xop+xml; charset=UTF-8; type=\"application/soap+xml\"\n" +
				"Content-Transfer-Encoding: binary\n" +
				"Content-ID: <0.urn:uuid:A62217736BFD4F63631516388993170@apache.org>\r\n" +
				"\r\n" +
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Header xmlns:wsa=\"http://www.w3.org/2005/08/addressing\"><wsa:Action>urn:ihe:iti:2007:RetrieveDocumentSetResponse</wsa:Action><wsa:RelatesTo>urn:uuid:a02ca8cd-86fa-4afc-a27c-616c183b2055</wsa:RelatesTo></soapenv:Header><soapenv:Body><xdsb:RetrieveDocumentSetResponse xmlns:xdsb=\"urn:ihe:iti:xds-b:2007\"><rs:RegistryResponse xmlns:rs=\"urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0\" status=\"urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success\"/><xdsb:DocumentResponse><xdsb:RepositoryUniqueId>1.3.6.1.4.1.21367.2010.1.2.1125</xdsb:RepositoryUniqueId><xdsb:DocumentUniqueId>1.42.20130403134532.123.1478642031821.45661498748839711</xdsb:DocumentUniqueId><xdsb:mimeType>text/xml</xdsb:mimeType><xdsb:Document><xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:1.urn:uuid:A62217736BFD4F63631516388993171@apache.org\"/></xdsb:Document></xdsb:DocumentResponse></xdsb:RetrieveDocumentSetResponse></soapenv:Body></soapenv:Envelope>\r\n"
				+ "--MIMEBoundaryurn_uuid_A62217736BFD4F63631516388993169--";

		FileOutputStream stream = new FileOutputStream(System.getProperty("user.dir") + "/src/test/resources/multipart/soapxop-doc-first/soap-xop.txt");
		stream.write(write.getBytes("UTF-8"));
		stream.close();
	}

	private static void writeSoapBase64() throws IOException {
		FileInputStream stream = new FileInputStream(System.getProperty("user.dir") + "/src/test/resources/multipart/docs/doc-part.xml");
		String docBase64 = Base64.encodeBase64String(IOUtils.toByteArray(stream));
		stream.close();

		stream = new FileInputStream(System.getProperty("user.dir") + "/src/test/resources/multipart/docs/soap-part.xml");
		String replace = IOUtils.toString(stream)
				.replace("<xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:1.urn:uuid:A62217736BFD4F63631516388993171@apache.org\"/>",
						docBase64);
		stream.close();

		FileOutputStream appSoap = new FileOutputStream(System.getProperty("user.dir") + "/src/test/resources/application-soap/application-soap.txt");
		appSoap.write(replace.getBytes("UTF-8"));

		appSoap.close();

	}

	private static void writeSoapXopStartHeader() throws FileNotFoundException, IOException, UnsupportedEncodingException {
		String write = "HTTP/1.1 200 OK\n" +
				"messageType: multipart/related\n" +
				"Content-Type: multipart/related; \n" +
				"  boundary=MIMEBoundaryurn_uuid_A62217736BFD4F63631516388993169; \n" +
				"  type=\"application/xop+xml\"; \n" +
				"  start=\"<0.urn:uuid:A62217736BFD4F63631516388993170@apache.org>\"; \n" +
				"  start-info=\"application/soap+xml\"; \n" +
				"  action=\"urn:ihe:iti:2007:RetrieveDocumentSetResponse\"\n" +
				"Date: Fri, 19 Jan 2018 19:08:55 GMT\n" +
				"Transfer-Encoding: chunked\n" +
				"Connection: Keep-Alive\r\n" +
				"\r\n" +
				"--MIMEBoundaryurn_uuid_A62217736BFD4F63631516388993169\n" +
				"Content-Type: application/xop+xml; charset=UTF-8; type=\"application/soap+xml\"\n" +
				"Content-Transfer-Encoding: binary\n" +
				"Content-ID: <0.urn:uuid:A62217736BFD4F63631516388993170@apache.org>\r\n" +
				"\r\n" +
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Header xmlns:wsa=\"http://www.w3.org/2005/08/addressing\"><wsa:Action>urn:ihe:iti:2007:RetrieveDocumentSetResponse</wsa:Action><wsa:RelatesTo>urn:uuid:a02ca8cd-86fa-4afc-a27c-616c183b2055</wsa:RelatesTo></soapenv:Header><soapenv:Body><xdsb:RetrieveDocumentSetResponse xmlns:xdsb=\"urn:ihe:iti:xds-b:2007\"><rs:RegistryResponse xmlns:rs=\"urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0\" status=\"urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success\"/><xdsb:DocumentResponse><xdsb:RepositoryUniqueId>1.3.6.1.4.1.21367.2010.1.2.1125</xdsb:RepositoryUniqueId><xdsb:DocumentUniqueId>1.42.20130403134532.123.1478642031821.45661498748839711</xdsb:DocumentUniqueId><xdsb:mimeType>text/xml</xdsb:mimeType><xdsb:Document><xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:1.urn:uuid:A62217736BFD4F63631516388993171@apache.org\"/></xdsb:Document></xdsb:DocumentResponse></xdsb:RetrieveDocumentSetResponse></soapenv:Body></soapenv:Envelope>\r\n"
				+ "--MIMEBoundaryurn_uuid_A62217736BFD4F63631516388993169\n" +
				"Content-Type: text/xml\n" +
				"Content-Transfer-Encoding: binary\n" +
				"Content-ID: <1.urn:uuid:A62217736BFD4F63631516388993171@apache.org>\r\n" +
				"\r\n" +
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><Encontro xmlns:oe=\"http://schemas.openehr.org/v1\" xmlns=\"http://schemas.oceanehr.com/templates\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://schemas.oceanehr.com/templates CN1.xsd\" template_id=\"Resumo de consulta ab_CN1_v1\"><name><value>Encontro</value></name><language><terminology_id><value>ISO_639-1</value></terminology_id><code_string>pt</code_string></language><territory><terminology_id><value>ISO_3166-1</value></terminology_id><code_string>BR</code_string></territory><category><value>event</value><defining_code><terminology_id><value>openehr</value></terminology_id><code_string>433</code_string></defining_code></category><composer xsi:type=\"oe:PARTY_SELF\"></composer><context><start_time><oe:value>2016-07-09T00:00:00.000-03:00</oe:value></start_time><setting><oe:value>other care</oe:value><oe:defining_code><oe:terminology_id><oe:value>openehr</oe:value></oe:terminology_id><oe:code_string>238</oe:code_string></oe:defining_code></setting></context><Caracterização_da_consulta><name><value>Caracterização da consulta</value></name><Admissão_do_paciente><name><value>Admissão do paciente</value></name><language><terminology_id><value>ISO_639-1</value></terminology_id><code_string>pt</code_string></language><encoding><terminology_id><value>IANA_character-sets</value></terminology_id><code_string>UTF-8</code_string></encoding><subject></subject><data><Tipo_de_atendimento><name><value>Tipo de atendimento</value></name><value><defining_code><terminology_id><value>local</value></terminology_id><code_string>at0.143</code_string></defining_code></value></Tipo_de_atendimento><Identificação_do_profissional><name><value>Identificação do profissional</value></name><CNS><name><value>CNS</value></name><value><oe:value>123456</oe:value></value></CNS><CBO><name><value>CBO</value></name><value><oe:value>654321</oe:value></value></CBO><É_o_responsável_pelo_atendimento_quest_><name><value>É o responsável pelo atendimento</value></name><value><oe:value>true</oe:value></value></É_o_responsável_pelo_atendimento_quest_></Identificação_do_profissional><Data_fslash_hora_da_admissão><name><value>Data/hora da admissão</value></name><value><oe:value>2016-07-09T00:00:00.000-03:00</oe:value></value></Data_fslash_hora_da_admissão><Turno_de_atendimento><name><value>Turno de atendimento</value></name><value><defining_code><terminology_id><value>local</value></terminology_id><code_string>at0.168</code_string></defining_code></value></Turno_de_atendimento></data></Admissão_do_paciente></Caracterização_da_consulta></Encontro>\r\n"
				+ "--MIMEBoundaryurn_uuid_A62217736BFD4F63631516388993169--";

		FileOutputStream stream = new FileOutputStream(System.getProperty("user.dir") + "/src/test/resources/multipart/soapxop-full/soap-xop.txt");
		stream.write(write.getBytes("UTF-8"));
		stream.close();
	}

	private static void writeSoapXopNoHeader() throws FileNotFoundException, IOException, UnsupportedEncodingException {
		String write = "--MIMEBoundaryurn_uuid_A62217736BFD4F63631516388993169\n" +
				"Content-Type: application/xop+xml; charset=UTF-8; type=\"application/soap+xml\"\n" +
				"Content-Transfer-Encoding: binary\n" +
				"Content-ID: <0.urn:uuid:A62217736BFD4F63631516388993170@apache.org>\r\n" +
				"\r\n" +
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Header xmlns:wsa=\"http://www.w3.org/2005/08/addressing\"><wsa:Action>urn:ihe:iti:2007:RetrieveDocumentSetResponse</wsa:Action><wsa:RelatesTo>urn:uuid:a02ca8cd-86fa-4afc-a27c-616c183b2055</wsa:RelatesTo></soapenv:Header><soapenv:Body><xdsb:RetrieveDocumentSetResponse xmlns:xdsb=\"urn:ihe:iti:xds-b:2007\"><rs:RegistryResponse xmlns:rs=\"urn:oasis:names:tc:ebxml-regrep:xsd:rs:3.0\" status=\"urn:oasis:names:tc:ebxml-regrep:ResponseStatusType:Success\"/><xdsb:DocumentResponse><xdsb:RepositoryUniqueId>1.3.6.1.4.1.21367.2010.1.2.1125</xdsb:RepositoryUniqueId><xdsb:DocumentUniqueId>1.42.20130403134532.123.1478642031821.45661498748839711</xdsb:DocumentUniqueId><xdsb:mimeType>text/xml</xdsb:mimeType><xdsb:Document><xop:Include xmlns:xop=\"http://www.w3.org/2004/08/xop/include\" href=\"cid:1.urn:uuid:A62217736BFD4F63631516388993171@apache.org\"/></xdsb:Document></xdsb:DocumentResponse></xdsb:RetrieveDocumentSetResponse></soapenv:Body></soapenv:Envelope>\r\n"
				+ "--MIMEBoundaryurn_uuid_A62217736BFD4F63631516388993169\n" +
				"Content-Type: text/xml\n" +
				"Content-Transfer-Encoding: binary\n" +
				"Content-ID: <1.urn:uuid:A62217736BFD4F63631516388993171@apache.org>\r\n" +
				"\r\n" +
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><Encontro xmlns:oe=\"http://schemas.openehr.org/v1\" xmlns=\"http://schemas.oceanehr.com/templates\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://schemas.oceanehr.com/templates CN1.xsd\" template_id=\"Resumo de consulta ab_CN1_v1\"><name><value>Encontro</value></name><language><terminology_id><value>ISO_639-1</value></terminology_id><code_string>pt</code_string></language><territory><terminology_id><value>ISO_3166-1</value></terminology_id><code_string>BR</code_string></territory><category><value>event</value><defining_code><terminology_id><value>openehr</value></terminology_id><code_string>433</code_string></defining_code></category><composer xsi:type=\"oe:PARTY_SELF\"></composer><context><start_time><oe:value>2016-07-09T00:00:00.000-03:00</oe:value></start_time><setting><oe:value>other care</oe:value><oe:defining_code><oe:terminology_id><oe:value>openehr</oe:value></oe:terminology_id><oe:code_string>238</oe:code_string></oe:defining_code></setting></context><Caracterização_da_consulta><name><value>Caracterização da consulta</value></name><Admissão_do_paciente><name><value>Admissão do paciente</value></name><language><terminology_id><value>ISO_639-1</value></terminology_id><code_string>pt</code_string></language><encoding><terminology_id><value>IANA_character-sets</value></terminology_id><code_string>UTF-8</code_string></encoding><subject></subject><data><Tipo_de_atendimento><name><value>Tipo de atendimento</value></name><value><defining_code><terminology_id><value>local</value></terminology_id><code_string>at0.143</code_string></defining_code></value></Tipo_de_atendimento><Identificação_do_profissional><name><value>Identificação do profissional</value></name><CNS><name><value>CNS</value></name><value><oe:value>123456</oe:value></value></CNS><CBO><name><value>CBO</value></name><value><oe:value>654321</oe:value></value></CBO><É_o_responsável_pelo_atendimento_quest_><name><value>É o responsável pelo atendimento</value></name><value><oe:value>true</oe:value></value></É_o_responsável_pelo_atendimento_quest_></Identificação_do_profissional><Data_fslash_hora_da_admissão><name><value>Data/hora da admissão</value></name><value><oe:value>2016-07-09T00:00:00.000-03:00</oe:value></value></Data_fslash_hora_da_admissão><Turno_de_atendimento><name><value>Turno de atendimento</value></name><value><defining_code><terminology_id><value>local</value></terminology_id><code_string>at0.168</code_string></defining_code></value></Turno_de_atendimento></data></Admissão_do_paciente></Caracterização_da_consulta></Encontro>\r\n"
				+ "--MIMEBoundaryurn_uuid_A62217736BFD4F63631516388993169--";

		FileOutputStream stream = new FileOutputStream(System.getProperty("user.dir") + "/src/test/resources/multipart/soapxop-noheader/soap-xop.txt");
		stream.write(write.getBytes("UTF-8"));
		stream.close();
	}

	public static void writeMultipart() throws UnsupportedEncodingException, IOException {
		String write = "HTTP/1.1 200 OK\n" +
				"messageType: multipart/related\n" +
				"Content-Type: multipart/related;boundary=MIME_boundary;type=\"application/xop+xml\";start=\"<1>\";start-info=\"application/soap+xml\";action=\"urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-bResponse\"\n"
				+
				"Date: Fri, 19 Jan 2018 18:55:51 GMT\n" +
				"Transfer-Encoding: chunked\n" +
				"Connection: Keep-Alive\r\n" +
				"\r\n" +
				"--MIME_boundary\n" +
				"Content-Type: application/xop+xml; charset=UTF-8; type=\"application/soap+xml\"\n" +
				"Content-Transfer-Encoding: binary\n" +
				"Content-ID: <0.urn:uuid:A62217736BFD4F63631516388208911@apache.org>\r\n" +
				"\r\n" +
				"teste\r\n" +
				"--MIME_boundary--";

		FileOutputStream stream = new FileOutputStream(System.getProperty("user.dir") + "/src/test/resources/multipart/simple/multipart.txt");
		stream.write(write.getBytes("UTF-8"));
		stream.close();
	}
}
