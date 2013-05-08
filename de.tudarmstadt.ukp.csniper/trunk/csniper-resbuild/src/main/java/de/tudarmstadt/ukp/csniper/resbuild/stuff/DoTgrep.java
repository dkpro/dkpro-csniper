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

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.junit.Test;
import org.uimafit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.csniper.resbuild.ProgressLogger;
import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasReader;
import de.tudarmstadt.ukp.dkpro.core.io.tgrep.TGrepWriter;

public class DoTgrep
{
	@Test
	public void doTgrep()
		throws UIMAException, IOException
	{
		double start = System.currentTimeMillis();

		CollectionReader bincas = createCollectionReader(SerializedCasReader.class,
				SerializedCasReader.PARAM_PATH, "D:\\hadoop\\output\\DEWAC\\ser_renamed",
				SerializedCasReader.PARAM_PATTERNS, new String[]{ "[+]**/*.xz" });

		AnalysisEngineDescription tgrep = createPrimitiveDescription(TGrepWriter.class,
				TGrepWriter.PARAM_COMPRESSION, CompressionMethod.GZIP,
				TGrepWriter.PARAM_DROP_MALFORMED_TREES, true,
				TGrepWriter.PARAM_TARGET_LOCATION, "D:\\hadoop\\tgrep\\dewac",
				TGrepWriter.PARAM_WRITE_COMMENTS, true,
				TGrepWriter.PARAM_WRITE_T2C, true);

		AnalysisEngineDescription log = createPrimitiveDescription(ProgressLogger.class,
				ProgressLogger.PARAM_BRIEF_OUTPUT, true);

		SimplePipeline.runPipeline(bincas, log, tgrep);

		double stop = System.currentTimeMillis();
		double s = (stop - start) / 1000;
		System.out.println("Time used: " + (int)s/60 + "min, " + s%60 + "sec");
	}
}
