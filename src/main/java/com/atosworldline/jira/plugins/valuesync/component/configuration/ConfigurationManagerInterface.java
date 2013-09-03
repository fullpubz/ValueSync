package com.atosworldline.jira.plugins.valuesync.component.configuration;

import java.util.List;

public interface ConfigurationManagerInterface {
	public boolean saveConfig();

	public List<Configuration> getConfigurations();

	public void setConfigurations(List<Configuration> configurations);
}
