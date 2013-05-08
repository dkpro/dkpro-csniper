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
package de.tudarmstadt.ukp.csniper.ml;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.CleartkAnnotator;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.jar.Train;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;

public class TKSVMlightModelWriter
	extends CleartkAnnotator<Boolean>
{
//	public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";
//	@ConfigurationParameter(name = PARAM_OUTPUT_DIRECTORY, mandatory = true)
	public File outputDirectory;
	
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException
	{
		super.initialize(aContext);
		outputDirectory = new File((String) aContext.getConfigParameterValue("org.cleartk.classifier.jar.DirectoryDataWriterFactory.outputDirectory"));
	}
	
	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		// create a new instance for each PennTree
		for (PennTree t : JCasUtil.select(aJCas, PennTree.class)) {
			Instance<Boolean> instance = new Instance<Boolean>();
			instance.add(new Feature("TK_tree", StringUtils.normalizeSpace(t.getPennTree())));
			instance.setOutcome(parseBool(DocumentMetaData.get(aJCas).getDocumentTitle()));
			train(instance);
		}
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException
	{
		super.collectionProcessComplete();
		// build the model
		try {
			Train.main(outputDirectory.getPath(), "-t", "5", "-c", "1.0", "-C", "+");
		}
		catch (Exception e) {
			throw new AnalysisEngineProcessException(e);
		}
	}
	
	private void train(Instance<Boolean> instance)
		throws CleartkProcessingException
	{
		dataWriter.write(instance);
	}

	private Boolean parseBool(String aResult)
	{
		return aResult.equalsIgnoreCase("correct");
	}
}
