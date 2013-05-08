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
package de.tudarmstadt.ukp.csniper.resbuild.hadoop;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.ToolRunner;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.csniper.resbuild.stuff.Renamer;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasWriter;
import de.tudarmstadt.ukp.dkpro.core.io.imscwb.ImsCwbReader;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;
import de.tudarmstadt.ukp.dkpro.hpc.hadoop.DkproHadoopDriver;
import de.tudarmstadt.ukp.dkpro.hpc.hadoop.DkproMapper;
import de.tudarmstadt.ukp.dkpro.hpc.hadoop.DkproReducer;

/**
 * @author Erik-Lân Do Dinh
 */
public class DewacHadoopCorpusBuilder
	extends DkproHadoopDriver
{
	private static final String COLLECTION_ID = "DEWAC";
	private static final String HADOOP_USER_HOME = "/home/local/UKP/dodinh/corpora";
	private static final String INPUT_PATH = HADOOP_USER_HOME;
	private static final String INCLUSION_FILE = HADOOP_USER_HOME + "/inclusions.txt";
	private static final String EXCLUSION_FILE = HADOOP_USER_HOME + "/exclusions.txt";
	// $dir will automatically be replaced by the user home dir on the hdfs
	private static final String OUTPUT_SER_CAS_PATH = "$dir/" + COLLECTION_ID + "/serialized/";
	private static final String OUTPUT_CSV_PATH = "$dir/" + COLLECTION_ID + "/csv/";
	private static final String CLASS_NAME = DewacHadoopCorpusBuilder.class.getSimpleName();

	@Override
	public CollectionReader buildCollectionReader()
		throws ResourceInitializationException
	{
		List<String> patterns = new ArrayList<String>();

		try {
			patterns.addAll(read(INCLUSION_FILE, "[+]**/"));
			System.out.println("Including documents specified in [" + INCLUSION_FILE + "].");
		}
		catch (IOException e) {
			patterns.add("[+]**/*.xml");
			System.out.println("No inclusions specified, parsing all documents in the input path ["
					+ INPUT_PATH + "].");
		}

		try {
			patterns.addAll(read(EXCLUSION_FILE, "[-]**/"));
			System.out.println("Excluding documents specified in [" + EXCLUSION_FILE + "].");
		}
		catch (IOException e) {
			System.out.println("No exclusions specified, parsing all specified documents.");
		}

		CollectionReader reader = createCollectionReader(ImsCwbReader.class, 
				ImsCwbReader.PARAM_PATH, INPUT_PATH,
				ImsCwbReader.PARAM_PATTERNS, new String[] { "[+]DEWAC-1.xml" },
				ImsCwbReader.PARAM_LANGUAGE, "de",
				ImsCwbReader.PARAM_ID_IS_URL, true,
				ImsCwbReader.PARAM_GENERATE_NEW_IDS, true,
				ImsCwbReader.PARAM_ENCODING, "ISO-8859-1");

		return reader;
	}

	private List<String> read(String aFile, String aPatternPrefix)
		throws IOException
	{
		List<String> patterns = new ArrayList<String>();
		for (String s : FileUtils.readLines(new File(aFile), "UTF-8")) {
			patterns.add(aPatternPrefix + s);
		}
		return patterns;
	}

	@Override
	public AnalysisEngineDescription buildMapperEngine(Configuration conf)
		throws ResourceInitializationException
	{
		// parse
		AnalysisEngineDescription sp = createPrimitiveDescription(StanfordParser.class,
				StanfordParser.PARAM_WRITE_PENN_TREE, true,
				StanfordParser.PARAM_LANGUAGE, "en",
				StanfordParser.PARAM_VARIANT, "factored");

		// output as serialized cas
		AnalysisEngineDescription scw = createPrimitiveDescription(SerializedCasWriter.class,
				SerializedCasWriter.PARAM_COMPRESSION, CompressionMethod.XZ,
				SerializedCasWriter.PARAM_PATH, OUTPUT_SER_CAS_PATH,
				SerializedCasWriter.PARAM_STRIP_EXTENSION, true);

		// output as csv for fast db import
		AnalysisEngineDescription csvw = createPrimitiveDescription(PennTreesToCsvWriter.class,
				PennTreesToCsvWriter.PARAM_PATH, OUTPUT_CSV_PATH);
		
		// rename collection id to corpus (originally, path is used)
		AnalysisEngineDescription ren = createPrimitiveDescription(Renamer.class,
				Renamer.PARAM_COLLECTION_ID, COLLECTION_ID);

		return createAggregateDescription(sp, scw, csvw, ren);
	}

	@Override
	public void configure(JobConf job)
	{
		Properties p = new Properties();
		try {
			p.load(new FileInputStream(HADOOP_USER_HOME + "/" + CLASS_NAME + ".properties"));

			for (Entry<Object, Object> e : p.entrySet()) {
				job.set((String) e.getKey(), (String) e.getValue());
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Could not load jobs properties file [" + CLASS_NAME
					+ ".properties].");
			System.out.println("Using default job configuration instead.");
		}
		// these do not work:
		// job.setBoolean("mapreduce.job.user.classpath.first", true);
		// job.setBoolean("mapreduce.user.classpath.first", true);

		// this works, but leads to a strange filesystem error
		// it actually sets: mapreduce.task.classpath.user.precedence to true
		// job.setUserClassesTakesPrecedence(true);
	}

	public static void main(String[] args)
	{
		try {
			DewacHadoopCorpusBuilder pipeline = new DewacHadoopCorpusBuilder();

			pipeline.setMapperClass(DkproMapper.class);
			pipeline.setReducerClass(DkproReducer.class);
			ToolRunner.run(new Configuration(), pipeline, args);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
