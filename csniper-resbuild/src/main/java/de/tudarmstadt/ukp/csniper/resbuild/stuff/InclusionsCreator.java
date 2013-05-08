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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class InclusionsCreator
{
	private static final String BASE_0 = "D:\\ukp\\data\\bnc\\xml";
	private static final String BASE = "D:\\hadoop\\output\\BNC_new";

	@Test
	public void createInclusionsFile()
		throws IOException
	{
		List<String> all = read(BASE_0, "xml");
//		all.removeAll(cut(read(BASE, "csv"), read(BASE, "xz")));
		all.removeAll(cut(read(BASE, "csv"), stripPlusXml(read("serialized.txt"))));
//		all.removeAll(read(BASE, "csv"));
		FileUtils.writeLines(new File(BASE, "inclusions.txt"), "UTF-8", all);
//		all.removeAll(read(BASE, "xz"));
//		all.removeAll(read(BASE, "csv"));
		IOUtils.writeLines(all, "\n", System.out, "UTF-8");
	}

	private List<String> read(String aFile)
		throws IOException
	{
		return FileUtils.readLines(new File(BASE, aFile), "UTF-8");
	}

	private List<String> read(String aDir, String... aExts)
			throws IOException
	{
		List<String> ret = new ArrayList<String>();
		for (File f : FileUtils.listFiles(new File(aDir), aExts, true)) {
			ret.add(stripPlusXml(f.getName()));
		}
		return ret;
	}
	
	private String stripPlusXml(String aStr)
	{
		return StringUtils.substringBefore(aStr, ".") + ".xml";
	}

	private List<String> stripPlusXml(List<String> aList)
	{
		List<String> ret = new ArrayList<String>();
		for (String s : aList) {
			ret.add(stripPlusXml(s));
		}
		return ret;
	}

	private List<String> cut(List<String> aList1, List<String> aList2)
	{
		List<String> ret = new ArrayList<String>(aList1);
		ret.retainAll(aList2);
		return ret;
	}
}
