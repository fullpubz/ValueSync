package com.atosworldline.jira.plugins;

import com.atlassian.sal.api.ApplicationProperties;

import org.apache.log4j.Logger;

public class MyPluginComponentImpl implements MyPluginComponent
{
    private final ApplicationProperties applicationProperties;
    private Logger log = Logger.getLogger(Utils.class);

    public MyPluginComponentImpl(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    public String getName()
    {
        if(null != applicationProperties)
        {
            return "myComponent:" + applicationProperties.getDisplayName();
        }
        
        return "myComponent";
    }
    
    public String CreateProjectTest() {
//    	Backdoor testKit = new Backdoor(new TestKitLocalEnvironmentData());
//		
//		testKit.restoreBlankInstance(TimeBombLicence.LICENCE_FOR_TESTING);
//		testKit.project().addProject("Test integration", "TI", "admin");
//		String issueKey = testKit.issues().createIssue("TI", "Issue test").key();
//		Issue i = testKit.issues().getIssue(issueKey);
//		i.fields.duedate = "20-08-2013";
//		return testKit.issues().getIssue("TI").fields.duedate;
    	return "";
    }
}