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
package de.tudarmstadt.ukp.csniper.webapp.search.tgrep;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.lang.SystemUtils;
import org.springframework.beans.factory.annotation.Required;

import de.tudarmstadt.ukp.csniper.webapp.search.CorpusService;
import de.tudarmstadt.ukp.csniper.webapp.search.SearchEngine;

public class TgrepEngine
	implements SearchEngine, Serializable
{
	private static final long serialVersionUID = 1375358091252109037L;

	public static final String EXT_CORPUS = ".txt";
	public static final String EXT_BINARY = ".t2c";
	public static final String EXT_COMPRESSED = EXT_BINARY + ".gz";
	public static final String COMMENT_SEPARATOR = ",";

	private String name;
	private File tgrepExecutable;
	private CorpusService corpusService;

	@Required
	public void setTgrepExecutable(File aTgrepExecutable)
	{
		tgrepExecutable = aTgrepExecutable;
	}

	public File getTgrepExecutable()
	{
		return tgrepExecutable;
	}

	@Override
	public void setBeanName(String aName)
	{
		name = aName;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public void setCorpusService(CorpusService aCorpusService)
	{
		corpusService = aCorpusService;
	}

	@Override
	public TgrepQuery createQuery(String aType, String aCollectionId, String aQuery)
	{
		return new TgrepQuery(this, aType, aCollectionId, aQuery);
	}
	
	public String getCorpusPath(String aCollectionId)
	{
		File base = new File(new File(corpusService.getRepositoryPath(), aCollectionId), name);

		// try gzipped corpus file first
		File cFile = new File(base, aCollectionId + EXT_COMPRESSED);

		// workaround for cygwin: packed corpus files only work with this approach
		if (cFile.exists() && SystemUtils.IS_OS_WINDOWS) {
			return "/cygdrive/" + cFile.getAbsolutePath().replace("\\", "/").replace(":", "");
		}
		else {
			cFile = new File(base, aCollectionId + EXT_BINARY);
			return cFile.getAbsolutePath();
		}
	}
}
