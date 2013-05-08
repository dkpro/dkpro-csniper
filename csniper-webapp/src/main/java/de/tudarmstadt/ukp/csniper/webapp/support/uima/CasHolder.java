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
package de.tudarmstadt.ukp.csniper.webapp.support.uima;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.TypeSystemUtil;
import org.apache.uima.util.XMLInputSource;
import org.xml.sax.SAXException;

public class CasHolder
	implements Externalizable
{
	private CAS cas;

	public CasHolder()
	{
		// Required for deserialization
	}
	
	public CasHolder(CAS aCas)
	{
		setCas(aCas);
	}

	public CAS getCas()
	{
		return cas;
	}

	public void setCas(CAS aCas)
	{
		cas = aCas;
	}

	@Override
	public void writeExternal(ObjectOutput aOut)
		throws IOException
	{
		if (cas == null) {
			aOut.writeInt(0);
		}
		else {
			ByteArrayOutputStream casOS = new ByteArrayOutputStream();
			ByteArrayOutputStream tsdOS = new ByteArrayOutputStream();
			try {
				XmiCasSerializer.serialize(cas, casOS);
				TypeSystemUtil.typeSystem2TypeSystemDescription(cas.getTypeSystem()).toXML(tsdOS);
			}
			catch (SAXException e) {
				throw new IOException(e);
			}
			// Write TSD data
			byte[] tsdData = tsdOS.toByteArray();
			aOut.writeInt(tsdData.length);
			aOut.write(tsdData);
			
			// Write CAS data
			byte[] casData = casOS.toByteArray();
			aOut.writeInt(casData.length);
			aOut.write(casData);
		}
	}

	@Override
	public void readExternal(ObjectInput aIn)
		throws IOException, ClassNotFoundException
	{
		int tsdSize = aIn.readInt();
		if (tsdSize == 0) {
			cas = null;
		}
		else try {
			byte[] tsdData = new byte[tsdSize];
			ByteArrayInputStream tsdIS = new ByteArrayInputStream(tsdData);
			TypeSystemDescription tsd = UIMAFramework.getXMLParser().parseTypeSystemDescription(
					new XMLInputSource(tsdIS, null));
			cas = CasCreationUtils.createCas(tsd, null, null);
			
			int casSize = aIn.readInt();
			byte[] casData = new byte[casSize];
			aIn.readFully(casData);
			ByteArrayInputStream bis = new ByteArrayInputStream(casData);
			XmiCasDeserializer.deserialize(bis, cas);
		}
		catch (UIMAException e) {
			throw new IOException(e);
		}
		catch (SAXException e) {
			throw new IOException(e);
		}
	}
}
