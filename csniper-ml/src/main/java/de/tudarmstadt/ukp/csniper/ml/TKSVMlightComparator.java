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
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.classifier.Feature;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.testing.util.HideOutput;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.csniper.ml.tksvm.TKSVMlightClassifier;
import de.tudarmstadt.ukp.csniper.ml.tksvm.TKSVMlightClassifierBuilder;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;

public class TKSVMlightComparator
	extends JCasAnnotator_ImplBase
{
	public static final String PARAM_OUTPUT_DIRECTORY = "outputDirectory";
	@ConfigurationParameter(name = PARAM_OUTPUT_DIRECTORY, mandatory = true)
	public File outputDirectory;

	private TKSVMlightClassifier classifier;

	private static double tp = 0;
	private static double tn = 0;
	private static double fp = 0;
	private static double fn = 0;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);
		try {
			// load tksvm classifier
			TKSVMlightClassifierBuilder builder = new TKSVMlightClassifierBuilder();
			classifier = builder.loadClassifierFromTrainingDirectory(outputDirectory);
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		// create a new instance for each PennTree
		for (PennTree t : JCasUtil.select(aJCas, PennTree.class)) {
			HideOutput hide = new HideOutput();
			Feature tree = new Feature("TK_tree", StringUtils.normalizeSpace(t.getPennTree()));
			boolean gold = parseBool(DocumentMetaData.get(aJCas).getDocumentTitle());
			boolean clas = classifier.classify(Arrays.asList(tree));
			hide.restoreOutput();

			if (gold == true) {
				if (clas == true) {
					tp++;
				}
				else {
					fn++;
				}
			}
			else {
				if (clas == true) {
					fp++;
				}
				else {
					tn++;
				}
			}
		}
	}

	@Override
	public void collectionProcessComplete()
		throws AnalysisEngineProcessException
	{
		super.collectionProcessComplete();
		double accuracy = (tp + tn) / (tp + tn + fp + fn);
		double precision = tp / (tp + fp);
		double recall = tp / (tp + fn);
		double fmeasure = 2 * precision * recall / (precision + recall);

		System.out.println("\nTest results ------");
		System.out.println("Total instances classified: " + (int) (tp + tn + fp + fn));
		System.out.println("TP[" + (int) tp + "] FP[" + (int) fp + "] FN[" + (int) fn + "] TN["
				+ (int) tn + "]");
		System.out.println("Accuracy:  " + accuracy);
		System.out.println("Precision: " + precision);
		System.out.println("Recall:    " + recall);
		System.out.println("F-Measure: " + fmeasure);
	}

	private Boolean parseBool(String aResult)
	{
		if (aResult.equalsIgnoreCase("correct")) {
			return true;
		}
		else if (aResult.equalsIgnoreCase("wrong")) {
			return false;
		}
		else {
			throw new IllegalArgumentException("This should not happen. >:(");
		}
	}
}
