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
package de.tudarmstadt.ukp.csniper.resbuild.stuff;

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.csniper.resbuild.ProgressLogger;
import de.tudarmstadt.ukp.csniper.resbuild.hadoop.BncReaderReloaded;
import de.tudarmstadt.ukp.csniper.resbuild.hadoop.PennTreesToCsvWriter;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasWriter;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordParser;

/** 
 * @author Erik-Lân Do Dinh
 */
public class BncLocalCorpusBuilder
{
	private static final String COLLECTION_ID = "BNC";
	private static final String HADOOP_USER_HOME = "D:/ukp/data";
	private static final String INPUT_BNC_PATH = "jar:file:" + HADOOP_USER_HOME + "/BNC.zip!";
	private static final String INCLUSION_FILE = HADOOP_USER_HOME + "/inclusions.txt";
	private static final String EXCLUSION_FILE = HADOOP_USER_HOME + "/exclusions.txt";
	// $dir will automatically be replaced by the user home dir on the hdfs 
	private static final String OUTPUT_SER_CAS_PATH = HADOOP_USER_HOME + "/output2/" + COLLECTION_ID + "/serialized/";
	private static final String OUTPUT_CSV_PATH = HADOOP_USER_HOME + "/output2/" + COLLECTION_ID + "/csv/";
	private static final String CLASS_NAME = BncLocalCorpusBuilder.class.getSimpleName();

	public static CollectionReader buildCollectionReader()
		throws ResourceInitializationException
	{
		List<String> patterns = new ArrayList<String>();

		try {
			patterns.addAll(read(INCLUSION_FILE, "[+]**/"));
			System.out.println("Including documents specified in [" + INCLUSION_FILE + "].");
		}
		catch (IOException e) {
			patterns.add("[+]**/*.xml");
			System.out.println("No inclusions specified, parsing all BNC documents.");
		}

		try {
			patterns.addAll(read(EXCLUSION_FILE, "[-]**/"));
			System.out.println("Excluding documents specified in [" + EXCLUSION_FILE + "].");
		}
		catch (IOException e) {
			System.out.println("No exclusions specified, parsing all specified BNC documents.");
		}

		CollectionReader reader = createCollectionReader(BncReaderReloaded.class,
				BncReaderReloaded.PARAM_PATH, INPUT_BNC_PATH,
				BncReaderReloaded.PARAM_PATTERNS, patterns.toArray(new String[0]),
				BncReaderReloaded.PARAM_LANGUAGE, "en");
		return reader;
	}

	private static List<String> read(String aFile, String aPatternPrefix)
		throws IOException
	{
		List<String> patterns = new ArrayList<String>();
		for (String s : FileUtils.readLines(new File(aFile), "UTF-8")) {
			patterns.add(aPatternPrefix + s);
		}
		return patterns;
	}

	public static AnalysisEngineDescription buildMapperEngine()
		throws ResourceInitializationException
	{
		// rename collectionId to BNC (from the path where BNC is located)
		AnalysisEngineDescription rn = createPrimitiveDescription(Renamer.class,
				Renamer.PARAM_COLLECTION_ID, COLLECTION_ID);

		// parse
		AnalysisEngineDescription sp = createPrimitiveDescription(StanfordParser.class,
				StanfordParser.PARAM_WRITE_PENN_TREE, true,
				StanfordParser.PARAM_LANGUAGE, "en",
				StanfordParser.PARAM_VARIANT, "factored",
				StanfordParser.PARAM_QUOTE_BEGIN, new String[] { "‘" },
				StanfordParser.PARAM_QUOTE_END, new String[] { "’" });

		// output as serialized cas
		AnalysisEngineDescription scw = createPrimitiveDescription(SerializedCasWriter.class,
				SerializedCasWriter.PARAM_COMPRESSION, CompressionMethod.XZ,
				SerializedCasWriter.PARAM_PATH, OUTPUT_SER_CAS_PATH,
				SerializedCasWriter.PARAM_STRIP_EXTENSION, true);

		// output as csv for fast db import
		AnalysisEngineDescription csvw = createPrimitiveDescription(PennTreesToCsvWriter.class,
				PennTreesToCsvWriter.PARAM_PATH, OUTPUT_CSV_PATH);
		
		AnalysisEngineDescription log = createPrimitiveDescription(ProgressLogger.class,
				ProgressLogger.PARAM_BRIEF_OUTPUT, true);

		return createAggregateDescription(rn, sp, scw, csvw, log);
	}

	public static void main(String[] args)
	{
		try {
			SimplePipeline.runPipeline(buildCollectionReader(), buildMapperEngine());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
