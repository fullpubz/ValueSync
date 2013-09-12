package com.atosworldline.jira.plugins;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.issue.IssueService.IssueResult;
import com.atlassian.jira.bc.issue.IssueService.TransitionValidationResult;
import com.atlassian.jira.bc.issue.IssueService.UpdateValidationResult;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.atosworldline.jira.plugins.valuesync.component.configuration.Configuration;
import com.opensymphony.util.TextUtils;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.StepDescriptor;

public class Utils {
	private Logger log = Logger.getLogger(Utils.class);

	public Utils() {
	}

	public void CompareValues(MutableIssue issue, Configuration configuration) {
		String field = configuration.getField();
		String valueToCompare = configuration.getValueToCompare();
		String operator = configuration.getOperator();
		Boolean compareOK = false;
		
		// On récupère le type du champ
		Method method = getMutableIssueMethodByString("get", getMethodName(field));
		Object obj = getFieldValue(issue, method);
		
		try {
			if (obj instanceof Timestamp && getTimestampValue(valueToCompare) != null) {
				if (CompareWithOperator(operator, (Timestamp) obj, getTimestampValue(valueToCompare))) {
					compareOK = true;
				}
			} else if (obj instanceof String) {
				if (CompareWithOperator(operator, (String) obj, valueToCompare)) {
					compareOK = true;
				}
			}
		} catch(Exception e) {
			log.warn("[CompareValues] Exception : " + e.getMessage());
		}
		
		if (compareOK) {
			Method methodActionToDo = getUtilsMethodByString(configuration.getActionToDo());	
			try {
				methodActionToDo.invoke(this, issue, configuration.getFieldToUpdate(), configuration.getUpdatedFieldValue());
			} catch (Exception e) {
				log.warn("[CompareValues] Exception : " + e.getMessage());
			}
		}
		
	}
	
