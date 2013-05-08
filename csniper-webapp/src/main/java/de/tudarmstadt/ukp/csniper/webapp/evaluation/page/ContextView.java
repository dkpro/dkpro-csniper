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
package de.tudarmstadt.ukp.csniper.webapp.evaluation.page;

import java.io.IOException;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationItem;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.ItemContext;
import de.tudarmstadt.ukp.csniper.webapp.search.ContextProvider;

public class ContextView
	extends Panel
{
	private static final long serialVersionUID = -4541901391361133303L;
	private ContextProvider contextProvider;
	private EvaluationItem evaluationItem;
	private ItemContext context;
	private int extensionSize = 300;
	private int leftSize = extensionSize;
	private int rightSize = extensionSize;

	public ContextView(ContextProvider aContextProvider, EvaluationItem aEvaluationItem)
		throws IOException
	{
		super("contextView");

		contextProvider = aContextProvider;
		evaluationItem = aEvaluationItem;
		context = getContext(false, false);

		final Label left = new HtmlLabel("left", new PropertyModel<ItemContext>(context, "left"));
		left.setOutputMarkupId(true);
		add(left);

		add(new HtmlLabel("unitLeft", new PropertyModel<ItemContext>(context, "unitLeft")));

		add(new HtmlLabel("unitMatch", new PropertyModel<ItemContext>(context, "match")));

		add(new HtmlLabel("unitRight", new PropertyModel<ItemContext>(context, "unitRight")));

		final Label right = new HtmlLabel("right", new PropertyModel<ItemContext>(context, "right"));
		right.setOutputMarkupId(true);
		add(right);

		add(new AjaxLink<ItemContext>("moreLeft", new PropertyModel<ItemContext>(context,
				"moreLeft"))
		{
			private static final long serialVersionUID = 3185794699985733066L;

			{
				setVisible(context.getMoreLeft() > 0);
				setOutputMarkupId(true);
			}

			@Override
			public void onComponentTagBody(final MarkupStream markupStream,
					final ComponentTag openTag)
			{
				replaceComponentTagBody(markupStream, openTag, "("
						+ getDefaultModelObjectAsString() + " chars) <<< ...");
			}

			@Override
			public void onClick(AjaxRequestTarget aTarget)
			{
				// get more context
				try {
					context = getContext(true, false);
					left.setDefaultModel(new PropertyModel<ItemContext>(context, "left"));
					setModel(new PropertyModel<ItemContext>(context, "moreLeft"));
				}
				catch (IOException e) {
					// TODO tell the user something went wrong - update feedbackpanel
					error("Unable to load context: " + e.getMessage());
				}
				// update link
				setVisible(context.getMoreLeft() > 0);
				aTarget.add(left, this);
			}
		});

		add(new AjaxLink<ItemContext>("moreRight", new PropertyModel<ItemContext>(context,
				"moreRight"))
		{
			private static final long serialVersionUID = 3185794699985733066L;

			{
				setVisible(context.getMoreRight() > 0);
				setOutputMarkupId(true);
			}

			@Override
			public void onComponentTagBody(final MarkupStream markupStream,
					final ComponentTag openTag)
			{
				replaceComponentTagBody(markupStream, openTag, "... >>> ("
						+ getDefaultModelObjectAsString() + " chars)");
			}

			@Override
			public void onClick(AjaxRequestTarget aTarget)
			{
				// get more context
				try {
					context = getContext(false, true);
					right.setDefaultModel(new PropertyModel<ItemContext>(context, "right"));
					setModel(new PropertyModel<ItemContext>(context, "moreRight"));
				}
				catch (IOException e) {
					// TODO tell the user something went wrong - update feedbackpanel
					error("Unable to load context: " + e.getMessage());
				}
				// update link
				setVisible(context.getMoreRight() > 0);
				aTarget.add(right, this);
			}
		});
	}

	private ItemContext getContext(boolean doExtendLeft, boolean doExtendRight)
		throws IOException
	{
		if (doExtendLeft) {
			leftSize += extensionSize;
		}
		if (doExtendRight) {
			rightSize += extensionSize;
		}
		return contextProvider.getContext(evaluationItem, leftSize, rightSize);
	}

	private class HtmlLabel
		extends Label
	{
		private static final long serialVersionUID = -8580880708875030344L;

		public HtmlLabel(String aId, IModel<?> aModel)
		{
			super(aId, aModel);
			setEscapeModelStrings(false);
		}

		@Override
		public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
		{
			replaceComponentTagBody(markupStream, openTag,
					Strings.escapeMarkup(getDefaultModelObjectAsString(), false, false).toString()
							.replaceAll("\n", "<br/>"));
		}
	}
}
