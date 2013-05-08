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
package de.tudarmstadt.ukp.csniper.webapp.analysis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;

public class ExamplesRepository
{
	private Map<String, Map<String, Resource>> examples;

	@Required
	public void setExamples(Map<String, Resource[]> aExamples)
	{
		examples = new HashMap<String, Map<String, Resource>>();
		for (String tool : aExamples.keySet()) {
			Map<String, Resource> toolExamples = examples.get(tool);
			if (toolExamples == null) {
				toolExamples = new HashMap<String, Resource>();
				examples.put(tool, toolExamples);
			}

			for (Resource res : aExamples.get(tool)) {
				toolExamples.put(res.getFilename(), res);
			}
		}
	}

	public Map<String, Resource> getExamplesForTool(String aTool)
	{
		Map<String, Resource> toolExamples = examples.get(aTool);
		if (toolExamples == null) {
			return new HashMap<String, Resource>();
		}
		else {
			return toolExamples;
		}
	}

	public String getExample(String aTool, String aKey)
	{
		Resource res = getExamplesForTool(aTool).get(aKey);
		if (res == null) {
			return "NO SCRIPT FOUND";
		}
		else {
			try {
				return IOUtils.toString(res.getInputStream(), "UTF-8");
			}
			catch (IOException e) {
				return "ERROR LOADING SCRIPT: "+e.getMessage();
			}
		}
	}

	public List<String> getTools()
	{
		return new ArrayList<String>(examples.keySet());
	}
	
	public String getDefaultExampleForTool(String aTool)
	{
		Collection<String> keys = getExamplesForTool(aTool).keySet();
		if (keys.size() == 0) {
			return null;
		}
		else {
			return keys.iterator().next();
		}
	}
}
