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

import static org.apache.commons.io.IOUtils.closeQuietly;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.commons.lang.time.StopWatch;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.impl.CASCompleteSerializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;

public class BinaryCasWriter
	extends JCasFileWriter_ImplBase
{
	StopWatch w1;
	StopWatch w2;
	StopWatch w3;
	long size1;
	long size2;

	{
		w1 = new StopWatch();
		w1.start();
		w1.suspend();
		
		w2 = new StopWatch();
		w2.start();
		w2.suspend();

		w3 = new StopWatch();
		w3.start();
		w3.suspend();
}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		
        OutputStream docOS = null;
        try {
    		w1.resume();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(500 * 1024);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
    		CASCompleteSerializer serializer = new CASCompleteSerializer(aJCas.getCasImpl());
    		oos.writeObject(serializer);
    		closeQuietly(oos);
    		size1 += bos.size();
    		w1.suspend();
    		
    		w3.resume();
            ByteArrayOutputStream bo2s = new ByteArrayOutputStream(500 * 1024);
			XmiCasSerializer.serialize(aJCas.getCas(), bo2s);
    		size2 += bo2s.size();
    		w3.suspend();
    		
//    		w2.resume();
//            docOS = getOutputStream(aJCas, ".bin");
//            docOS.write(bos.toByteArray());
//    		w2.suspend();
        }
        catch (Exception e) {
            throw new AnalysisEngineProcessException(e);
        }
        finally {
            closeQuietly(docOS);
        }
        
        System.out.printf("bin: %s %d %n", w1, size1);
        System.out.printf("xmi: %s %d %n", w3, size2);
	}
	
	@Override
	public void destroy()
	{
		w1.stop();
		System.out.println("encoded bin : " + w1);
		w2.stop();
		System.out.println("written in : " + w1);
		w3.stop();
		System.out.println("encoded xmi : " + w1);
	}
}
