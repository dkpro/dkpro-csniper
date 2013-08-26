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
package de.tudarmstadt.ukp.csniper.webapp;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.strategies.CompoundAuthorizationStrategy;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AnnotationsRoleAuthorizationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.time.Duration;
import org.odlabs.wiquery.ui.themes.IThemableApplication;
import org.odlabs.wiquery.ui.themes.WiQueryCoreThemeResourceReference;

import de.tudarmstadt.ukp.csniper.webapp.analysis.page.AnalysisPage;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.page.EvaluationPage;
import de.tudarmstadt.ukp.csniper.webapp.page.login.LoginPage;
import de.tudarmstadt.ukp.csniper.webapp.page.welcome.WelcomePage;
import de.tudarmstadt.ukp.csniper.webapp.project.page.AnnotationTypePage;
import de.tudarmstadt.ukp.csniper.webapp.project.page.ProjectPage;
import de.tudarmstadt.ukp.csniper.webapp.search.page.SearchPage;
import de.tudarmstadt.ukp.csniper.webapp.security.SpringAuthenticatedWebSession;
import de.tudarmstadt.ukp.csniper.webapp.security.page.ManageUsersPage;
import de.tudarmstadt.ukp.csniper.webapp.statistics.page.StatisticsPage;
import de.tudarmstadt.ukp.csniper.webapp.statistics.page.StatisticsPage2;

/**
 * Application object for your web application. If you want to run this application without
 * deploying, run the Start class.
 * 
 * @see de.tudarmstadt.ukp.csniper.webapp.Start#main(String[])
 */
public class WicketApplication
	extends AuthenticatedWebApplication
	implements IThemableApplication
{
	boolean isInitialized = false;

	private ResourceReference theme;
	
	public WicketApplication()
	{
		theme = new WiQueryCoreThemeResourceReference("redlion");
	}
	
	@Override
	public void init()
	{
		if (!isInitialized) {
			super.init();
			
			getRequestCycleSettings().setTimeout(Duration.minutes(10)); 
			
			getComponentInstantiationListeners().add(new SpringComponentInjector(this));

			CompoundAuthorizationStrategy autr = new CompoundAuthorizationStrategy();
			autr.add(new AnnotationsRoleAuthorizationStrategy(this));
			autr.add(new MetaDataRoleAuthorizationStrategy(this));
			getSecuritySettings().setAuthorizationStrategy(autr);

			mountPage("/login.html", getSignInPageClass());
			mountPage("/analysis.html", AnalysisPage.class);
			mountPage("/evaluation.html", EvaluationPage.class);
			mountPage("/project.html", ProjectPage.class);
			mountPage("/type.html", AnnotationTypePage.class);
			mountPage("/statistics.html", StatisticsPage.class);
			mountPage("/statistics2.html", StatisticsPage2.class);
//			mountPage("/export.html", ExportPage.class);
			mountPage("/search.html", SearchPage.class);
			mountPage("/welcome.html", getHomePage());
//			mountPage("/exportHtml.html", ExportHtmlPage.class);
			mountPage("/users.html", ManageUsersPage.class);

			isInitialized = true;
		}
	}

	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends Page> getHomePage()
	{
		return WelcomePage.class;
	}

	@Override
	public Class<? extends WebPage> getSignInPageClass()
	{
		return LoginPage.class;
	}

	@Override
	protected Class<? extends AuthenticatedWebSession> getWebSessionClass()
	{
		return SpringAuthenticatedWebSession.class;
	}
	
	public void setTheme(ResourceReference theme) {
		this.theme = theme;
	}
	
	@Override
	public ResourceReference getTheme(Session session) {
		return theme;
	}
}
