package com.atosworldline.jira.plugins.service;

import java.util.List;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.service.AbstractService;
import com.atosworldline.jira.plugins.Utils;
import com.atosworldline.jira.plugins.valuesync.component.configuration.Configuration;
import com.atosworldline.jira.plugins.valuesync.component.configuration.ConfigurationManager;

public class ValueSyncService extends AbstractService {

	Utils utils = new Utils();

	@Override
	public void run() {
		ConfigurationManager configurationManager = new ConfigurationManager();
		List<Configuration> configurations = configurationManager
				.getConfigurations();
		for (Configuration configuration : configurations) {
			executeConfiguration(configuration);
		}
	}

	@Override
	public ObjectConfiguration getObjectConfiguration()
			throws ObjectConfigurationException {
		return getObjectConfiguration("VALUESYNCSERVICE",
				"com/atosworldline/jira/plugins/services/ValueSyncService.xml",
				null);
	}

	public void executeConfiguration(Configuration configuration) {	
		Project project = utils.getProject(configuration.getProject());
		if (project != null) {
			IssueManager issueManager = ComponentAccessor
					.getOSGiComponentInstanceOfType(IssueManager.class);
			try {
				List<Long> issueIds = (List<Long>) issueManager
						.getIssueIdsForProject(project.getId());
				if (!issueIds.isEmpty()) {
					for (Long issueId : issueIds) {
						MutableIssue issue = issueManager.getIssueObject(issueId);
						utils.CompareValues(issue, configuration);
						log.warn("[executeConfiguration] Service ValueSync execute");							
					}
				}
			} catch (Exception e) {
				log.warn("[executeConfiguration] " + e.getMessage());
			}
		}
	}

}
