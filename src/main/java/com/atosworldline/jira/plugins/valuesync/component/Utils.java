package com.atosworldline.jira.plugins.valuesync.component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

public class Utils {

	public static Log log = LogFactory.getLog(Utils.class);

	/**
	 * Transform the XML String into a Document
	 * 
	 * @param s
	 * @return
	 */
	public static Document stringToDoc(String s) {
		log.debug("[stringToDoc] Entering stringToDoc");
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			if (s == null) {
				log.warn("[stringToDoc] Impossible to build XML Document from String '"
						+ s + "'");
				log.debug("[stringToDoc] Exiting stringToDoc with result of : null");
				return null;
			}

			InputStream is = new ByteArrayInputStream(s.getBytes("UTF-8"));

			Document doc = db.parse(is);

			// log.debug("[stringToDoc] document = " + docToString(doc));
			log.debug("[stringToDoc] Exiting stringToDoc");
			return doc;
		} catch (Exception e) {
			log.warn("[stringToDoc] Impossible to build XML Document from String '"
					+ s + "'");
			log.debug("[stringToDoc] Exiting stringToDoc with result of : null");
			return null;
		}
	}

	/**
	 * Transform the Document into a String
	 */
	public static String docToString(Document doc) {
		log.debug("[docToString] Entering docToString");
		try {
			DOMSource domSource = new DOMSource(doc);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			java.io.StringWriter sw = new java.io.StringWriter();
			StreamResult sr = new StreamResult(sw);
			transformer.transform(domSource, sr);
			String xml = sw.toString();
			log.debug("[docToString] Exiting docToString with a result of "
					+ xml);
			return xml;
		} catch (Exception ex) {
			log.warn("[docToString] Unable to create String from XML Document : "
					+ ex.getMessage());
			log.debug("[docToString] Exiting docToString with a result of : null");
			return null;
		}
	}

}
