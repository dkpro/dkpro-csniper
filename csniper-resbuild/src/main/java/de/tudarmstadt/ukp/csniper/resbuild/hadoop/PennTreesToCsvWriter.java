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
package de.tudarmstadt.ukp.csniper.resbuild.hadoop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import au.com.bytecode.opencsv.CSVWriter;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;

/**
 * Write a .csv file for each CAS.
 * Each .csv file consists of the PennTrees found in the CAS, one line per tree, format:
 * collectionId,documentId,beginOffset,endOffset,penntree
 * 
 * @author Erik-Lân Do Dinh
 */
public class PennTreesToCsvWriter
	extends JCasAnnotator_ImplBase
{
	public static final String PARAM_PATH = "outputPath";
	@ConfigurationParameter(name = PARAM_PATH, mandatory = true)
	private File outputPath;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);
		if (!outputPath.exists()) {
			outputPath.mkdirs();
		}
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		DocumentMetaData meta = DocumentMetaData.get(aJCas);
		CSVWriter writer = null;

		try {
			File out = new File(outputPath, meta.getDocumentId() + ".csv");
			writer = new CSVWriter(new OutputStreamWriter(new FileOutputStream(out), "UTF-8"));
			String[] row = new String[5];

			// write header row
			row[0] = "collectionId";
			row[1] = "documentId";
			row[2] = "beginOffset";
			row[3] = "endOffset";
			row[4] = "penntree";
			writer.writeNext(row);

			// write data rows
			row[0] = meta.getCollectionId();
			row[1] = meta.getDocumentId();
			for (PennTree pt : JCasUtil.select(aJCas, PennTree.class)) {
				row[2] = Integer.toString(pt.getBegin());
				row[3] = Integer.toString(pt.getEnd());
				row[4] = normalizeSpace(pt.getPennTree());
				writer.writeNext(row);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			IOUtils.closeQuietly(writer);
		}
	}

	/**
	 * Taken from commons-lang:2.6
	 */
    private String normalizeSpace(String str) {
        str = StringUtils.strip(str);
        if(str == null || str.length() <= 2) {
            return str;
        }
        StrBuilder b = new StrBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isWhitespace(c)) {
                if (i > 0 && !Character.isWhitespace(str.charAt(i - 1))) {
                    b.append(' ');
                }
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }
}
