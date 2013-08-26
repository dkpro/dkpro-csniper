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
package de.tudarmstadt.ukp.csniper.webapp.support.uima;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.analysis_component.AnalysisComponent;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.resource.ResourceInitializationException;
import org.springframework.context.ApplicationContext;

import de.tudarmstadt.ukp.csniper.webapp.support.spring.ApplicationContextProvider;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

public class AnalysisEngineFactory
{
	private static Log log = LogFactory.getLog(AnalysisEngineFactory.class);

	public static final String SEGMENTER = "segmenter.properties";
	public static final String PARSER = "parser.properties";

	/**
	 * Create an AnalysisEngine from a .properties file.
	 * 
	 * @param aSettingsFile
	 *            filename of the file which specifies the options for the AE
	 * @param aAdditionalParameters
	 *            additional parameters for the AE
	 * @return an AnalysisEngine with the specified parameters
	 */
	public static AnalysisEngine createAnalysisEngine(String aSettingsFile,
			Object... aAdditionalParameters)
		throws ResourceInitializationException
	{
		Properties options = new Properties();
		InputStream is = null;
		try {
			File settingsFile = new File(getSettingsPath(), aSettingsFile);
			// first look in the settings directory for the specified file
			if (settingsFile.exists()) {
				is = new FileInputStream(settingsFile);
				log.info("Loading AnalysisEngine from " + settingsFile.getAbsolutePath());
			}
			// if the file cannot be found, use the default file from the classpath
			else {
				is = ResourceUtils.resolveLocation("classpath:" + aSettingsFile).openStream();
				log.info("Loading AnalysisEngine from classpath:" + aSettingsFile);
			}
			options.load(is);
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
		finally {
			IOUtils.closeQuietly(is);
		}

		// find class
		String classname;
		try {
			classname = (String) options.remove("classname");
		}
		catch (NullPointerException e) {
			throw new IllegalArgumentException(
					"The properties file needs a property [classname] whose value has to be the qualified pathname of the analysis engine class that should be used.");
		}

		// check for additional parameters
		if (aAdditionalParameters.length % 2 == 1) {
			throw new IllegalArgumentException("Illegal number of additional parameters ["
					+ aAdditionalParameters.length + "], has to be an even amount.");
		}

		// create list of parameters
		List<Object> params = new ArrayList<Object>();
		for (Entry<Object, Object> param : options.entrySet()) {
			params.add(param.getKey());
			params.add(convert(param.getValue()));
		}
		for (Object adParam : aAdditionalParameters) {
			params.add(convert(adParam));
		}

		// create class
		Class<? extends AnalysisComponent> aeClass;
		try {
			aeClass = Class.forName(classname).asSubclass(AnalysisComponent.class);
		}
		catch (ClassNotFoundException e) {
			throw new ResourceInitializationException(e);
		}

		log.info("Creating AnalysisEngine [" + classname + "] with following options: "
				+ StringUtils.join(params, ","));

		return createPrimitive(aeClass, params.toArray());
	}

	private static Object convert(Object aValue)
	{
		if (aValue instanceof String) {
			String aString = (String) aValue;
			if (aString.equalsIgnoreCase("true")) {
				return true;
			}
			else if (aString.equalsIgnoreCase("false")) {
				return false;
			}
			else {
				try {
					return Integer.parseInt(aString);
				}
				catch (NumberFormatException e) {
					return aValue;
				}
			}
		}
		else {
			return aValue;
		}
	}

	private static File getSettingsPath()
	{
		ApplicationContext context = ApplicationContextProvider.getApplicationContext();
		return new File(context.getBean("settingsPath", String.class));
	}
}
