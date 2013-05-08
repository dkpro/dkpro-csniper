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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Richard Eckart de Castilho
 */
public class PennTreeNode
{
	private String label;
	private int nodeStartIndex = Integer.MAX_VALUE;
	private int nodeEndIndex = -1;
	private List<PennTreeNode> children = new ArrayList<PennTreeNode>();

	public String getLabel()
	{
		return label;
	}

	public void setLabel(String aLabel)
	{
		label = aLabel;
	}

	public List<PennTreeNode> getChildren()
	{
		return children;
	}

	public void setChildren(List<PennTreeNode> aChildren)
	{
		children = aChildren;
		for (PennTreeNode child : children) {
			setIndices(child);
		}
	}

	public void addChild(PennTreeNode aNode)
	{
		children.add(aNode);
		setIndices(aNode);
	}

	public boolean isTerminal()
	{
		return children.isEmpty();
	}

	public String getCoveredText()
	{
		if (isTerminal()) {
			return label;
		}
		else {
			StringBuilder coveredText = new StringBuilder();
			for (PennTreeNode child : children) {
				coveredText.append(child.getCoveredText());
			}
			return coveredText.toString();
		}
	}

	public int getNodeStartIndex()
	{
		return nodeStartIndex;
	}

	public int getNodeEndIndex()
	{
		return nodeEndIndex;
	}

	private void setIndices(PennTreeNode aNode)
	{
		if (aNode.nodeStartIndex < nodeStartIndex) {
			nodeStartIndex = aNode.nodeStartIndex;
		}
		if (aNode.nodeEndIndex > nodeEndIndex) {
			nodeEndIndex = aNode.nodeEndIndex;
		}
	}

	@Override
	public String toString()
	{
		return PennTreeUtils.toPennTree(this);
	}

	public void insertIndexedNode(int aNodeStartIndex, int aNodeEndIndex, String aLabel)
		throws IllegalArgumentException
	{
		for (PennTreeNode child : children) {
			if (child.nodeStartIndex > aNodeStartIndex) {
				continue;
			}
			else if (child.nodeEndIndex < aNodeEndIndex) {
				continue;
			}
			else if (child.isTerminal()) {
				break;
			}
			else {
				child.insertIndexedNode(aNodeStartIndex, aNodeEndIndex, aLabel);
				return;
			}
		}
		// if it's not included in any child, but in this node, gather all children who belong
		int childStartIndex = -1;
		int childrenEndIndex = -1;
		for (int i = 0; i < children.size(); i++) {
			PennTreeNode child = children.get(i);
			if (childStartIndex == -1) {
				if (child.nodeStartIndex != aNodeStartIndex) {
					continue;
				}
				else {
					childStartIndex = i;
				}
			}
			if (child.nodeEndIndex != aNodeEndIndex) {
				continue;
			}
			else {
				// sublist works [inclusive, exclusive], so we have to increment endIndex by one
				childrenEndIndex = i + 1;
				List<PennTreeNode> newChildren = new ArrayList<PennTreeNode>(children.subList(
						childStartIndex, childrenEndIndex));
				children.subList(childStartIndex, childrenEndIndex).clear();
				PennTreeNode newNode = new PennTreeNode();
				newNode.setLabel(aLabel);
				newNode.setChildren(newChildren);
				children.add(childStartIndex, newNode);
				return;
			}
		}
		throw new IllegalArgumentException("The Label [" + aLabel
				+ "]  you are trying to insert from [" + aNodeStartIndex + "] to [" + aNodeEndIndex
				+ "] violates Constituent borders.");
	}

	public void setTokenIndex(int aI)
	{
		nodeStartIndex = aI;
		nodeEndIndex = aI;
	}
}
