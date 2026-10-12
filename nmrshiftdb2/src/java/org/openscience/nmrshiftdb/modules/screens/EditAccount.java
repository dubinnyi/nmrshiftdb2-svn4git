package org.openscience.nmrshiftdb.modules.screens;

import org.apache.jetspeed.services.rundata.JetspeedRunData;
import org.apache.turbine.modules.screens.VelocityScreen;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.openscience.nmrshiftdb.util.UrlTool;


/**
 * The screen class for the personal page screen, corresponds with EditAccount.vm
 * 
 * @author shk3
 *
 */
public class EditAccount extends VelocityScreen {

	/* (non-Javadoc)
	 * @see org.apache.turbine.modules.screens.VelocityScreen#doBuildTemplate(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
	 */
	protected void doBuildTemplate(RunData data, Context context)
			throws Exception {
		if(new UrlTool().getTurbineUser((JetspeedRunData)data).isReviewer())
			context.put("tabheaders", new String[]{"Personal Data","My Private Submission","My Unreviewed Submission","Reviews Assigned to Me"});
		else
			context.put("tabheaders", new String[]{"Personal Data","My Private Submission","My Unreviewed Submission"});
		context.put("selectedtab", data.getParameters().getInt("tab",0));
	}
}
