package it.com.atosworldline.jira.plugins;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.atlassian.jira.testkit.client.Backdoor;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.util.TestKitLocalEnvironmentData;
import com.atlassian.jira.testkit.client.util.TimeBombLicence;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;

@RunWith(AtlassianPluginsTestRunner.class)
public class IntegrationTest {
	
	@Test
	public void testMethod() {
		assertEquals("OK", test());		
	}
	
	public String test() {
		Backdoor testKit = new Backdoor(new TestKitLocalEnvironmentData());
		
		testKit.restoreBlankInstance(TimeBombLicence.LICENCE_FOR_TESTING);
		testKit.project().addProject("Test integration", "TI", "admin");
		String issueKey = testKit.issues().createIssue("TI", "Issue test").key();
		Issue i = testKit.issues().getIssue(issueKey);
		i.fields.duedate = "20-08-2013";
		return testKit.issues().getIssue("TI").fields.duedate;
		
	}
}
