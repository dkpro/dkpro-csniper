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

import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;

/**
 * Adapted from http://wicketinaction.com/2008/10/building-a-listeditor-form-component/
 * 
 * @author Erik-Lân Do Dinh
 * 
 * @param <T>
 */
public class RepeatingItem<T>
	extends Item<T>
{
	private static final long serialVersionUID = -1469824558308527864L;

	public RepeatingItem(String id, int index)
	{
		super(id, index);
		setModel(new AbstractReadOnlyModel<T>()
		{
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("unchecked")
			@Override
			public T getObject()
			{
				return ((RepeatingEditor<T>) RepeatingItem.this.getParent()).items.get(getIndex());
			}

		});
	}
}
