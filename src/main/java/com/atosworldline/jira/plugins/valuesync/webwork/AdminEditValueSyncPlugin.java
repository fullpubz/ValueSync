package com.atosworldline.jira.plugins.valuesync.webwork;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atosworldline.jira.plugins.valuesync.component.configuration.Configuration;
import com.atosworldline.jira.plugins.valuesync.component.configuration.ConfigurationManager;
import com.atosworldline.jira.plugins.valuesync.component.configuration.ConfigurationManagerInterface;

/**
 * Action r�alis�e pour afficher la page d'�dition de la configuration du plugin
 * 
 * @author a187716
 * 
 */
public class AdminEditValueSyncPlugin extends JiraWebActionSupport {

	private static final long serialVersionUID = 1L;

	// There is already a log variable in JiraActionSupport, so there's
	// no need to define one here as well. To see log messages for this
	// class, use the fully-qualified class name in log4j.properties,
	// e.g. com.consultingtoolsmiths.jira.samples.webwork.ActionAlpha
	/**
	 * Authentication context.
	 */
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
	public AdminEditValueSyncPlugin(
			JiraAuthenticationContext authenticationContext,
			PermissionManager permissionMng, EventTypeManager eventTypeManager,
			ProjectManager projectManager) {
		log.debug("[AdminEditValueSyncPlugin] Entering AdminEditValueSyncPlugin");
		this.authenticationContext = authenticationContext;
		this.permissionManager = permissionMng;
		this.eventTypeManager = eventTypeManager;
		this.projectManager = projectManager;
		log.debug("[AdminEditValueSyncPlugin] Exiting AdminEditValueSyncPlugin");
	}

	/**
	 * Validation des param�tres de la requ�te. The HTML form may or may not
	 * have been submitted yet since doValidate() is always called when this
	 * Action's .jspa URL is invoked.
	 * 
	 * Si un message d'erreur existe et qu'aucune vue "input" n'existe, alors
	 * doExecute n'est pas appel� et la vue "error" d�finie dans
	 * atlassian-plugin.xml est utilis�e
	 * 
	 * Si un message d'erreur existe et qu'une vue "input" existe, alors cette
	 * vue "input" est utilis�e � la place de la vue "error"
	 * 
	 * L'URL affich�e dans le navigateur ne change pas pour une erreur,
	 * simplement la vue
	 * 
	 * Aucune exception n'est lanc�e, � la place, des erreurs et des messages
	 * d'erreurs sont cr��s.
	 */
	protected void doValidation() {
		log.debug("[doValidation] Entering doValidation");

		// Messages d'erreur
		if (authenticationContext.getLoggedInUser() == null) {
			addErrorMessage(ErrorConstants.NOT_LOGGED_IN);
		} else if (!permissionManager.hasPermission(Permissions.ADMINISTER,
				authenticationContext.getLoggedInUser())) {
			addErrorMessage(ErrorConstants.NO_PERMISSION);
		}

		// invalidInput() checks for error messages, and errors too.
		if (invalidInput()) {
			for (Iterator it = getErrorMessages().iterator(); it.hasNext();) {
				String msg = (String) it.next();
				log.debug("[doValidation] Error: " + msg);
			}

			for (Iterator it2 = getErrors().entrySet().iterator(); it2
					.hasNext();) {
				Map.Entry entry = (Map.Entry) it2.next();
				log.debug("[doValidation] Error: field=" + entry.getKey()
						+ ", error=" + entry.getValue());
			}
		}
		log.debug("[doValidation] Exiting doValidation");
	}

	/**
	 * This method is always called when this Action's .jspa URL is invoked if
	 * there were no errors in doValidation().
	 */
	protected String doExecute() {
		log.debug("[doExecute] Entering doExecute");
		doValidation();

		// Liste des valeurs en entr�e
		for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
			String n = (String) e.nextElement();
			String[] vals = request.getParameterValues(n);
			log.debug("[doExecute] name " + n + ": " + vals[0]);
			// TODO projectmap, eventmap, classemap, methodmap(num,valeur)
		}

		ConfigurationManager configurationManager = (ConfigurationManager) ComponentAccessor
				.getOSGiComponentInstanceOfType(ConfigurationManagerInterface.class);
		if (configurationManager == null) { // si aucune configuration
			log.debug("[doExecute] configurationManager null, on fait un new");
			configurationManager = new ConfigurationManager();
		}

		// Parametrage du module
		List<Configuration> configurations = new ArrayList<Configuration>();
		int size = Integer.parseInt(request.getParameter("conf-size"));
		log.debug("[doExecute] " + (size - 1) + " configurations to update");
		for (int i = 0; i < size; i++) {
			log.debug("[doExecute] checking Configuration " + i);
			if (!isToDelete(i)) {
				String project = request.getParameter("conf-project-" + i);
				String field = request.getParameter("conf-field-" + i);
				String operator = request.getParameter("conf-operator-" + i);
				String valueToCompare = request
						.getParameter("conf-valuetocompare-" + i);
				String fieldToUpdate = request
						.getParameter("conf-fieldtoupdate-" + i);
				String updatedFieldValue = request
						.getParameter("conf-updatedfieldvalue-" + i);
				String actionToDo = request
						.getParameter("conf-actiontodo-" + i);
				Configuration configuration = new Configuration(project, field,
						operator, valueToCompare, fieldToUpdate,
						updatedFieldValue, actionToDo);
				if (configuration.isValid()) {
					log.debug("[doExecute] adding Configuration "
							+ configuration);
					configurations.add(configuration);
				}
			} else
				log.debug("[doExecute] removing Configuration " + i);
		}
		String project = request.getParameter("conf-project");
		String field = request.getParameter("conf-field");
		String operator = request.getParameter("conf-operator");
		String valueToCompare = request.getParameter("conf-valuetocompare");
		String fieldToUpdate = request.getParameter("conf-fieldtoupdate");
		String updatedFieldValue = request
				.getParameter("conf-updatedfieldvalue");
		String actionToDo = request.getParameter("conf-actiontodo");
		Configuration configuration = new Configuration(project, field,
				operator, valueToCompare, fieldToUpdate, updatedFieldValue,
				actionToDo);
		if (configuration.isValid()) {
			log.debug("[doExecute] adding Configuration " + configuration);
			configurations.add(configuration);
		}
		try {
			configurationManager.setConfigurations(configurations);
		} catch (Exception e) {
			log.debug("[doExecute] configurationManager Exception = "
					+ e.getMessage());
		}

