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
package de.tudarmstadt.ukp.csniper.webapp.page.welcome;

import org.apache.wicket.Component;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.markup.html.WebMarkupContainer;

import de.tudarmstadt.ukp.csniper.webapp.page.ApplicationPageBase;

/**
 * Evaluation Page
 */
public class WelcomePage
	extends ApplicationPageBase
{
	private static final long serialVersionUID = -530084892002620197L;

	public WelcomePage()
	{
		super();

		WebMarkupContainer users = new WebMarkupContainer("manageUsersLi");
		MetaDataRoleAuthorizationStrategy.authorize(users, Component.RENDER, "ROLE_ADMIN");
		add(users);

		WebMarkupContainer settings = new WebMarkupContainer("settingsLi");
		MetaDataRoleAuthorizationStrategy.authorize(settings, Component.RENDER, "ROLE_ADMIN");
		add(settings);
	}
}
