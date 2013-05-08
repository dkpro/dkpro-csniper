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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class InfoPanel
	extends Panel
{
	private Label infoLabel;
	protected WebMarkupContainer infoPanel;
	private String labelKey;

	public InfoPanel(String id)
	{
		super(id);
		initialize();
	}

	public InfoPanel(String id, IModel<String> model)
	{
		super(id, model);
		initialize();
	}

	public InfoPanel(String id, String aLabelKey)
	{
		super(id);
		initialize();
		labelKey = aLabelKey;
	}

	protected void initialize()
	{
		infoLabel = new Label("infoLabel");
		infoLabel.setEscapeModelStrings(false);
		add(infoLabel);
		infoPanel = new WebMarkupContainer("infoPanel");
		add(infoPanel);
		add(new AjaxLink("hideInfo")
		{
			@Override
			public void onClick(AjaxRequestTarget aTarget)
			{
				InfoPanel.this.setVisible(false);
				aTarget.add(InfoPanel.this);
			}
		});
		setOutputMarkupPlaceholderTag(true);
		setVisible(false);
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();
		infoLabel.setDefaultModel(new Model<String>(getPage().getString(labelKey, null, "")));
	}
}
