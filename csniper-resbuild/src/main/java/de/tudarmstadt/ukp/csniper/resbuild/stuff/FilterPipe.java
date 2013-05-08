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
import org.apache.commons.lang.StringUtils;

public class FilterPipe
{
	static String base = "D:\\hadoop\\output\\BNC_new\\csv";
//	String file = "*";
//
//	@Test
//	public void view()
//		throws UIMAException, IOException
//	{
//		CollectionReader bincas = createCollectionReader(SerializedCasReader.class,
//				SerializedCasReader.PARAM_PATH, base,
//				SerializedCasReader.PARAM_PATTERNS, new String[] { "[+]**/" + file });
//		
//		AnalysisEngineDescription filter = createPrimitiveDescription(CasFilter.class);
//		
//		AnalysisEngineDescription log = createPrimitiveDescription(CasCounterProgressLogger.class);
//
//		SimplePipeline.runPipeline(bincas, filter, log);
//	}

	public static void main(String[] args) throws IOException
	{
		List<String> files = new ArrayList<String>();
		int i = 0;
		for (File file : FileUtils.listFiles(new File(base), new String[] { "csv" }, true)) {
			String text = FileUtils.readFileToString(file, "UTF-8");
			files.add(StringUtils.substringBeforeLast(file.getName(), ".") + ".xml");
			if (StringUtils.containsAny(text, "‘’")) {
				files.remove(StringUtils.substringBeforeLast(file.getName(), ".") + ".xml");
			}
			i++;
			if (i % 100 == 0) {
				System.out.println("ok:"+i);
			}
		}
		
		FileUtils.writeLines(new File("D:\\hadoop\\output\\BNC_new\\exclusions.txt"), "UTF-8", files);
	}
}
