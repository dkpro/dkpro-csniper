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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;

/**
 * A variant of the {@link AjaxButton} that displays a busy indicator while the ajax request is in
 * progress.<br>
 * As opposed to {@link IndicatingAjaxButton} (which appends the busy indicator to the end),
 * IndicatingAjaxButton injects the busy indicator into the button.
 * 
 * @author Erik-Lân Do Dinh
 */
public abstract class ExtendedIndicatingAjaxButton
	extends AjaxButton
	implements IAjaxIndicatorAware
{
	private static final long serialVersionUID = 1L;

	IModel<String> model, busyModel, failureModel, successModel;

	public ExtendedIndicatingAjaxButton(final String aId, final IModel<String> aModel,
			final IModel<String> aBusyModel, final IModel<String> aFailureModel,
			final IModel<String> aSuccessModel)
	{
		super(aId, aModel);
		model = aModel;
		busyModel = aBusyModel;
		failureModel = aFailureModel;
		successModel = aSuccessModel;
	}

	public ExtendedIndicatingAjaxButton(final String aId, final IModel<String> aModel,
			final IModel<String> aBusyModel)
	{
		this(aId, aModel, aBusyModel, new Model<String>("Failure."), aModel);
	}

	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
		StringBuilder injection = new StringBuilder();
		CharSequence indicator = RequestCycle.get().urlFor(
				new ResourceReferenceRequestHandler(AbstractDefaultAjaxBehavior.INDICATOR));

		injection.append("<img id=\"" + getAjaxIndicatorMarkupId() + "\" src=\"" + indicator
				+ "\" style=\"vertical-align: bottom; display:none;\" /> ");
		injection.append("<span>" + model.getObject() + "</span>");

		replaceComponentTagBody(markupStream, openTag, injection);
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes aAttributes)
	{
		AjaxCallListener listener = new AjaxCallListener()
		{
			private static final long serialVersionUID = 8211975176278631439L;

			@Override
			public CharSequence getSuccessHandler(Component aComponent)
			{
				return getJs(aComponent.getMarkupId(), model.getObject(), false);
			}

			@Override
			public CharSequence getFailureHandler(Component aComponent)
			{
				return getJs(aComponent.getMarkupId(), failureModel.getObject(), false);
			}

			@Override
			public CharSequence getAfterHandler(Component aComponent)
			{
				return getJs(aComponent.getMarkupId(), busyModel.getObject(), true);
			}
		};
		aAttributes.getAjaxCallListeners().add(listener);
	}

	@Override
	public String getAjaxIndicatorMarkupId()
	{
		return getMarkupId() + "-busy-indicator";
	}

	@Override
	public void onError(AjaxRequestTarget target, Form<?> form)
	{
	}

	public String getJs(String aId, String aButtonText, boolean disableButton)
	{
		StringBuilder js = new StringBuilder();

		// get button, set text, disable/enable button
		js.append("var button = document.getElementById('" + aId + "');");
		js.append("button.getElementsByTagName('span')[0].firstChild.data = '" + aButtonText + "';");
		js.append("button.disabled = '" + (disableButton ? "disabled" : "") + "';");

		return js.toString();
	}
}
