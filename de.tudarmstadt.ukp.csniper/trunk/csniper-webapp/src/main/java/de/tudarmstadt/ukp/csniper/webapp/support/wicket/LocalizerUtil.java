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

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.MapModel;

public class LocalizerUtil
{
	public static String getString(FormComponent<?> aComponent, String aKey)
	{
		Map<String, Object> args = new HashMap<String, Object>();

		String arg = "label";
		IModel<?> label = aComponent.getLabel();
		if (label != null) {
			args.put(arg, label.getObject());
		}
		else {
			args.put(arg, aComponent.getLocalizer().getString(aComponent.getId(), 
					aComponent.getParent(), aComponent.getId()));
		}

		args.put("input", aComponent.getInput());
		args.put("name", aComponent.getId());

		return aComponent.getString(aKey, new MapModel<String, Object>(args));
	}
}
