package com.atosworldline.jira.plugins.valuesync.webwork;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.ar.ArabicAnalyzer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.layout.field.FieldConfigurationScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.FieldScreenSchemeItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenScheme;
import com.atlassian.jira.issue.fields.screen.issuetype.IssueTypeScreenSchemeEntity;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;

public class EditPluginServlet extends HttpServlet {
	public void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException{
		
	    response.setContentType("text/xml");
	    response.setHeader("Cache-Control", "no-cache");
	    
		if (request.getParameter("projectId") != null) {
			// on cherche juste le champs
		    String valeur = request.getParameter("projectId");
		    // on cherche les champs correspondant à la méthode actionToDo
			if (request.getParameter("actionToDo") != null) {		
				String actionToDo = request.getParameter("actionToDo");	
				if (actionToDo.equals("ModifyFieldValue")) {
					response.getWriter().write("<fields>" + getFieldsFromProject(Long.parseLong(valeur)) + "</fields>");
				} else if (actionToDo.equals("ModifyStatusValue")) {
					response.getWriter().write("<fields><field>StatusObject</field></fields>");
				}
			} else {
				response.getWriter().write("<fields>" + getFieldsFromProject(Long.parseLong(valeur)) + "</fields>");
			}
		} else {
			// projects
		    response.getWriter().write("<projects>" + getProjects() + "</projects>");
		}
	}
	
	public String getProjects() {
		ProjectManager projectManager = ComponentAccessor.getOSGiComponentInstanceOfType(ProjectManager.class);
		List<Project> projects = projectManager.getProjectObjects();
		String projectsString = "";
		
		for (Project project : projects) {
			projectsString += "<project><id>" + project.getId() + "</id><name>" + project.getName() + "</name></project>";
		}
		
		return projectsString;
	}
	
	public String getFieldsFromProject(Long projectId) {
		String testFields = "";
		
		ProjectManager projectManager = ComponentAccessor.getOSGiComponentInstanceOfType(ProjectManager.class);
		Project project = projectManager.getProjectObj(projectId);
		
		FieldLayoutManager fieldLayoutManager = ComponentAccessor.getOSGiComponentInstanceOfType(FieldLayoutManager.class);
		IssueTypeScreenScheme issueTypeScreenScheme = ComponentAccessor.getIssueTypeScreenSchemeManager().getIssueTypeScreenScheme(project);
		FieldManager fieldManager = ComponentAccessor.getFieldManager();

		for (IssueTypeScreenSchemeEntity issueTypeScreenSchemeEntity : issueTypeScreenScheme.getEntities()) {
			FieldScreenScheme fieldScreenScheme = issueTypeScreenSchemeEntity.getFieldScreenScheme();
			for (FieldScreenSchemeItem fieldScreenSchemeItem : fieldScreenScheme.getFieldScreenSchemeItems()) {
				for (FieldScreenTab fieldScreenTab : fieldScreenSchemeItem.getFieldScreen().getTabs()) {
					for (FieldScreenLayoutItem fieldScreenLayoutItem : fieldScreenTab.getFieldScreenLayoutItems()) {
						testFields += "<field>" + fieldManager.getField(fieldScreenLayoutItem.getFieldId()).getName() + "</field>";
					}
				}
			}
		}
		return testFields;
	}
	
	public String getWorkflowStatus(Long projectId) {
		String statusString = "";
		ProjectManager projectManager = ComponentAccessor.getProjectManager();
		Project project = projectManager.getProjectObj(projectId);
		
		WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager();
		JiraWorkflow workflow = workflowManager.getWorkflow(projectId, "1");
		for (Status status : workflow.getLinkedStatusObjects()) {
			statusString += status.getName() + " ";
		}
		return statusString;
	}
	
	public List<String> getOperators() {
		List<String> operators = new ArrayList<String>();
		operators.add("<");
		operators.add(">");
		operators.add("=");
		return operators;
	}
}
