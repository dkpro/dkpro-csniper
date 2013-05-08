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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang.time.StopWatch;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.CASCompleteSerializer;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.collection.CollectionException;

import de.tudarmstadt.ukp.dkpro.core.api.io.ResourceCollectionReaderBase;

public class BinaryCasReader
	extends ResourceCollectionReaderBase
{
	StopWatch w;

	{
		w = new StopWatch();
		w.start();
		w.suspend();
	}

	@Override
	public void getNext(CAS aCAS)
		throws IOException, CollectionException
	{
		w.resume();
		
		Resource res = nextFile();
		InputStream is = null;
		try {
			is = res.getInputStream();
			if (res.getResource().getFilename().endsWith(".gz")) {
				is = new GZIPInputStream(is);
			}
			CASCompleteSerializer serializer = (CASCompleteSerializer) new ObjectInputStream(
					new BufferedInputStream(is)).readObject();
			((CASImpl) aCAS).reinit(serializer);
		}
		catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		finally {
			closeQuietly(is);
		}
		
		w.suspend();
	}
	
	@Override
	public void destroy()
	{
		w.stop();
		System.out.println("read in : " + w);
	}
}
