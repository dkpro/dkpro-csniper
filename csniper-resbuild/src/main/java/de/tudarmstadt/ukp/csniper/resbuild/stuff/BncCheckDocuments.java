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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class BncCheckDocuments
{
	private static final String BASE = "D:\\ukp\\data\\bnc\\xml";

	public static void main(String[] args)
		throws IOException
	{
		boolean enabled = false;

		for (File f : FileUtils.listFiles(new File(BASE), new String[] { "xml" }, true)) {
			BufferedInputStream bis = null;
			try {
				bis = new BufferedInputStream(new FileInputStream(f));
				int c;
				StringBuilder sb = new StringBuilder();
				while ((c = bis.read()) != '>') {
					if (enabled) {
						if (c == '"') {
							enabled = false;
							break;
						}
						else {
							sb.append((char) c);
						}
					}
					if (c == '"') {
						enabled = true;
					}
				}
				String name = f.getName().substring(0, 3);
				String id = sb.toString();
				if (!name.equals(id)) {
					System.out.println("Name is [" + name + "], but ID is [" + id + "].");
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally {
				IOUtils.closeQuietly(bis);
			}
		}
	}
}
