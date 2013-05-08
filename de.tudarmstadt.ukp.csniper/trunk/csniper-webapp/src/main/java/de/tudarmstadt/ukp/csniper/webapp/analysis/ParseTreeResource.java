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
package de.tudarmstadt.ukp.csniper.webapp.analysis;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.KEY_STROKE_CONTROL;
import static java.awt.RenderingHints.KEY_TEXT_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.RenderingHints.VALUE_RENDER_QUALITY;
import static java.awt.RenderingHints.VALUE_STROKE_PURE;
import static java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
import static org.uimafit.util.CasUtil.getType;
import static org.uimafit.util.CasUtil.select;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.util.time.Time;
import org.uimafit.util.JCasUtil;

import annis.frontend.servlets.visualizers.Visualizer;
import annis.frontend.servlets.visualizers.tree.TigerTreeVisualizer;
import annis.model.AnnisNode;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.model.Edge.EdgeType;
import de.tudarmstadt.ukp.csniper.webapp.search.tgrep.PennTreeNode;
import de.tudarmstadt.ukp.csniper.webapp.search.tgrep.PennTreeUtils;
import de.tudarmstadt.ukp.csniper.webapp.support.uima.CasHolder;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.Constituent;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.constituent.ROOT;
import de.tudarmstadt.ukp.csniper.treevisualizer.AnnisResultIncompleteImpl;

