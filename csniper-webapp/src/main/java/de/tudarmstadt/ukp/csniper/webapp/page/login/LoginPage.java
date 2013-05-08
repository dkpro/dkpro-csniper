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
package de.tudarmstadt.ukp.csniper.webapp.page.login;

import org.apache.commons.logging.LogFactory;
import org.apache.wicket.spring.injection.annot.SpringBean;

import de.tudarmstadt.ukp.csniper.webapp.page.ApplicationPageBase;
import de.tudarmstadt.ukp.csniper.webapp.security.LoginForm;
import de.tudarmstadt.ukp.csniper.webapp.security.dao.UserDao;
import de.tudarmstadt.ukp.csniper.webapp.security.model.Role;
import de.tudarmstadt.ukp.csniper.webapp.security.model.User;

public class LoginPage
	extends ApplicationPageBase
{
	private static final long serialVersionUID = 4556072152990919319L;

	private static final String ADMIN_DEFAULT_USERNAME = "admin";
	private static final String ADMIN_DEFAULT_PASSWORD = "admin";

	@SpringBean(name = "userRepository")
	private UserDao userRepository;

	public LoginPage()
	{
		if (userRepository.list().isEmpty()) {
			User admin = new User();
			admin.setUsername(ADMIN_DEFAULT_USERNAME);
			admin.setPassword(ADMIN_DEFAULT_PASSWORD);
			admin.setEnabled(true);
			admin.setRoles(Role.getRoles());
			userRepository.create(admin);

			String msg = "No user accounts have been found. An admin account has been created: "
					+ ADMIN_DEFAULT_USERNAME + "/" + ADMIN_DEFAULT_PASSWORD;
			info(msg);
			LogFactory.getLog(getClass()).info(msg);
		}

		add(new LoginForm("loginForm"));
	}
}
