package com.atosworldline.jira.plugins.valuesync.webwork;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.JiraWebActionSupport;
//import com.opensymphony.user.User;
import com.atosworldline.jira.plugins.valuesync.component.configuration.Configuration;
import com.atosworldline.jira.plugins.valuesync.component.configuration.ConfigurationManager;

/**
 * Action r�alis�e pour afficher la page de visualisation de la configuration du
 * plugin
 * 
 * @author a187716
 * 
 */
public class AdminViewValueSyncPlugin extends JiraWebActionSupport {

	// There is already a log variable in JiraActionSupport, so there's
	// no need to define one here as well. To see log messages for this
	// class, use the fully-qualified class name in log4j.properties,
	// e.g. com.consultingtoolsmiths.jira.samples.webwork.ActionAlpha

	private static final long serialVersionUID = 1059954267839733147L;
	private final JiraAuthenticationContext authenticationContext;
	private final PermissionManager permissionManager;
	private final EventTypeManager eventTypeManager;
	private final ProjectManager projectManager;

	/**
	 * Nothing is needed in this constructor, but many different JIRA components
	 * can be accessed using dependency injection with pico containers. See
	 * http:
	 * //confluence.atlassian.com/display/JIRA/Differences+between+Plugins1+
	 * and+Plugins2#DifferencesbetweenPlugins1andPlugins2-DependencyInjection
	 */
	public AdminViewValueSyncPlugin(
			JiraAuthenticationContext authenticationContext,
			PermissionManager permissionManager,
			EventTypeManager eventTypeManager, ProjectManager projectManager) {
		this.authenticationContext = authenticationContext;
		this.permissionManager = permissionManager;
		this.eventTypeManager = eventTypeManager;
		this.projectManager = projectManager;
	}

	protected void doValidation() {
		log.debug("Entering doValidation");
		// Messages d'erreur
		if (authenticationContext.getLoggedInUser() == null) {
			addErrorMessage(ErrorConstants.NOT_LOGGED_IN);
		} else if (!permissionManager.hasPermission(Permissions.ADMINISTER,
				authenticationContext.getLoggedInUser())) {
			addErrorMessage(ErrorConstants.NO_PERMISSION);
		}
		if (invalidInput()) {
			for (Iterator it = getErrorMessages().iterator(); it.hasNext();) {
				String msg = (String) it.next();
				log.debug("Error : " + msg);
			}

			for (Iterator it2 = getErrors().entrySet().iterator(); it2
					.hasNext();) {
				Map.Entry entry = (Map.Entry) it2.next();
				log.debug("Error : field=" + entry.getKey() + ", error="
						+ entry.getValue());
			}
		}
		log.debug("Exiting doValidation");
	}

	public String doDefault() throws Exception {
		log.debug("Entering doDefault");
		doValidation();
		String result = super.doDefault();
		log.debug("Exiting doDefault with a result of: " + result);
		return result;
	}

	/**
	 * Redirection vers la page de connexion de JIRA
	 * 
	 * @param id
	 * @return
	 */
	public String doRedirect() {
		log.debug("Entering doRedirect");
		String nextUrl = request.getContextPath()
				+ "/login.jsp?os_destination=/secure/ViewPluginProperties!default.jspa";
		log.debug("Exiting doRedirect with nextUrl : " + nextUrl);
		return super.forceRedirect(nextUrl);
	}

	public String getProjet(String id) {
		try {
			long project = Long.parseLong(id);
			Project projet = projectManager.getProjectObj(project);
			return projet.getName() + " [" + projet.getKey() + "] (" + id + ")";
		} catch (Exception e) {
			// Cas du "ALL"
			// log.error("AdminViewISMPLinkPlugin | getProjet : An error occured : "+
			// e.getMessage(), e);
		}
		return id;
	}

	public List<Configuration> getConfigurations() {
		try {
			ConfigurationManager cm = new ConfigurationManager();
			return cm.getConfigurations();
		} catch (Exception e) {
			log.warn("[getConfigurations view] Exception = " + e.getMessage());
			List<Configuration> c = new ArrayList<Configuration>();
			return c;
		}
	}
}