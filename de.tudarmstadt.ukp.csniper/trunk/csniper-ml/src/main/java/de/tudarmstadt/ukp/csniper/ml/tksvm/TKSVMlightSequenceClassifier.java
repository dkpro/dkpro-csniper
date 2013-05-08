/** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
 */
package de.tudarmstadt.ukp.csniper.ml.tksvm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.jar.SequenceClassifier_ImplBase;

import de.tudarmstadt.ukp.dkpro.core.api.resources.RuntimeProvider;

/**
 * <br>
 * Copyright (c) 2007-2008, Regents of the University of Colorado <br>
 * All rights reserved.
 * 
 * @author Daryl Lonnon
 * @version 0.2.1
 * 
 *          A Tree Kernel SVM light classifier implementation. All features named with the prefix
 *          "TK_" treated as Tree Kernels.
 * 
 * @uses TreeFeatureVector
 * @see OVATKSVMlightClassifier
 */
public class TKSVMlightSequenceClassifier
	extends SequenceClassifier_ImplBase<TreeFeatureVector, Boolean, Boolean>
{
	static Logger logger = UIMAFramework.getLogger(TKSVMlightSequenceClassifier.class);

	File mFile;

	/**
	 * Constructor
	 * 
	 * @param featuresEncoder
	 *            The features encoder used by this classifier.
	 * @param outcomeEncoder
	 *            The outcome encoder used by this classifier.
	 * @param models
	 *            The files for the models used by this classifier.
	 */
	public TKSVMlightSequenceClassifier(FeaturesEncoder<TreeFeatureVector> featuresEncoder,
			OutcomeEncoder<Boolean, Boolean> outcomeEncoder, File modelFile)
	{
		super(featuresEncoder, outcomeEncoder);
		mFile = modelFile;
	}

	/**
	 * Classify a features list.
	 * 
	 * @param features
	 *            The feature list to classify.
	 * @returns A Boolean of whether the features match this classification.
	 */
	@Override
	public List<Boolean> classify(List<List<Feature>> aFeatures)
		throws CleartkProcessingException
	{
		List<Boolean> outcomes = new ArrayList<Boolean>();
		List<ScoredOutcome<Boolean>> scores = score(aFeatures);
		for (ScoredOutcome<Boolean> s : scores) {
			outcomes.add(s.getOutcome());
		}
		return outcomes;
	}

	/**
	 * Score a list of features against the model.
	 * 
	 * @param features
	 *            The features to classify
	 * @param maxResult
	 *            The maximum number of results to return in the list (at most 2).
	 * @returns A list of scored outcomes ordered by likelihood.
	 */
	@Override
	public List<ScoredOutcome<List<Boolean>>> score(List<List<Feature>> aFeatures,
			int aMaxResults)
	{

//		List<ScoredOutcome<Boolean>> resultList = new ArrayList<ScoredOutcome<Boolean>>();
//		if (maxResults > 0)
//			resultList.add(this.score(features));
//		if (maxResults > 1) {
//			ScoredOutcome<Boolean> v1 = resultList.get(0);
//			ScoredOutcome<Boolean> v2 = new ScoredOutcome<Boolean>(!v1.getOutcome(),
//					1 - v1.getScore());
//			resultList.add(v2);
//		}
//		return resultList;
		// TODO implement...
		throw new UnsupportedOperationException("Not yet implemented.");
	}

	private List<ScoredOutcome<Boolean>> score(List<List<Feature>> featuresList)
		throws CleartkProcessingException
	{
		List<TreeFeatureVector> featureVectors = new ArrayList<TreeFeatureVector>();
		for (List<Feature> features : featuresList) {
			featureVectors.add(featuresEncoder.encodeAll(features));
		}
		List<Double> predictions = tkSvmLightPredict(mFile, featureVectors);

		// I got this from the svmlight classifier, shouldn't this be over 0.0 (doesn't it range
		// from -1.0 to 1.0
		List<ScoredOutcome<Boolean>> outcomes = new ArrayList<ScoredOutcome<Boolean>>();

		for (double prediction : predictions) {
			if ((prediction > 0.0)) {
				outcomes.add( new ScoredOutcome<Boolean>(true, prediction));
			}
			else {
				outcomes.add( new ScoredOutcome<Boolean>(false, Math.abs(prediction)));
			}
		}
		return outcomes;	
	}

	/**
	 * Predict which side of the line a feature vector resides upon for a particular svm model.
	 * 
	 * @param mFile
	 *            The model file to predict against.
	 * @param featureVector
	 *            The feature vector to predict for.
	 * @return A double that represents which side of the line the feature vector resides (negative
	 *         below, positive above).
	 * @throws CleartkProcessingException
	 */
	// Moving this into public space so it can be used by one versus all thing.
	public static List<Double> tkSvmLightPredict(File mFile, List<TreeFeatureVector> featureVectors)
		throws CleartkProcessingException
	{
		List<Double> predictions;

		RuntimeProvider runtime = new RuntimeProvider("classpath:/tksvmlight/");
		try {
			File cFile = File.createTempFile("tkclassify", ".txt");
			File oFile = File.createTempFile("tkoutput", ".out");
			BufferedWriter out = new BufferedWriter(new FileWriter(cFile));
			for (TreeFeatureVector tfv : featureVectors) {
				out.write("0");
				out.write(TKSVMlightDataWriter.createString(tfv));
				out.write(SystemUtils.LINE_SEPARATOR);
			}
			out.close();

			String[] command = new String[4];
			command[0] = runtime.getFile("svm_classify").getAbsolutePath();
			command[1] = cFile.getPath();
			command[2] = mFile.getPath();
			command[3] = oFile.getPath();

			logger.log(Level.INFO,
					"classifying with tree kernel svmlight using the following command: "
							+ toString(command));
			logger.log(
					Level.INFO,
					"if the tree kernel svmlight classifier does not seem to be working correctly, then try running the above command directly to see if e.g. svm_classify gives a useful error message.");
			Process process = Runtime.getRuntime().exec(command);
			output(process.getInputStream(), System.out);
			output(process.getErrorStream(), System.err);
			process.waitFor();

			predictions = new ArrayList<Double>();
			for (String line : FileUtils.readLines(oFile)) {
				predictions.add(Double.parseDouble(line));
			}			
		}
		catch (IOException e) {
			throw new CleartkProcessingException(e);
		}
		catch (InterruptedException e) {
			throw new CleartkProcessingException(e);
		}
		return predictions;
	}

	private static String toString(String[] command)
	{
		StringBuilder sb = new StringBuilder();
		for (String cmmnd : command) {
			sb.append(cmmnd + " ");
		}
		return sb.toString();
	}

	private static void output(InputStream input, PrintStream output)
		throws IOException
	{
		byte[] buffer = new byte[128];
		int count = input.read(buffer);
		while (count != -1) {
			output.write(buffer, 0, count);
			count = input.read(buffer);
		}
	}

	public FeaturesEncoder<TreeFeatureVector> getFeaturesEncoder()
	{
		return featuresEncoder;
	}
	
	public List<Double> tkSvmLightPredict2(File cFile)
		throws CleartkProcessingException
	{
		List<Double> predictions;

		RuntimeProvider runtime = new RuntimeProvider("classpath:/tksvmlight/");
		try {
			File oFile = File.createTempFile("tkoutput", ".out");

			String[] command = new String[4];
			command[0] = runtime.getFile("svm_classify").getAbsolutePath();
			command[1] = cFile.getPath();
			command[2] = mFile.getPath();
			command[3] = oFile.getPath();

			logger.log(Level.INFO,
					"classifying with tree kernel svmlight using the following command: "
							+ toString(command));
			logger.log(
					Level.INFO,
					"if the tree kernel svmlight classifier does not seem to be working correctly, then try running the above command directly to see if e.g. svm_classify gives a useful error message.");
			Process process = Runtime.getRuntime().exec(command);
			output(process.getInputStream(), System.out);
			output(process.getErrorStream(), System.err);
			process.waitFor();

			predictions = new ArrayList<Double>();
			for (String line : FileUtils.readLines(oFile)) {
				predictions.add(Double.parseDouble(line));
			}
		}
		catch (IOException e) {
			throw new CleartkProcessingException(e);
		}
		catch (InterruptedException e) {
			throw new CleartkProcessingException(e);
		}
		finally {
			runtime.uninstall();
		}
		return predictions;
	}
}