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

import static org.uimafit.factory.AnalysisEngineFactory.createPrimitiveDescription;
import static org.uimafit.factory.CollectionReaderFactory.createCollectionReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.impl.CASCompleteSerializer;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.tcas.Annotation;
import org.junit.Test;
import org.tukaani.xz.XZInputStream;
import org.uimafit.pipeline.SimplePipeline;
import org.uimafit.util.CasUtil;
import org.uimafit.util.JCasUtil;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.PennTree;
import de.tudarmstadt.ukp.dkpro.core.io.bincas.SerializedCasReader;
import de.tudarmstadt.ukp.dkpro.core.io.tgrep.TGrepWriter;

public class CasFlusher
	extends CasAnnotator_ImplBase
{
	public static void flush(File aSerializedCas, OutputStream aOutputStream, int aBegin, int aEnd)
		throws IOException
	{
		CAS cas = new CASImpl();

		InputStream is = null;
		try {
			is = new FileInputStream(aSerializedCas);
			if (aSerializedCas.getName().endsWith(".gz")) {
				is = new GZIPInputStream(is);
			}
			else if (aSerializedCas.getName().endsWith(".xz")) {
				is = new XZInputStream(is);
			}
			is = new ObjectInputStream(new BufferedInputStream(is));
			CASCompleteSerializer serializer = (CASCompleteSerializer) ((ObjectInputStream) is)
					.readObject();

			((CASImpl) cas).reinit(serializer);
		}
		catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
		finally {
			IOUtils.closeQuietly(is);
		}

		Collection<AnnotationFS> annos;
		if (aBegin > -1 && aEnd > -1) {
			annos = CasUtil
					.selectCovered(cas, CasUtil.getType(cas, Annotation.class), aBegin, aEnd);
		}
		else {
			annos = CasUtil.selectAll(cas);
		}
		for (AnnotationFS anno : annos) {
			StringBuilder sb = new StringBuilder();
			sb.append("[" + anno.getClass().getSimpleName() + "] ");
			sb.append("(" + anno.getBegin() + "," + anno.getEnd() + ") ");
			sb.append(anno.getCoveredText() + "\n");
			IOUtils.write(sb, aOutputStream, "UTF-8");
		}
	}

	@Test
	public void test()
		throws IOException, UIMAException
	{
//		File source = new File("D:\\hadoop\\output\\BNC\\serialized\\A\\A6\\A63.xml.ser.xz");
//		flush(source, System.out, 29976, 30073);
		
		CollectionReader bincas = createCollectionReader(SerializedCasReader.class,
//				SerializedCasReader.PARAM_PATH, "D:\\ukp\\data\\output\\BNC\\serialized\\A\\A0",
//				SerializedCasReader.PARAM_PATH, "D:\\hadoop\\output\\BNC\\serialized\\A\\A0",
				SerializedCasReader.PARAM_PATH, "D:\\downloads",
//				SerializedCasReader.PARAM_PATTERNS, new String[]{ "[+]**/A02.ser.xz" });
				SerializedCasReader.PARAM_PATTERNS, new String[]{ "[+]**/_user_dodinh_output_uima_output_attempt_201211181809_0459_m_000001_0_BNC_serialized_A_A0_A02.ser.xz" });

		AnalysisEngineDescription flush = createPrimitiveDescription(CasFlusher.class);

		AnalysisEngineDescription tgrep = createPrimitiveDescription(TGrepWriter.class,
				TGrepWriter.PARAM_DROP_MALFORMED_TREES, true,
				TGrepWriter.PARAM_TARGET_LOCATION, "D:\\hadoop",
				TGrepWriter.PARAM_WRITE_COMMENTS, true);

		SimplePipeline.runPipeline(bincas, flush);
	}

	@Override
	public void process(CAS aCas)
		throws AnalysisEngineProcessException
	{
		try {
			int aBegin = 112715;//98877;
			int aEnd = 112734;//98993;
			OutputStream aOutputStream = System.out;
			
			Collection<? extends Annotation> annos;
			annos = JCasUtil.select(aCas.getJCas(), Sentence.class);
			Annotation a = new ArrayList<Annotation>(annos).get(92);
			aBegin = a.getBegin();
			aEnd = a.getEnd();
			if (aBegin > -1 && aEnd > -1) {
				annos = JCasUtil
						.selectCovered(aCas.getJCas(), Annotation.class, aBegin, aEnd);
			}
			else {
				annos = JCasUtil.select(aCas.getJCas(), Annotation.class);
			}
			for (Annotation anno : annos) {
				StringBuilder sb = new StringBuilder();
				sb.append("[" + anno.getClass().getSimpleName() + "] ");
				sb.append("(" + anno.getBegin() + "," + anno.getEnd() + ") ");
				sb.append(anno.getCoveredText() + "\n");
				try {
					IOUtils.write(sb, aOutputStream, "UTF-8");
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			for (PennTree pt : JCasUtil.selectCovered(aCas.getJCas(), PennTree.class, aBegin, aEnd)) {
				IOUtils.write(StringUtils.normalizeSpace(pt.getPennTree()), aOutputStream, "UTF-8");
			}
		}
		catch (CASException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
