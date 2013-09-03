package com.atosworldline.jira.plugins.valuesync.component.configuration;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atosworldline.jira.plugins.valuesync.component.Utils;

public class ConfigurationManager implements ConfigurationManagerInterface {

	public static Log log = LogFactory.getLog(ConfigurationManager.class);
	private final PluginSettingsFactory pluginSettingsFactory;

	public List<Configuration> configurations;

	public ConfigurationManager() {
		super();
		this.pluginSettingsFactory = (PluginSettingsFactory) ComponentAccessor
				.getOSGiComponentInstanceOfType(PluginSettingsFactory.class);
		loadConfig();
	}

	private void loadConfig() {
		PluginSettings pluginSettings = pluginSettingsFactory
				.createGlobalSettings();
		String configs = (String) pluginSettings
				.get(ConfigurationConstants.PLUGIN_STORAGE_KEY);
		configurations = buildConfigurations(configs);
	}

	private List<Configuration> buildConfigurations(String configs) {
		Document doc = Utils.stringToDoc(configs);
		configurations = docToConfig(doc);
		log.debug("[buildConfigurations] Exiting buildConfigurations with a list of : "
				+ configurations.size() + " Configuration");
		return configurations;
	}

	private List<Configuration> docToConfig(Document doc) {
		List<Configuration> listConfigurations = new ArrayList<Configuration>();
		if (doc != null) {
			try {
				Element element = doc.getDocumentElement();
				NodeList configurations = element
						.getElementsByTagName("configuration");
				log.debug("[docToConfig] configurations nbElement = : "
						+ configurations.getLength());
				for (int i = 0; i < configurations.getLength(); i++) {
					Node configuration = configurations.item(i);
					NodeList params = configuration.getChildNodes();
					String project = "", field = "", operator = "", valueToCompare = "", fieldToUpdate = "", updatedFieldValue = "", actionToDo = "";
					log.debug("[docToConfig] params nbElement = : "
							+ params.getLength());
					for (int j = 0; j < params.getLength(); j++) {
						Node param = params.item(j);
						if ("project".equals(param.getNodeName()))
							project = param.getFirstChild().getNodeValue();
						if ("field".equals(param.getNodeName()))
							field = param.getFirstChild().getNodeValue();
						if ("operator".equals(param.getNodeName()))
							operator = param.getFirstChild().getNodeValue();
						if ("valuetocompare".equals(param.getNodeName()))
							valueToCompare = param.getFirstChild()
									.getNodeValue();
						if ("fieldtoupdate".equals(param.getNodeName()))
							fieldToUpdate = param.getFirstChild()
									.getNodeValue();
						if ("updatedfieldvalue".equals(param.getNodeName()))
							updatedFieldValue = param.getFirstChild()
									.getNodeValue();
						if ("actiontodo".equals(param.getNodeName()))
							actionToDo = param.getFirstChild().getNodeValue();
					}
					listConfigurations.add(new Configuration(project, field,
							operator, valueToCompare, fieldToUpdate,
							updatedFieldValue, actionToDo));
				}
			} catch (Exception ex) {
				log.warn(
						"[docToConfig] An error occured while building Configuration for Doc : "
								+ ex.getMessage(), ex);
			}
		} else
			log.warn("[docToConfig] Cannot build the List<Configuration> : XML Document is null");
		return listConfigurations;
	}

	/**
	 * Persist a Config to the database
	 */
	public boolean saveConfig() {
		log.info("[saveConfig] Saving configuration as : " + this.toString());

		try {
			PluginSettings pluginSettings = pluginSettingsFactory
					.createGlobalSettings();
			pluginSettings.put(ConfigurationConstants.PLUGIN_STORAGE_KEY,
					Utils.docToString(configToDoc(configurations)));
		} catch (Exception e) {
			log.warn("[saveConfig] pluginSettings : " + e.getMessage());
		}
		log.debug("[saveConfig] Updated Configuration for ValueSync plugin saved");
		return true;
	}

	private Document configToDoc(List<Configuration> configurations) {
		log.debug("[configToDoc] Entering configToDoc(List<Configuration>)");
		if (configurations == null) {
			log.debug("[configToDoc] configurations list is null");
			log.debug("[configToDoc] Exiting configToDoc with a result of : null");
			return null;
		}
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = db.newDocument();
			Element rootConfigurations = doc.createElement("configurations");
			doc.appendChild(rootConfigurations);
			log.debug("[configToDoc] Converting " + configurations.size()
					+ " Configuration to XML");
			for (int i = 0; i < configurations.size(); i++) {
				Configuration configuration = configurations.get(i);
				if (configuration != null) {
					Element rootConfiguration = doc
							.createElement("configuration");
					rootConfigurations.appendChild(rootConfiguration);
					// project
					Element project = doc.createElement("project");
					project.setTextContent(configuration.getProject());
					rootConfiguration.appendChild(project);
					// field
					Element field = doc.createElement("field");
					field.setTextContent(configuration.getField());
					rootConfiguration.appendChild(field);
					// operator
					Element operator = doc.createElement("operator");
					operator.setTextContent(configuration.getOperator());
					rootConfiguration.appendChild(operator);
					// valuetocompare
					Element valueToCompare = doc
							.createElement("valuetocompare");
					valueToCompare.setTextContent(configuration
							.getValueToCompare());
					rootConfiguration.appendChild(valueToCompare);
					// fieldtoupdate
					Element fieldToUpdate = doc.createElement("fieldtoupdate");
					fieldToUpdate.setTextContent(configuration
							.getFieldToUpdate());
					rootConfiguration.appendChild(fieldToUpdate);
					// updatedfieldvalue
					Element updatedFieldValue = doc
							.createElement("updatedfieldvalue");
					updatedFieldValue.setTextContent(configuration
							.getUpdatedFieldValue());
					rootConfiguration.appendChild(updatedFieldValue);
					// actiontodo
					Element actionToDo = doc.createElement("actiontodo");
					actionToDo.setTextContent(configuration.getActionToDo());
					rootConfiguration.appendChild(actionToDo);
				}
			}
			log.debug("[configToDoc] Exiting configToDoc");
			return doc;
		} catch (ParserConfigurationException pce) {
			log.error(
					"[configToDoc] An error occured while creating XML Document from List<Configuration> : "
							+ pce.getMessage(), pce);
			log.debug("[configToDoc] Exiting configToDoc with a result of : null");
			return null;
		}
	}

	public List<Configuration> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(List<Configuration> configurations) {
		this.configurations = configurations;
	}

}
