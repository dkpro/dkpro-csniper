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
package de.tudarmstadt.ukp.csniper.webapp.support.wicket;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.model.Model;

public class ThresholdLink
	extends Link<String>
{
	private static final long serialVersionUID = 1L;

	public ThresholdLink(String aId, int aWidth, int aHeight)
	{
		super(aId);

		PopupSettings ps = new PopupSettings(aId, PopupSettings.SCROLLBARS
				+ PopupSettings.LOCATION_BAR);
		ps.setWidth(aWidth);
		ps.setHeight(aHeight);
		setPopupSettings(ps);

		setBody(new Model<String>(
				"<img src=\"images/questionmark.png\" alt=\"Explanation\" style=\"vertical-align:bottom\" />"));
		setEscapeModelStrings(false);
	}

	public ThresholdLink(String aId)
	{
		this(aId, 600, 400);
	}

	@Override
	public void onClick()
	{
		setResponsePage(ThresholdHelpPage.class);
	}
}
