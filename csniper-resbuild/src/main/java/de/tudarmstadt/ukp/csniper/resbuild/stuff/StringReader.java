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

import java.io.IOException;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Progress;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

/**
 * Simple UIMA reader for strings.
 * 
 * @author Erik-Lân Do Dinh
 */
public class StringReader
	extends JCasCollectionReader_ImplBase
{
	public static final String PARAM_INPUT = "Input";
	@ConfigurationParameter(name = PARAM_INPUT, mandatory = true)
	private String input;

	public static final String PARAM_LANGUAGE = "Language";
	@ConfigurationParameter(name = PARAM_LANGUAGE, mandatory = true, defaultValue = "en")
	private String language;

	public static final String PARAM_COLLECTION_ID = "CollectionId";
	@ConfigurationParameter(name = PARAM_COLLECTION_ID, mandatory = true, defaultValue = "COLLECTION_ID")
	private String collectionId;

	public static final String PARAM_DOCUMENT_ID = "DocumentId";
	@ConfigurationParameter(name = PARAM_DOCUMENT_ID, mandatory = true, defaultValue = "DOCUMENT_ID")
	private String documentId;

	private boolean isDone = false;

	@Override
	public void getNext(JCas sJCas)
		throws IOException
	{
		sJCas.setDocumentLanguage(language);
		sJCas.setDocumentText(input);

		DocumentMetaData meta = new DocumentMetaData(sJCas);
		meta.setCollectionId(collectionId);
		meta.setDocumentUri("STRING");
		meta.setDocumentId(documentId);
		meta.addToIndexes();

		isDone = true;
	}

	@Override
	public boolean hasNext()
		throws IOException, CollectionException
	{
		return !isDone;
	}

	@Override
	public Progress[] getProgress()
	{
		return null;
	}
}
