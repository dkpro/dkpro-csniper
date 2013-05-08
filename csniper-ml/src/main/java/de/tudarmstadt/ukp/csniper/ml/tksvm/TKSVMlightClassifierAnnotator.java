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
package de.tudarmstadt.ukp.csniper.ml.tksvm;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.ScoredOutcome;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.testing.util.HideOutput;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.csniper.ml.type.BooleanClassification;
import de.tudarmstadt.ukp.csniper.ml.type.ClearTkFeature;

public class TKSVMlightClassifierAnnotator
	extends JCasAnnotator_ImplBase
{
	public static final String PARAM_MODEL_PATH = "modelPath";
	@ConfigurationParameter(name = PARAM_MODEL_PATH, mandatory = true)
	public File modelPath;

	private TKSVMlightClassifier classifier;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);
		try {
			// load tksvm classifier
			TKSVMlightClassifierBuilder builder = new TKSVMlightClassifierBuilder();
			classifier = builder.loadClassifierFromTrainingDirectory(modelPath);
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		HideOutput hide = new HideOutput();

		List<Feature> features = new ArrayList<Feature>();
		BooleanClassification bc;
		bc = JCasUtil.selectSingle(aJCas, BooleanClassification.class);

		for (ClearTkFeature ctkf : JCasUtil.select(aJCas, ClearTkFeature.class)) {
			features.add(new Feature(ctkf.getClearTkFeatureName(), ctkf.getClearTkFeatureValue()));
		}

		ScoredOutcome<Boolean> out = classifier.score(features);
		bc.setPredictedLabel(out.getOutcome());
		bc.setScore(out.getScore());

		hide.restoreOutput();
		IOUtils.closeQuietly(hide);
	}
}
