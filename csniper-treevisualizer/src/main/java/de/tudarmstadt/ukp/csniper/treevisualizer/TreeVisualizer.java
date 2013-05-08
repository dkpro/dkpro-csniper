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
package de.tudarmstadt.ukp.csniper.treevisualizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.util.JCasUtil;

import annis.frontend.servlets.visualizers.Visualizer;
import annis.frontend.servlets.visualizers.tree.TigerTreeVisualizer;
import annis.model.AnnisNode;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.model.Edge.EdgeType;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.parameter.ComponentParameters;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;

/**
 * Draws constituent trees to png files.<br>
 * Requires a parser to be run before.
 * 
 * @author Erik-Lân Do Dinh
 */
public class TreeVisualizer
	extends JCasAnnotator_ImplBase
{
	public static final String PARAM_OUTPUT_PATH = ComponentParameters.PARAM_TARGET_LOCATION;
	@ConfigurationParameter(name = PARAM_OUTPUT_PATH, mandatory = true)
	private File targetLocation;

	public static final String PARAM_NAMESPACE = "namespace";
	@ConfigurationParameter(name = PARAM_NAMESPACE, mandatory = true)
	private String namespace;

	private Log log = LogFactory.getLog(getClass());

	private Map<Annotation, AnnisNode> nodes;
	private List<Edge> edges;
	private JCas jcas;

	@Override
	public void initialize(UimaContext aContext)
		throws ResourceInitializationException
	{
		super.initialize(aContext);

		try {
			FileUtils.forceMkdir(targetLocation);
		}
		catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(JCas aJCas)
		throws AnalysisEngineProcessException
	{
		jcas = aJCas;
		OutputStream os = null;
		int i = 0;

		for (ROOT root : JCasUtil.select(jcas, ROOT.class)) {
			try {
				DocumentMetaData meta = DocumentMetaData.get(aJCas);
				File image = new File(targetLocation, meta.getDocumentId() + "_" + i + ".png");

				if (log.isInfoEnabled()) {
					log.info("Started creating image [" + image.getAbsolutePath() + "].");
				}

				os = new FileOutputStream(image);
				Visualizer ttv = new TigerTreeVisualizer();
				ttv.setResult(new AnnisResultIncompleteImpl(createGraph(root)));
				ttv.setMappings(new Properties());
				ttv.setNamespace(namespace);
				ttv.writeOutput(os);

				if (log.isInfoEnabled()) {
					log.info("Finished writing image [" + image.getAbsolutePath() + "].");
				}
			}
			catch (FileNotFoundException e) {
				throw new AnalysisEngineProcessException(e);
			}
			finally {
				i++;
				IOUtils.closeQuietly(os);
			}
		}
	}

	private AnnotationGraph createGraph(ROOT aRoot)
	{
		nodes = new HashMap<Annotation, AnnisNode>();
		edges = new ArrayList<Edge>();

		AnnotationGraph graph = new AnnotationGraph();
		AnnisNode rootV = getAnnisNode(aRoot);
		nodes.put(aRoot, rootV);
		traverse(aRoot);
		for (AnnisNode node : nodes.values()) {
			graph.addNode(node);
		}
		return graph;
	}

	private void traverse(Constituent parent)
	{
		FSArray children = parent.getChildren();

		AnnisNode parentNode = getAnnisNode(parent);
		annis.model.Annotation nodeAnno = new annis.model.Annotation(namespace, "cat",
				parent.getConstituentType());
		parentNode.addNodeAnnotation(nodeAnno);
		nodes.put(parent, parentNode);

		for (int i = 0; i < children.size(); i++) {
			Annotation child = parent.getChildren(i);

			// create/save node
			AnnisNode childNode = getAnnisNode(child);
			nodes.put(child, childNode);

			// create/save edge
			Edge edge = new Edge();
			edge.setName("edge");
			edge.setSource(parentNode);
			edge.setDestination(childNode);
			edge.setEdgeType(EdgeType.DOMINANCE);
			if ((child instanceof Constituent)) {
				// TODO show syntactic functions only if available
				String synFunc = ((Constituent) child).getSyntacticFunction();
				synFunc = synFunc == null ? "#" : synFunc;
				annis.model.Annotation edgeAnno = new annis.model.Annotation(namespace, "func",
						synFunc);
				edge.addAnnotation(edgeAnno);
			}
			edges.add(edge);

			// add edges to nodes
			parentNode.addOutgoingEdge(edge);
			childNode.addIncomingEdge(edge);

			// traverse deeper
			if (child instanceof Constituent) {
				traverse((Constituent) child);
			}
		}
	}

	private AnnisNode getAnnisNode(Annotation a)
	{
		AnnisNode node = nodes.get(a);
		if (node == null) {
			long id = a.hashCode();
			node = new AnnisNode(id);
			node.setRoot(a.getClass() == ROOT.class);
			node.setNamespace(namespace);
			node.setSpannedText(a.getCoveredText());
			if (a instanceof Constituent) {
				node.setToken(false);
			}
			else {
				node.setToken(true);
				Annotation dummy = new Annotation(jcas, 0, a.getBegin());
				Long idx = (long) JCasUtil.selectCovered(jcas, Token.class, dummy).size();
				node.setTokenIndex(idx);
			}
		}
		return node;
	}
}
