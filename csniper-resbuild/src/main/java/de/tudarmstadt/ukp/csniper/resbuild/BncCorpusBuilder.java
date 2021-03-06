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

import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;

import de.tudarmstadt.ukp.dkpro.core.io.bnc.BncReader;

public class BncCorpusBuilder
{
	public static void main(String[] args) throws Exception
	{
		CollectionReaderDescription reader = createReaderDescription(BncReader.class, 
				BncReader.PARAM_PATH, "jar:file:/Users/bluefire/UKP/Library/Corpora/BNC/BNC.zip!",
				BncReader.PARAM_PATTERNS, new String[] { "[+]**/*.xml" },
				BncReader.PARAM_LANGUAGE, "en");

		SimplePipeline.runPipeline(reader, CSniperConsumerUtils.getConsumers("bnc"));
	}
}
