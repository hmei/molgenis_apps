/* Date:        April 30, 2011
 * Template:	NewPluginModelGen.java.ftl
 * generator:   org.molgenis.generators.ui.NewPluginModelGen 3.3.3
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package org.molgenis.sandbox.plugins;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.framework.ui.SimpleScreenModel;
import org.molgenis.organization.Investigation;

/**
 * TestModel takes care of all state and it can have helper methods to query the database.
 * It should not contain layout or application logic which are solved in View and Controller.
 * @See org.molgenis.framework.ui.ScreenController for available services.
 */
public class TestModel extends SimpleScreenModel
{
	//a system veriable that is needed by tomcat
	private static final long serialVersionUID = 1L;
	
	//this string can be referenced from View template as ${model.helloWorld}
	public String helloWorld = "hello World";
	
	//another example, you can also use getInvestigations() and setInvestigations(...)
	public List<Investigation> investigations = new ArrayList<Investigation>();

	public TestModel(Test controller)
	{
		//each Model can access the controller to notify it when needed.
		super(controller);
	}
}
