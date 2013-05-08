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

import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.AbstractItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * This creates a list with expandable items, i.e. each list item consists of a caption and a hidden
 * body, which is displayed when the caption is clicked.
 * 
 * @author Erik-Lân Do Dinh
 */

public class ExpandableList
	extends Panel
{
	private static final long serialVersionUID = 1L;

	private RepeatingView expandableList;
	private Map<String, String> items;

	public ExpandableList(String aId)
	{
		super(aId);
		initialize();
	}

	public ExpandableList(String aId, IModel<?> aModel)
	{
		super(aId, aModel);
		initialize();
	}

	/**
	 * @param aId
	 * @param aList
	 *            A map caption->body, from which the list will be created.
	 */
	public ExpandableList(String aId, Map<String, String> aList)
	{
		super(aId);
		items = aList;
		initialize();
	}

	private void initialize()
	{
		expandableList = new RepeatingView("expandableList");
		AbstractItem item;
		for (Entry<String, String> entry : items.entrySet()) {
			item = new AbstractItem(expandableList.newChildId());

			final Label body = new Label("body", entry.getValue());
			body.setOutputMarkupPlaceholderTag(true).setVisible(false).setEscapeModelStrings(false);
			item.add(body);

			item.add(new AjaxLink<String>("caption", new Model<String>(entry.getKey()))
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
				{
					replaceComponentTagBody(markupStream, openTag, getModelObject());
				}

				@Override
				public void onClick(AjaxRequestTarget aTarget)
				{
					body.setVisible(!body.isVisible());
					aTarget.add(body);
				}
			}.add(new AttributeModifier("class", new Model<String>("clickableElement"))));
			expandableList.add(item);
		}
		add(expandableList);
	}
}