	/*
	 * Compare 2 Timestamp
	 */
	public Timestamp getTimestampValue(String valueToCompare) {
		// on regarde si on a mis un mot clé dans l'input, sinon on cast juste la date en timestamp
		Timestamp timestampValueToCompare = null;
		if (valueToCompare.equals("now") || valueToCompare.equals("NOW")) {
			Date date = new Date();
			timestampValueToCompare = new Timestamp(date.getTime());
		} else {
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd");
				Date parsedDate = dateFormat.parse(valueToCompare);
				timestampValueToCompare = new Timestamp(
						parsedDate.getTime());
			} catch (Exception e) {
				log.warn("[executeConfiguration] La valeur saisie dans le champ Valeur à comparer n'est pas un Timestamp");
			}
		}
		return timestampValueToCompare;
	}

	/*
	 * Méthode pour comparer 2 valeurs (ici 2 Timestamp)
	 * A surcharger en fonction des besoins
	 */
	public Boolean CompareWithOperator(String operator, Timestamp field,
			Timestamp valueToCompare) {
		if (operator.equals("<")) {
			return field.before(valueToCompare);
		} else if (operator.equals(">")) {
			return field.after(valueToCompare);
		} else if (operator.equals("=")) {
			return field.equals(valueToCompare);
		} else {
			return false;
		}
	}
	
	public Boolean CompareWithOperator(String operator, String field,
			String valueToCompare) {
		log.warn("[CompareWithOperator]" + field + valueToCompare + field.equals(valueToCompare));
		if (operator.equals("=") || operator.contains("=")) {
			return field.equals(valueToCompare);
		} else if (operator.equals("!=")) {
			return !field.equals(valueToCompare);
		} else {
			return false;
		}
	}
	
	public void ModifyFieldValue(MutableIssue issue, String field,
			String value) {
			Method method = getMutableIssueMethodByString("set", getMethodName(field), String.class);
		
		if (method != null) {
			try {
				method.invoke(issue, value);
				issue.store();
			} catch (Exception e) {
				log.warn("[ModifyFieldValue] Exception -> " + e.getMessage());
			}
		}
	}
	
	/*
	 * Pour modifier la valeur du status (état) de l'issue
	 */
	public void ModifyStatusValue(MutableIssue issue, String field,
			String value) {	
		IssueManager issueManager = ComponentAccessor.getIssueManager();
		User user = getUser("admin");
		
		WorkflowManager workflowManager = (WorkflowManager) ComponentAccessor
				.getOSGiComponentInstanceOfType(WorkflowManager.class);
		JiraWorkflow workflow = workflowManager.getWorkflow(issue);
		List<Status> statuss = workflow.getLinkedStatusObjects();
		
		for (ActionDescriptor actionDescriptor : workflow.getAllActions()) {
			if (actionDescriptor.getName().equals(value)) {
				try {
					boolean wasIndexing = ImportUtils.isIndexIssues();
					ImportUtils.setIndexIssues( true );
					 
					WorkflowTransitionUtil workflowTransitionUtil = ( WorkflowTransitionUtil ) JiraUtils.loadComponent( WorkflowTransitionUtilImpl.class );
					workflowTransitionUtil.setIssue( issue );
					workflowTransitionUtil.setUsername("admin");
					workflowTransitionUtil.setAction(actionDescriptor.getId());
					
					workflowTransitionUtil.validate();
					workflowTransitionUtil.progress();
					 
					ImportUtils.setIndexIssues( wasIndexing );
					
				} catch (Exception e) {
					log.warn("[ModifyStatusValue] Exception -> " + e.getMessage());
				}
				break;
			}
		}
	}
	
	/*
	 * Pour récupérer une méthode de la classe MutableIssue (Jira) à partir de son nom (pour les getter)
	 */
	public Method getMutableIssueMethodByString(String prefixe, String methodName) {
		try {
			Method method = MutableIssue.class.getMethod(prefixe + methodName);
			return method;
		} catch (NoSuchMethodException e) {
			log.warn("[getProjectMethodByString] Pas de mÃ©thode '" + prefixe + methodName
					+ "' dans l'objet MutableIssue");
			return null;
		}
	}
	
	/*
	 * Pour récupérer une méthode de la classe MutableIssue (Jira) à partir de son nom (pour les setter)
	 * ici, surcharge avec le parametre Class, qui représente la classe du paramètre attendu par la méthode
	 */
	public Method getMutableIssueMethodByString(String prefixe, String methodName, Class c) {
		try {
			Method method = MutableIssue.class.getMethod(prefixe + methodName, c);
			return method;
		} catch (NoSuchMethodException e) {
			log.warn("[getProjectMethodByString] Pas de mÃ©thode '" + prefixe + methodName
					+ "' dans l'objet MutableIssue");
			return null;
		}
	}
	
	/*
	 * Pour récupérer une méthode de la classe Utils à partir de son nom
	 */
	public Method getUtilsMethodByString(String methodName) {
		try {
			Method method = Utils.class.getMethod(methodName, MutableIssue.class, String.class, String.class);
			return method;
		} catch (NoSuchMethodException e) {
			log.warn("[getUtilsMethodByString] Pas de mÃ©thode '" + methodName
					+ "' dans l'objet Utils");
			return null;
		}
	}
	
	/*
	 * Pour récupérer la valeur d'un champ à partir de l'issue et de la methode pour acceder à la valeur du champ
	 */
	public Object getFieldValue(MutableIssue issue, Method method) {
		try {
			return method.invoke(issue);
		} catch (Exception e) {
			log.warn("[getFieldValue] Impossible de rÃ©cupÃ©rer la valeur du champ Ã  travers la methode '"
					+ method);
			return null;
		}
	}

	public Project getProject(String id) {
		try {
			ProjectManager projectManager = (ProjectManager) ComponentAccessor
					.getOSGiComponentInstanceOfType(ProjectManager.class);
			long projectId = Long.parseLong(id);
			Project projet = projectManager.getProjectObj(projectId);
			return projet;
		} catch (Exception e) {
			log.warn("[getProjet] Impossible de trouver le projet ayant pour id : "
					+ id);
			return null;
		}
	}

	public String getProjectName(String id) {
		if (getProject(id) != null) {
			Project project = getProject(id);
			return project.getName();
		} else {
			return "";
		}
	}

	public User getUser(String name) {
		UserManager userManager = ComponentAccessor
				.getOSGiComponentInstanceOfType(UserManager.class);
		try {
			//User user = null;
			User user = userManager.getUser(name);
			return user;
		} catch (Exception e) {
			log.warn("[getUser] Le user avec le nom " + name + " n'existe pas");
			return null;
		}
	}
	
	/*
	 * Envlève les espaces et / dans le nom du champ
	 */
	public String getMethodName(String fieldName) {
		return fieldName.replace(" ", "").replace("/", "");
	}
}
