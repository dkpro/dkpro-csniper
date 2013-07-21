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

import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasReader;

public class SerCasViewerPipe
{
	String base = "D:\\hadoop\\output\\DEWAC\\serialized";//_renamed";
	String file = "0.ser.xz";
	
	@Test
	public void view()
		throws UIMAException, IOException
	{
		CollectionReader bincas = createCollectionReader(SerializedCasReader.class,
				SerializedCasReader.PARAM_PATH, base,
				SerializedCasReader.PARAM_PATTERNS, new String[] { "[+]**/" + file });
		
		AnalysisEngineDescription cons = createPrimitiveDescription(PrintConsumer.class);

		SimplePipeline.runPipeline(bincas, cons);
	}
}
