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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UIMAFramework;
import org.apache.uima.util.Logger;
import org.cleartk.classifier.CleartkProcessingException;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.ScoredOutcome;
import org.cleartk.classifier.encoder.features.FeaturesEncoder;
import org.cleartk.classifier.encoder.outcome.OutcomeEncoder;
import org.cleartk.classifier.jar.Classifier_ImplBase;

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
public class TKSVMlightClassifier
	extends Classifier_ImplBase<TreeFeatureVector, Boolean, Boolean>
{

	static Logger logger = UIMAFramework.getLogger(TKSVMlightClassifier.class);

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
	public TKSVMlightClassifier(FeaturesEncoder<TreeFeatureVector> featuresEncoder,
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
	public Boolean classify(List<Feature> features)
		throws CleartkProcessingException
	{
		ScoredOutcome<Boolean> s = score(features);
		return s.getOutcome();
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
	public List<ScoredOutcome<Boolean>> score(List<Feature> features, int maxResults)
		throws CleartkProcessingException
	{

		List<ScoredOutcome<Boolean>> resultList = new ArrayList<ScoredOutcome<Boolean>>();
		if (maxResults > 0)
			resultList.add(this.score(features));
		if (maxResults > 1) {
			ScoredOutcome<Boolean> v1 = resultList.get(0);
			ScoredOutcome<Boolean> v2 = new ScoredOutcome<Boolean>(!v1.getOutcome(),
					1 - v1.getScore());
			resultList.add(v2);
		}
		return resultList;
	}

	public ScoredOutcome<Boolean> score(List<Feature> features)
		throws CleartkProcessingException
	{
		TreeFeatureVector featureVector = featuresEncoder.encodeAll(features);
		double prediction = tkSvmLightPredict(mFile, featureVector);

		if (prediction > 0.0) {
			return new ScoredOutcome<Boolean>(true, prediction);
		}
		else {
			return new ScoredOutcome<Boolean>(false, Math.abs(prediction));
		}
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
	public static double tkSvmLightPredict(File mFile, TreeFeatureVector featureVector)
		throws CleartkProcessingException
	{
		double prediction;

		RuntimeProvider runtime = new RuntimeProvider("classpath:/tksvmlight/");
		try {
			File cFile = File.createTempFile("tkclassify", ".txt");
			File oFile = File.createTempFile("tkoutput", ".out");
			BufferedWriter out = new BufferedWriter(new FileWriter(cFile));
			out.write("0");
			out.write(TKSVMlightDataWriter.createString(featureVector));
			out.write("\n");
			out.close();

			String[] command = new String[4];
			command[0] = runtime.getFile("svm_classify").getAbsolutePath();
			command[1] = cFile.getPath();
			command[2] = mFile.getPath();
			command[3] = oFile.getPath();

//			logger.log(Level.INFO,
//					"classifying with tree kernel svmlight using the following command: "
//							+ toString(command));
//			logger.log(
//					Level.INFO,
//					"if the tree kernel svmlight classifier does not seem to be working correctly, then try running the above command directly to see if e.g. svm_classify gives a useful error message.");
			Process process = Runtime.getRuntime().exec(command);
			System.out.println("output");
			output(process.getInputStream(), System.out);
			System.out.println("error");
			output(process.getErrorStream(), System.err);
			System.out.println("wait");
			process.waitFor();

			BufferedReader in = new BufferedReader(new FileReader(oFile));
			prediction = Double.parseDouble(in.readLine());
			in.close();
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
		return prediction;
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
}
