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

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createCollectionReader;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.junit.Test;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.csniper.resbuild.ProgressLogger;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasReader;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasWriter;

public class RenameCollection
{
	private static final String CORPUS = "DEWAC";
	private static final String OUTPUT_SER_CAS_PATH = "D:\\hadoop\\output\\" + CORPUS + "\\ser_renamed";

	@Test
	public void rename()
		throws UIMAException, IOException
	{
		CollectionReader bincas = createCollectionReader(SerializedCasReader.class,
				SerializedCasReader.PARAM_PATH, "D:\\hadoop\\output\\" + CORPUS + "\\serialized",
				SerializedCasReader.PARAM_PATTERNS, new String[] { "[+]**/*.ser.xz" });

		AnalysisEngineDescription renamer = createPrimitiveDescription(Renamer.class, 
				Renamer.PARAM_COLLECTION_ID, CORPUS);

		AnalysisEngineDescription scw = createPrimitiveDescription(SerializedCasWriter.class,
				SerializedCasWriter.PARAM_COMPRESSION, CompressionMethod.XZ,
				SerializedCasWriter.PARAM_TARGET_LOCATION, OUTPUT_SER_CAS_PATH,
				SerializedCasWriter.PARAM_STRIP_EXTENSION, true);

		AnalysisEngineDescription log = createPrimitiveDescription(ProgressLogger.class,
				ProgressLogger.PARAM_BRIEF_OUTPUT, true);

		SimplePipeline.runPipeline(bincas, renamer, scw, log);
//		SimplePipeline.runPipeline(bincas, log);
	}
}
