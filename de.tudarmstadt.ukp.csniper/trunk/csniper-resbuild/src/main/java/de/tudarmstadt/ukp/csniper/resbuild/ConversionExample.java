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
package de.tudarmstadt.ukp.csniper.resbuild;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import java.io.File;
import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.uimafit.factory.CollectionReaderFactory;
import org.uimafit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasWriter;
import de.tudarmstadt.ukp.dkpro.core.io.imscwb.ImsCwbWriter;
import de.tudarmstadt.ukp.dkpro.core.io.negra.NegraExportReader;

public class ConversionExample
{
	private static final File CQP_HOME = new File("D:\\ukp\\cwb-3.4.3\\bin");

	public static void main(String[] args)
		throws UIMAException, IOException
	{
		File corpusPath = new File("D:\\ukp\\csniper\\corpus-sample.export");
		String corpusName = "sample";
		String language = "en";
		convert(corpusPath, corpusName, language);
	}

	public static void convert(File aPathToCorpus, String aCollection, String aLanguage)
		throws UIMAException, IOException
	{
		// read the sample file
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
				NegraExportReader.class,
				NegraExportReader.PARAM_ENCODING, "ISO-8859-1",
				NegraExportReader.PARAM_LANGUAGE, aLanguage,
				NegraExportReader.PARAM_SOURCE_LOCATION, aPathToCorpus);

		// write serialized CAS (used for displying context in CSniper)
		AnalysisEngineDescription casWriter = createPrimitiveDescription(SerializedCasWriter.class,
				SerializedCasWriter.PARAM_PATH, "target/" + aCollection.toUpperCase() + "/bin",
				SerializedCasWriter.PARAM_USE_DOCUMENT_ID, true,
				SerializedCasWriter.PARAM_COMPRESSION, CompressionMethod.XZ);

		// write corpus in cqp format (used for searching in CSniper)
		AnalysisEngineDescription cqpWriter = createPrimitiveDescription(ImsCwbWriter.class,
				ImsCwbWriter.PARAM_CORPUS_NAME, aCollection,
				ImsCwbWriter.PARAM_TARGET_ENCODING, "UTF-8",
				ImsCwbWriter.PARAM_TARGET_LOCATION, "target/" + aCollection.toUpperCase() + "/cqp",
				ImsCwbWriter.PARAM_CQP_HOME, CQP_HOME,
				ImsCwbWriter.PARAM_WRITE_TEXT_TAG, true,
				ImsCwbWriter.PARAM_WRITE_DOCUMENT_TAG, true,
				ImsCwbWriter.PARAM_WRITE_OFFSETS, true,
				ImsCwbWriter.PARAM_WRITE_LEMMA, true,
				ImsCwbWriter.PARAM_WRITE_DOC_ID, false);

		SimplePipeline.runPipeline(reader, casWriter, cqpWriter);
	}
}
