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
package de.tudarmstadt.ukp.csniper.webapp.analysis.uima;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.component.CasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.resource.ResourceInitializationException;
import org.dom4j.io.SAXContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * A simple consumer which produces an html-file. Given annotation types are marked in given colors.
 * 
 * @author Erik-Lân Do Dinh
 * 
 */
public class HTMLColorMarkerConsumer
	extends CasAnnotator_ImplBase
{

	public static final String PARAM_MARKED_TYPES = "MarkedTypes";
	@ConfigurationParameter(name = PARAM_MARKED_TYPES, mandatory = true)
	private String[] markedTypes;

	public static final String PARAM_MARKER_COLORS = "MarkerColors";
	@ConfigurationParameter(name = PARAM_MARKER_COLORS, mandatory = true)
	private String[] markerColors;

	public static final String PARAM_OUTPUT_FILE = "OutputFile";
	@ConfigurationParameter(name = PARAM_OUTPUT_FILE, mandatory = true)
	private File outputFile;

	public static final String PARAM_ENCODING = "Encoding";
	@ConfigurationParameter(name = PARAM_ENCODING, mandatory = true, defaultValue = "UTF-8")
	private String encoding;

	public static final String PARAM_CONTAINER_CSS_CLASS = "OutputClass";
	@ConfigurationParameter(name = PARAM_CONTAINER_CSS_CLASS, mandatory = true, defaultValue = "markedOutput")
	private String outputClass;

	private SAXContentHandler handler;
	private Map<String, String> colors;
	private Type tokenType, rootType;
	private int typeCount;

	@Override
	public void typeSystemInit(TypeSystem ts)
	{
		tokenType = ts.getType("de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token");
		rootType = ts.getType("de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT");
		// TODO use sentence type instead; blocked by a bug mpst probably in the tsurgeon AE
		// sentenceType =
		// ts.getType("de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence");
	}

	@Override
	public void initialize(UimaContext context)
		throws ResourceInitializationException
	{
		super.initialize(context);

		// TODO notify user that the number of arguments is not equal
		// if (markedAnnotations.length != markerColors.length) {
		// throw new IllegalArgumentException(
		// "The amount of colors and annotation types have to be equal.");
		// }
		typeCount = Math.min(markerColors.length, markedTypes.length);

		// build map: type->color
		colors = new HashMap<String, String>();
		for (int i = 0; i < typeCount; i++) {
			colors.put(markedTypes[i], markerColors[i]);
		}

		handler = new SAXContentHandler();
		try {
			// add stylesheet information
			handler.startDocument();
			AttributesImpl attr = new AttributesImpl();
			attr.addAttribute("", "", "id", "CDATA", outputClass);
			handler.startElement("", "div", "", attr);

			AttributesImpl style = new AttributesImpl();
			style.addAttribute("", "", "type", "CDATA", "text/css");
			handler.startElement("", "style", "", style);

			StringBuffer headerCss = new StringBuffer();
			headerCss.append("div#");
			headerCss.append(outputClass);
			headerCss.append(" span {");
			headerCss.append(" display:inline-block; padding:0 1 0 1;");
			headerCss.append(" margin:1px; border:solid 1px #FFFFFF; }");

			handler.characters(headerCss.toString().toCharArray(), 0, headerCss.toString().length());
			handler.endElement("", "style", "");
		}
		catch (SAXException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(CAS cas)
		throws AnalysisEngineProcessException
	{
		List<AnnotationFS> filtered = new ArrayList<AnnotationFS>();
		Iterator<CAS> viewIterator = cas.getViewIterator();
		while (viewIterator.hasNext()) {
			CAS view = viewIterator.next();

			for (int i = 0; i < typeCount; i++) {
				try {
					Type type = CasUtil.getType(view, markedTypes[i]);
					filtered.addAll(CasUtil.select(view, type));
				}
				catch (IllegalArgumentException e) {
					// TODO at the moment, don't do anything when a type is not found
				}
			}

			try {
				for (AnnotationFS root : CasUtil.select(view, rootType)) {
					for (AnnotationFS token : CasUtil.selectCovered(view, tokenType, root)) {
						for (AnnotationFS a : getAnnotationsBeginningAt(token.getBegin(), filtered)) {
							String color = "background:" + colors.get(a.getType().getName())
									+ "; color:#DDDDDD;";
							AttributesImpl attr = new AttributesImpl();
							attr.addAttribute("", "", "style", "CDATA", color);
							handler.startElement("", "span", "", attr);
						}
						char[] t = token.getCoveredText().toCharArray();
						handler.characters(t, 0, t.length);
						for (AnnotationFS a : getAnnotationsEndingAt(token.getEnd(), filtered)) {
							handler.endElement("", "span", "");
						}
						handler.characters(new char[] { ' ' }, 0, 1);
					}
					// newline for each sentence
					handler.startElement("", "br", "", new AttributesImpl());
					handler.endElement("", "br", "");
				}
			}
			catch (SAXException e) {
				throw new AnalysisEngineProcessException(e);
			}
		}
	}

	/**
	 * Get annotations which start at a specified index.
	 * 
	 * @param i
	 *            the index where an annotation should start.
	 * @param filtered
	 *            the List to take the Annotations from.
	 * @return a List of AnnotationFS contained in filtered which start at index i.
	 */
	private List<AnnotationFS> getAnnotationsBeginningAt(int i, List<AnnotationFS> filtered)
	{
		List<AnnotationFS> annotations = new ArrayList<AnnotationFS>();
		for (AnnotationFS a : filtered) {
			if (a.getBegin() == i) {
				annotations.add(a);
			}
		}
		return annotations;
	}

	/**
	 * Get annotations which end at a specified index.
	 * 
	 * @param i
	 *            the index where an annotation should end.
	 * @param filtered
	 *            the List to take the Annotations from.
	 * @returna a List of AnnotationFS contained in filtered which end at index i.
	 */
	private List<AnnotationFS> getAnnotationsEndingAt(int i, List<AnnotationFS> filtered)
	{
		List<AnnotationFS> annotations = new ArrayList<AnnotationFS>();
		for (AnnotationFS a : filtered) {
			if (a.getEnd() == i) {
				annotations.add(a);
			}
		}
		return annotations;
	}

	@Override
	public void collectionProcessComplete()
	{
		try {
			handler.endElement("", "div", "");
			handler.endDocument();
			String xml = handler.getDocument().asXML();
			FileUtils.writeStringToFile(outputFile, xml, encoding);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
