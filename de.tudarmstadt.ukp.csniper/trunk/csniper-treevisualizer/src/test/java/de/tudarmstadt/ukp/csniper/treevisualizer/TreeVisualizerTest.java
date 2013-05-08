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
package de.tudarmstadt.ukp.csniper.treevisualizer;

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;
import static org.uimafit.pipeline.SimplePipeline.runPipeline;

import java.io.IOException;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReader;
import org.junit.Test;

import de.tudarmstadt.ukp.csniper.treevisualizer.TreeVisualizer;
import de.tudarmstadt.ukp.dkpro.core.io.negra.NegraExportReader;

public class TreeVisualizerTest
{
	@Test
	public void test()
		throws UIMAException, IOException
	{
		String filename = "src/test/resources/corpus-sample.export";

		CollectionReader ner = createCollectionReader(NegraExportReader.class,
				NegraExportReader.PARAM_SOURCE_LOCATION, filename,
				NegraExportReader.PARAM_LANGUAGE, "de");

		AnalysisEngineDescription stv = createPrimitiveDescription(TreeVisualizer.class,
				TreeVisualizer.PARAM_NAMESPACE, "__",
				TreeVisualizer.PARAM_OUTPUT_PATH, "target/png/");

		runPipeline(ner, stv);
	}
}
