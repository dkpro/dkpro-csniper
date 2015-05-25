/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.csniper.webapp.support.wicket;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.model.Model;

/**
 * A variant of the {@link NavigationToolbar} that incorporates a "Jump to" field.
 * 
 * @author Erik-Lân Do Dinh
 */
public class ExtendedNavigationToolbar
	extends NavigationToolbar
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param table
	 *            data table this toolbar will be attached to
	 */
	@SuppressWarnings("rawtypes")
	public ExtendedNavigationToolbar(final DataTable<?, String> table)
	{
		super(table);

		WebMarkupContainer span = (WebMarkupContainer) get("span");
		span.add(new Form("form")
		{
			private static final long serialVersionUID = 1L;

			{
				final NumberTextField<Long> jumpto = new NumberTextField<Long>("jumpto",
						new Model<Long>())
				{
					private static final long serialVersionUID = 1L;

					@Override
					public void onConfigure()
					{
						super.onConfigure();
						setModelObject(table.getCurrentPage() + 1);
						setMinimum(1L);
						setMaximum(table.getPageCount());
					}
				};
				jumpto.setType(Long.class);
				add(jumpto);
				add(new Button("jumptoButton")
				{
					private static final long serialVersionUID = 1L;

					@Override
					public void onSubmit()
					{
						table.setCurrentPage(jumpto.getModelObject() - 1);
					}
				});
			}
		});
	}
}
