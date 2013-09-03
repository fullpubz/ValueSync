package com.atosworldline.jira.plugins.valuesync;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atosworldline.jira.plugins.valuesync.component.configuration.Configuration;

public class Utils {
	private Logger log = Logger.getLogger(Utils.class);

	public Utils() {
	}

	public void CompareValues(MutableIssue issue, Configuration configuration) {
		String field = configuration.getField();
		String valueToCompare = configuration.getValueToCompare();
		String operator = configuration.getOperator();
		
		// type TIMESTAMP (voir
		// https://developer.atlassian.com/static/javadoc/jira/5.1.8/reference/com/atlassian/jira/issue/Issue.html
		// pour connaitre les types retournÃ©s
		if (CompareTimestamp(issue, field, valueToCompare, operator)) {
			Method method = getUtilsMethodByString(configuration.getActionToDo());	
			try {
				method.invoke(this, issue, configuration.getFieldToUpdate(), configuration.getUpdatedFieldValue());
			} catch (Exception e) {
				log.warn("[CompareValues] Exception : " + e.getMessage());
			}
		}
		
	}
	
	/*
	 * Compare 2 Timestamp
	 */
	public Boolean CompareTimestamp(MutableIssue issue, String field,
			String valueToCompare, String operator) {
		// pour ajouter un champ, il suffit de rajouter '&&
		// configuration.getField() == nomDuChamp'
		if (field.equals("DueDate")) {
			Method method = getMutableIssueMethodByString("get", field);
			Timestamp value = (Timestamp) getFieldValue(issue, method);
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
					log.warn("[executeConfiguration] La valeur saisie dans le champ Valeur Ã  comparer n'est pas un Timestamp");
				}
			}
			if (timestampValueToCompare != null)
				return Compare(operator, value, timestampValueToCompare);
			else
				return false;
		}
		return false;
	}

	/*
	 * Méthode pour comparer 2 valeurs (ici 2 Timestamp)
	 * A surcharger en fonction des besoins
	 */
	public Boolean Compare(String operator, Timestamp field,
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
	
	/*
	 * Methode appelée par le service, elle teste le champ a odifier et appelle la méthode correspondant du type de donnée
	 * Si on souhaite rajouter des champ pris en charge par le plugin, c'est ici,
	 * il suffit de connaitre le type du champs (Status, Timestamp, String...) et d'ajouter le libellé du champ dans le if correspondant
	 * 
	 * Si le type n'est pas encore pris en charge, rajouter un if et écrire la méthode correspondante en se basant sur celles deja existantes
	 */
	public void ModifyFieldValue(MutableIssue issue, String field,
			String value) {
		Method method = null;
		
		// champs de type Status
		if (field.equals("StatusObject")) {
			ModifyStatusValue(issue, field, value);
			
		// champs de type String
		} else if (field.equals("Description")) {
			ModifyStringValue(issue, field, value);
		}
	}
	
	public void ModifyStringValue(MutableIssue issue, String field,
			String value) {
		Method method = getMutableIssueMethodByString("set", field, String.class);
		
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
		WorkflowManager workflowManager = (WorkflowManager) ComponentAccessor
				.getOSGiComponentInstanceOfType(WorkflowManager.class);
		JiraWorkflow workflow = workflowManager.getWorkflow(issue);
		
		List<Status> statuss = workflow.getLinkedStatusObjects();
		
		for (Status status : statuss) {
			if (status.getName().equals(value)) {
				try {
					issue.setStatusId(status.getId());
					issue.store();
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
}
