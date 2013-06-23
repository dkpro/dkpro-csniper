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

import static org.uimafit.factory.AnalysisEngineFactory.createAggregateDescription;
import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import de.tudarmstadt.ukp.dkpro.core.api.resources.CompressionMethod;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasWriter;
import de.tudarmstadt.ukp.dkpro.core.io.imscwb.ImsCwbWriter;
import de.tudarmstadt.ukp.dkpro.core.io.text.TextWriter;
import de.tudarmstadt.ukp.dkpro.core.io.tgrep.TGrepWriter;
import de.tudarmstadt.ukp.dkpro.core.io.xmi.XmiWriter;

public class CSniperConsumerUtils
{
    public static AnalysisEngineDescription getConsumers(String aCollection)
        throws ResourceInitializationException
    {
        List<AnalysisEngineDescription> aes = new ArrayList<AnalysisEngineDescription>();

        aes.add(createPrimitiveDescription(ProgressLogger.class, ProgressLogger.PARAM_BRIEF_OUTPUT,
                true));

        aes.add(createPrimitiveDescription(SerializedCasWriter.class,
                SerializedCasWriter.PARAM_PATH, "target/" + aCollection + "/bin",
                SerializedCasWriter.PARAM_USE_DOCUMENT_ID, true,
                SerializedCasWriter.PARAM_COMPRESSION, CompressionMethod.XZ));

        aes.add(createPrimitiveDescription(TextWriter.class, TextWriter.PARAM_PATH, "target/"
                + aCollection + "/txt"));

        aes.add(createPrimitiveDescription(XmiWriter.class, 
                XmiWriter.PARAM_PATH, "target/" + aCollection + "/xmi", 
                XmiWriter.PARAM_COMPRESSION, CompressionMethod.XZ,
                XmiWriter.PARAM_TYPE_SYSTEM_FILE, "TypeSystem.xml",
                XmiWriter.PARAM_USE_DOCUMENT_ID, true));

        aes.add(createPrimitiveDescription(TGrepWriter.class, 
                TGrepWriter.PARAM_TARGET_LOCATION, "target/" + aCollection + "/tgrep", 
                TGrepWriter.PARAM_COMPRESSION, CompressionMethod.GZIP,
                TGrepWriter.PARAM_DROP_MALFORMED_TREES, true,
                TGrepWriter.PARAM_WRITE_COMMENTS, true,
                TGrepWriter.PARAM_WRITE_T2C, true));

        aes.add(createPrimitiveDescription(ImsCwbWriter.class, 
                ImsCwbWriter.PARAM_TARGET_ENCODING, "UTF-8", 
                ImsCwbWriter.PARAM_TARGET_LOCATION, "target/" + aCollection + "/cqp",
                // ImsCwbWriter.PARAM_CQP_HOME, "/Users/bluefire/bin/cwb-3.2.0-snapshot-282",
                ImsCwbWriter.PARAM_WRITE_TEXT_TAG, true, 
                ImsCwbWriter.PARAM_WRITE_DOCUMENT_TAG, true, 
                ImsCwbWriter.PARAM_WRITE_OFFSETS, true, 
                ImsCwbWriter.PARAM_WRITE_LEMMA, true,
                ImsCwbWriter.PARAM_WRITE_DOC_ID, false));

        return createAggregateDescription(aes.toArray(new AnalysisEngineDescription[aes.size()]));
    }
}