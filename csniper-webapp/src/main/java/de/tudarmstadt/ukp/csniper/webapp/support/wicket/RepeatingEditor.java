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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.IFormModelUpdateListener;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

/**
 * Adapted from http://wicketinaction.com/2008/10/building-a-listeditor-form-component/
 * 
 * @author Erik-Lân Do Dinh
 */
public abstract class RepeatingEditor<T>
	extends RepeatingView
	implements IFormModelUpdateListener
{

	private static final long serialVersionUID = 7693701876791908846L;

	List<T> items;

	public RepeatingEditor(String id)
	{
		super(id);
	}

	public RepeatingEditor(String id, IModel<List<T>> model)
	{
		super(id, model);
	}

	protected abstract void onPopulateItem(RepeatingItem<T> item);

	public void addItem(T value)
	{
		items.add(value);
//		addItem(items.size() - 1);
		updateModel();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onBeforeRender()
	{
		items = new ArrayList<T>((List<T>) getDefaultModelObject());

		// remove old elements prior to re-adding; some may have been deleted, some changed;
		// this may not be the fanciest way to do this
		removeAll();

		for (int i = 0; i < items.size(); i++) {
			RepeatingItem<T> li = new RepeatingItem<T>(newChildId(), i);
			add(li);
			onPopulateItem(li);
		}
		super.onBeforeRender();
	}

//	private void addItem(int aIndex)
//	{
//		RepeatingItem<T> li = new RepeatingItem<T>(newChildId(), aIndex);
//		add(li);
//		onPopulateItem(li);
//	}

	@Override
	public void updateModel()
	{
		setDefaultModelObject(items);
	}

	@SuppressWarnings("unchecked")
	public List<T> getModelObject()
	{
		return (List<T>) getDefaultModelObject();
	}
}
