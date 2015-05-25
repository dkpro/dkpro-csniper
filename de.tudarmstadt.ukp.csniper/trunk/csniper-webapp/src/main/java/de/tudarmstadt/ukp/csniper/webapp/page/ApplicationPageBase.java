/*******************************************************************************
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.csniper.webapp.page;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.WebApplicationContextUtils;

import de.tudarmstadt.ukp.csniper.webapp.search.cqp.CqpEngine;
import de.tudarmstadt.ukp.csniper.webapp.search.cqp.CqpMacro;
import de.tudarmstadt.ukp.csniper.webapp.security.page.LogInOutPanel;
import de.tudarmstadt.ukp.csniper.webapp.support.task.TaskService;
import de.tudarmstadt.ukp.csniper.webapp.support.wicket.ExpandableList;

public abstract class ApplicationPageBase
	extends WebPage
{
	private final static Log LOG = LogFactory.getLog(ApplicationPageBase.class);

	private static final long serialVersionUID = -1690130604031181803L;

	private LogInOutPanel logInOutPanel;
	private InfoPanel helpPanel;
	private InfoPanel macroPanel;
	private AjaxLink<Void> openHelp;
	private AjaxLink<Void> openMacros;
	private FeedbackPanel feedbackPanel;
	private Label versionLabel;

	protected ApplicationPageBase()
	{
		commonInit();
	}
	
	protected ApplicationPageBase(final PageParameters parameters)
	{
		super(parameters);
		commonInit();
	}

	@SuppressWarnings({ "serial" })
	private void commonInit()
	{
		getSession().setLocale(Locale.ENGLISH);
		
		logInOutPanel = new LogInOutPanel("logInOutPanel");
		helpPanel = new InfoPanel("helpPanel", "page.help");
		macroPanel = new InfoPanel("macroPanel", "page.macros")
		{
			@Override
			protected void initialize()
			{
				super.initialize();
				Map<String, String> macros = new HashMap<String, String>();
				for (CqpMacro macro : CqpEngine.getMacros()) {
					macros.put(macro.getName(), macro.getBodyAsHtml());
				}
				infoPanel.replaceWith(new ExpandableList("infoPanel", macros));
			}
		};
		openHelp = new AjaxLink<Void>("openHelp")
		{
			@Override
			public void onClick(AjaxRequestTarget aTarget)
			{
				helpPanel.setVisible(!helpPanel.isVisible());
				aTarget.add(helpPanel);
			}
		};
		openMacros = new AjaxLink<Void>("openMacros")
		{
			@Override
			public void onClick(AjaxRequestTarget aTarget)
			{
				macroPanel.setVisible(!macroPanel.isVisible());
				aTarget.add(macroPanel);
			}
		};
		feedbackPanel = new FeedbackPanel("feedbackPanel");
		feedbackPanel.setOutputMarkupId(true);
		feedbackPanel.setFilter(new IFeedbackMessageFilter()
		{
			@Override
			public boolean accept(FeedbackMessage aMessage)
			{
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				String username = auth != null ? auth.getName() : "UNKNOWN";
				if (aMessage.isFatal()) {
					LOG.fatal(username + ": " + aMessage.getMessage());
				}
				else if (aMessage.isError()) {
					LOG.error(username + ": " + aMessage.getMessage());
				}
				else if (aMessage.isWarning()) {
					LOG.warn(username + ": " + aMessage.getMessage());
				}
				else if (aMessage.isInfo()) {
					LOG.info(username + ": " + aMessage.getMessage());
				}
				else if (aMessage.isDebug()) {
					LOG.debug(username + ": " + aMessage.getMessage());
				}
				return true;
			}
		});
		
		Properties props = getVersionProperties();
		versionLabel = new Label("version", props.getProperty("version") + " ("
				+ props.getProperty("timestamp") + ")");

		add(openHelp);
		add(openMacros);
		add(helpPanel);
		add(macroPanel);
		add(logInOutPanel);
		add(feedbackPanel);
		add(versionLabel);
	}

	@Override
	protected void onConfigure()
	{
		super.onConfigure();
//		logInOutPanel.setVisible(AuthenticatedWebSession.get().isSignedIn());
		openHelp.setVisible(!StringUtils.isBlank(getString("page.help", null, "")));
		openMacros.setVisible(!StringUtils.isBlank(getString("page.macros", null, "")));
	}
	
	public FeedbackPanel getFeedbackPanel()
	{
		return feedbackPanel;
	}
	
	public Properties getVersionProperties()
	{
		try {
			return PropertiesLoaderUtils.loadAllProperties("/META-INF/version.properties");
		}
		catch (IOException e) {
			LOG.error("Unable to load version information", e);
			return new Properties();
		}
	}
	
	public TaskService getTaskService()
	{
		return (TaskService) WebApplicationContextUtils.getRequiredWebApplicationContext(
				((WebApplication) getApplication()).getServletContext()).getBean("taskService");
	}
}
