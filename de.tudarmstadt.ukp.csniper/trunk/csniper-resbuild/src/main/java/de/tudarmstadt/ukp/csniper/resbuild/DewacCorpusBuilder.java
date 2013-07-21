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

import static org.apache.uima.fit.factory.CollectionReaderFactory.createDescription;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.imscwb.ImsCwbReader;

public class DewacCorpusBuilder
{
	public static void main(String[] args) throws Exception
	{
		CollectionReaderDescription reader = createDescription(ImsCwbReader.class, 
				ImsCwbReader.PARAM_PATH, "/Users/bluefire/Downloads",
				ImsCwbReader.PARAM_PATTERNS, new String[] { "[+]DEWAC-1.xml" },
				ImsCwbReader.PARAM_LANGUAGE, "de",
				ImsCwbReader.PARAM_ID_IS_URL, true,
				ImsCwbReader.PARAM_GENERATE_NEW_IDS, true,
				ImsCwbReader.PARAM_ENCODING, "ISO-8859-1");

		SimplePipeline.runPipeline(reader, CSniperConsumerUtils.getConsumers("dewac"));
	}
}
