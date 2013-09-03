package ut.com.atosworldline.jira.plugins;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.MockComponentManager;
import com.atlassian.jira.testkit.client.Backdoor;
import com.atlassian.jira.testkit.client.util.TestKitLocalEnvironmentData;
import com.atlassian.jira.testkit.client.util.TimeBombLicence;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MockIssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ComponentManagerMocker;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.MockProjectManager;
import com.atlassian.jira.project.DefaultAssigneeException;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.project.ProjectManager;
//import com.atosworldline.jira.plugins.MyPluginComponent;
//import com.atosworldline.jira.plugins.MyPluginComponentImpl;

import static org.junit.Assert.assertEquals;

public class MyComponentUnitTest
{
	@Ignore
    @Test
    public void testMyName()
    {
        //MyPluginComponent component = new MyPluginComponentImpl(null);
        //assertEquals("names do not match!", "myComponent",component.getName());
    }
	
	@Ignore
	@Before
    public void test() {
//		Backdoor testKit = new Backdoor(new TestKitLocalEnvironmentData());
//		
//		testKit.restoreBlankInstance(TimeBombLicence.LICENCE_FOR_TESTING);
//		testKit.project().addProject("Test integration", "TI", "admin");
//		String issueKey = testKit.issues().createIssue("TI", "Issue test").key();
//		Issue i = testKit.issues().getIssue(issueKey);
//		i.fields.duedate = "20-08-2013";
//		return testKit.issues().getIssue("TI").fields.duedate;
	}
	
	@Ignore
    @Test
    public void testUTProject() {	
    	assertEquals("TEST UNITAIRE", getProject());
    }
    
	public String getProject() {
		try {				
			// exemple avec user
			User user = new MockUser("testunit");			
			MockUserManager mum = new MockUserManager();
			mum.addUser(user);
			
			// récupération des projets (créés par défaut)
			MockProjectManager mockProjectManager = MockProjectManager.createDefaultProjectManager();
			String concat = "";
			for (Project p : mockProjectManager.getProjectObjects()) {
				//concat += p.getId() + " | ";
			}
			
			//mockProjectManager.getProject(new Long(1));
			
			// création d'une issue
			MockIssueManager mockIssueManager = new MockIssueManager();
			MutableIssue mutableIssue = MockIssueFactory.createIssue((long) 1, "ISS", (long) 1);		
			mutableIssue.setSummary("test issue 1");
			Date date = new Date();
			mutableIssue.setDueDate(new Timestamp(date.getTime()));
			mockIssueManager.addIssue(mutableIssue);

			MutableIssue mi = mockIssueManager.getIssueObject((long) 1);
			//mockIssueManager.getIssueIdsForProject(projectId);
			//for (Long id : l) {
				//concat += mockIssueManager.getIssueObject(id).toString() + " | ";
			//}
			
			//return String.valueOf(projectManager.getProjectCount());
			if (mi == null)
				return "OK";
			else
				return mi.getDueDate().toString();
		} catch (Exception e) {
			return "Exception : " + e.getMessage();
		}
	}
   
}