public class ParseTreeResource
	extends DynamicImageResource
{
	private static final long serialVersionUID = -7078485350723019878L;

	private static final String NAMESPACE = "testrat";

	transient private Map<Annotation, AnnisNode> nodes;
	private final CasHolder casHolder;
	private final String pennTree;
	private int currentParse = 1;

	public ParseTreeResource(CasHolder aCas)
	{
		casHolder = aCas;
		pennTree = null;
	}
	
	public ParseTreeResource(String aPennTree)
	{
		casHolder = null;
		pennTree = aPennTree;
	}

	public void setCurrentParse(int aCurrentParse)
	{
		if ((aCurrentParse <= getMaxParse()) && (aCurrentParse > 0)) {
			currentParse = aCurrentParse;
		}
	}

	public int getCurrentParse()
	{
		if (getMaxParse() == 0) {
			return 0;
		}
		else {
			return currentParse;
		}
	}

	public int getMaxParse()
	{
		if (casHolder != null && casHolder.getCas() != null) {
			return select(casHolder.getCas(), getType(casHolder.getCas(), Sentence.class)).size();
		}
		else if (pennTree != null) {
			return 1;
		}
		else {
			return 0;
		}
	}

	@Override
	protected byte[] getImageData(Attributes aArg0)
	{
		setLastModifiedTime(Time.now());
		if ((casHolder == null || casHolder.getCas() == null) && pennTree == null) {
			BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
			Graphics2D canvas = (Graphics2D) image.getGraphics();
			canvas.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
			canvas.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
			canvas.setRenderingHint(KEY_RENDERING, VALUE_RENDER_QUALITY);
			canvas.setRenderingHint(KEY_STROKE_CONTROL, VALUE_STROKE_PURE);
			canvas.drawString("No data", 0, 0);
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ImageIO.write(image, "png", bos);
				return bos.toByteArray();
			}
			catch (IOException e) {
				return new byte[0];
			}
		}
		else {
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				Visualizer ttv = new TigerTreeVisualizer();
				if (pennTree != null) {
					ttv.setResult(new AnnisResultIncompleteImpl(createGraphFromPennTree(pennTree)));
				}
				else {
					ttv.setResult(new AnnisResultIncompleteImpl(createGraph(casHolder.getCas()
							.getJCas())));
				}
				ttv.setMappings(new Properties());
				ttv.setNamespace(NAMESPACE);
				ttv.writeOutput(bos);
				return bos.toByteArray();
			}
			catch (CASException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private AnnotationGraph createGraph(JCas aJCas)
	{
		// Fetch parse tree
		ROOT root = JCasUtil.selectByIndex(aJCas, ROOT.class, currentParse - 1);

		// Build model
		nodes = new HashMap<Annotation, AnnisNode>();
		AnnisNode rootV = getAnnisNode(aJCas, null, root);
		nodes.put(root, rootV);
		traverse(aJCas, root);

		// Populate graph
		AnnotationGraph graph = new AnnotationGraph();
		for (AnnisNode node : nodes.values()) {
			graph.addNode(node);
		}

		return graph;
	}

	private void traverse(JCas aJCas, Constituent parent)
	{
		FSArray children = parent.getChildren();

		AnnisNode parentNode = getAnnisNode(aJCas, null, parent);
		annis.model.Annotation nodeAnno = new annis.model.Annotation(NAMESPACE, "cat",
				parent.getConstituentType());
		parentNode.addNodeAnnotation(nodeAnno);
		nodes.put(parent, parentNode);

		for (int i = 0; i < children.size(); i++) {
			Annotation child = parent.getChildren(i);

			// create/save node
			AnnisNode childNode = getAnnisNode(aJCas, parentNode, child);
			nodes.put(child, childNode);

			// traverse deeper
			if (child instanceof Constituent) {
				traverse(aJCas, (Constituent) child);
			}
		}
	}

	private AnnisNode getAnnisNode(JCas aJCas, AnnisNode aParentNode, Annotation aChild)
	{
		AnnisNode parentNode = aParentNode;
		AnnisNode node = nodes.get(aChild);
		if (node == null) {
			long id = aChild.hashCode();
			node = new AnnisNode(id);
			node.setRoot(aChild.getClass() == ROOT.class);
			node.setNamespace(NAMESPACE);
			node.setSpannedText(aChild.getCoveredText());
			if (aChild instanceof Constituent) {
				Constituent constituent = (Constituent) aChild;
				node.setToken(false);

				// create/save edge
				if (parentNode != null) {
					Edge edge = createEdge(parentNode, node);
					String synFunc = constituent.getSyntacticFunction();
					edge.addAnnotation(new annis.model.Annotation(NAMESPACE, "func", synFunc));
				}
			}
			else if (aChild instanceof Token) {
				Token token = (Token) aChild;
				if (token.getPos() != null && token.getPos().getPosValue() != null) {
					AnnisNode posNode = new AnnisNode(token.getPos().hashCode());
					posNode.setRoot(false);
					posNode.setToken(false);
					posNode.setNamespace(NAMESPACE);
					posNode.setSpannedText(aChild.getCoveredText());
					posNode.addNodeAnnotation(new annis.model.Annotation(NAMESPACE, "cat", token
							.getPos().getPosValue()));

					if (parentNode != null) {
						createEdge(parentNode, posNode);
					}
					parentNode = posNode;
				}

				node.setToken(true);
				Long idx = (long) JCasUtil.selectCovered(aJCas, Token.class, 0, aChild.getBegin())
						.size();
				node.setTokenIndex(idx);
				if (parentNode != null) {
					createEdge(parentNode, node);
				}
			}
		}
		return node;
	}

	private Edge createEdge(AnnisNode parentNode, AnnisNode childNode)
	{
		// create edge
		Edge edge = new Edge();
		edge.setName("edge");
		edge.setSource(parentNode);
		edge.setDestination(childNode);
		edge.setEdgeType(EdgeType.DOMINANCE);

		// add edges to nodes
		parentNode.addOutgoingEdge(edge);
		childNode.addIncomingEdge(edge);

		return edge;
	}

	// ----

	private AnnotationGraph createGraphFromPennTree(String aTree)
	{
		// Fetch parse tree
		PennTreeNode root = PennTreeUtils.parsePennTree(aTree);

		// Build model
		List<AnnisNode> nodes = traverse(null, root);

		// Populate graph
		AnnotationGraph graph = new AnnotationGraph();
		for (AnnisNode node : nodes) {
			graph.addNode(node);
		}

		return graph;
	}

	private List<AnnisNode> traverse(AnnisNode aParent, PennTreeNode aChild)
	{
		List<AnnisNode> nodes = new ArrayList<AnnisNode>();

		AnnisNode node = new AnnisNode(aChild.hashCode());
		annis.model.Annotation nodeAnno = new annis.model.Annotation(NAMESPACE, "cat",
				aChild.getLabel());
		node.addNodeAnnotation(nodeAnno);
		node.setNamespace(NAMESPACE);
		node.setSpannedText(aChild.getCoveredText());

		if (aParent == null) {
			node.setRoot(true);
		}
		else {
			node.setRoot(false);
			createEdge(aParent, node);
		}
		if (aChild.isTerminal()) {
			node.setToken(true);
			node.setTokenIndex((long) aChild.getNodeStartIndex());
		}
		else {
			node.setToken(false);
		}
		nodes.add(node);

		for (PennTreeNode grandChild : aChild.getChildren()) {
			nodes.addAll(traverse(node, grandChild));
		}

		return nodes;
	}
}
