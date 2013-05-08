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

import java.util.List;
import java.util.Set;

import annis.model.AnnotationGraph;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisToken;

public class AnnisResultIncompleteImpl
	implements AnnisResult
{
	private AnnotationGraph graph;

	public AnnisResultIncompleteImpl(AnnotationGraph graph)
	{
		setGraph(graph);
	}

	public void setGraph(AnnotationGraph graph)
	{
		this.graph = graph;
	}

	@Override
	public String getPaula()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long getEndNodeId()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long getStartNodeId()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> getAnnotationLevelSet()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> getTokenAnnotationLevelSet()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getMarkerId(Long nodeId)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasMarker(String markerId)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public AnnotationGraph getGraph()
	{
		return graph;
	}

	@Override
	public List<AnnisToken> getTokenList()
	{
		throw new UnsupportedOperationException();
	}

}