		// Sauvegarde en base ----
		configurationManager.saveConfig();

		log.debug("[doExecute] Exiting doExecute with a result of : " + SUCCESS);
		return SUCCESS;
	}

	/**
	 * Set up des valeurs par d�faut, si il y en a. Si vous n'avez pas de
	 * valeurs par d�faut, ceci est inutile
	 * 
	 * Si vous souhaitez avoir des valeurs par d�faut dans vos champs de
	 * formulaire au chargement, alors commencez par appeler cette m�thode (ou
	 * une autre avec un nom tel que doInit) pour initialiser les variables
	 * locales. Puis retournez "input" pour utiliser la vue formulaire input, et
	 * dans le formulaire, utilisez $(myfirstparameter) pour appeler
	 * getMyfirstparameter() pour charger les variables locales.
	 */
	public String doDefault() throws Exception {
		log.debug("[doDefault] Entering doDefault");
		doValidation();
		String result = super.doDefault();
		log.debug("[doDefault] Exiting doDefault with a result of : " + result);
		return result;
	}

	/**
	 * Redirection vers la page de connexion de JIRA
	 * 
	 * @param id
	 * @return
	 */
	public String doRedirect() {
		log.debug("[doRedirect] Entering doRedirect");
		String nextUrl = request.getContextPath()
				+ "/login.jsp?os_destination=/secure/EditPluginProperties!default.jspa";
		log.debug("[doRedirect] Exiting doRedirect with a result of nextUrl : "
				+ nextUrl);
		return super.forceRedirect(nextUrl);
	}

	// -----------------------------------------------------------------------------------

	private boolean isToDelete(int i) {
		log.debug("[isToDelete] Entering isToDelete");
		String[] toDeleteConfigs = request.getParameterValues("delete-select");
		if (toDeleteConfigs != null)
			if (toDeleteConfigs.length > 0)
				for (int j = 0; j < toDeleteConfigs.length; j++) {
					try {
						int config = Integer.parseInt(toDeleteConfigs[j]);
						if (i == config) {
							log.debug("[isToDelete] Exiting isToDelete with a result of : true");
							return true;
						}
					} catch (Exception e) {
						log.warn("[isToDelete] Unable to parse "
								+ toDeleteConfigs[j]);
					}
				}
		log.debug("[isToDelete] Exiting isToDelete with a result of : false");
		return false;
	}

	// -----------------------------------------------------------------------------------

	public List<Configuration> getConfigurations() {
		try {
			ConfigurationManager cm = new ConfigurationManager();
			return cm.getConfigurations();
		} catch (Exception e) {
			log.debug("[getConfigurations edit] Exception = " + e.getMessage());
			List<Configuration> c = new ArrayList<Configuration>();
			return c;
		}
	}

	public int countConfigurations() {
		return getConfigurations().size() + 1;
	}

	// pour en sélectionner un lors de l'ajout de règle
	public List<Project> getProjects() {
		return projectManager.getProjectObjects();
	}

	// pour afficher dans le tableau récapitulatif des règles
	public String getProjet(String id) {
		try {
			long project = Long.parseLong(id);
			Project projet = projectManager.getProjectObj(project);
			return projet.getName() + " [" + projet.getKey() + "] (" + id + ")";
		} catch (Exception e) {
			log.warn("[getProjet] Exception : " + e.getMessage());
		}
		return id;
	}

	public List<String> getIssueFieldsFromProjectId() {
		List<String> issueFields = new ArrayList<String>();
//		try {
//			IssueManager issueManager = ComponentAccessor.getOSGiComponentInstanceOfType(IssueManager.class);
//			List<Long> issueIds = (List<Long>) issueManager.getIssueIdsForProject(projectId);
//			List<Issue> issues = issueManager.getIssueObjects(issueIds);
//			
//			for(Issue issue : issues) {
				issueFields.add("test");
//			}
//			log.warn("[getIssueFieldsFromProjectId]");							
//		} catch (Exception e) {
//			log.warn("[executeConfiguration] " + e.getMessage());
//		}
		return issueFields;
	}
	
	public List<String> getFields() {
		List<String> fields = new ArrayList<String>();
		fields.add("DueDate");
		fields.add("StatusObject");
		fields.add("Description");
		return fields;
	}

	public List<String> getOperators() {
		List<String> operators = new ArrayList<String>();
		operators.add("<");
		operators.add(">");
		operators.add("=");
		operators.add("!=");
		return operators;
	}

	// rajoutter ici le nom des méthodes disponibles
	public List<String> getActionToDos() {
		List<String> actiontodos = new ArrayList<String>();
		actiontodos.add("ModifyFieldValue");
		actiontodos.add("ModifyStatusValue");
		return actiontodos;
	}
}