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

import java.util.Iterator;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.commons.lang.mutable.MutableInt;

/**
 * @author Richard Eckart de Castilho
 */
public class PennTreeUtils
{
    public static String toText(String aTree)
    {
        return toText(parsePennTree(aTree));
    }
    
    public static String toText(PennTreeNode aNode)
    {
        StringBuilder buf = new StringBuilder();
        toText(buf, aNode);
        return buf.toString();
    }
    
    private static void toText(StringBuilder aBuffer, PennTreeNode aNode)
    {
        if (aNode.isTerminal()) {
            if (aBuffer.length() > 0) {
                aBuffer.append(" ");
            }
            String label = aNode.getLabel();
            if ("-LRB-".equals(label)) {
                aBuffer.append("(");
            }
            else if ("-RRB-".equals(label)) {
                aBuffer.append(")");
            }
            else {
                aBuffer.append(label);
            }
        }
        else {
            for (PennTreeNode n : aNode.getChildren()) {
                toText(aBuffer, n);
            }
        }
    }
    
	public static PennTreeNode selectDfs(PennTreeNode aNode, int aIndex)
	{
		return dfs(aIndex, new MutableInt(0), aNode);
	}
	
	private static PennTreeNode dfs(int aTarget, MutableInt aIndex, PennTreeNode aNode)
	{
		if (aTarget == aIndex.intValue()) {
			return aNode;
		}
		
		for (PennTreeNode n : aNode.getChildren()) {
			aIndex.increment();
			PennTreeNode r = dfs(aTarget, aIndex, n);
			if (r != null) {
				return r;
			}
		}
		
		return null;
	}

	public static PennTreeNode parsePennTree(String aTree)
	{
		StringTokenizer st = new StringTokenizer(aTree, "() ", true);
		
		PennTreeNode root = null;
		Stack<PennTreeNode> stack = new Stack<PennTreeNode>();
		boolean seenLabel = false;
		int i = 0;
		while (st.hasMoreTokens()) {
			String t = st.nextToken().trim();
			if (t.length() == 0) {
				// Skip
			}
			else if ("(".equals(t)) {
				PennTreeNode n = new PennTreeNode();
				stack.push(n);
				if (root == null) {
					root = n;
				}
				seenLabel = false;
			}
			else if (")".equals(t)) {
				PennTreeNode n = stack.pop();
				if (!stack.isEmpty()) {
					PennTreeNode p = stack.peek();
					p.addChild(n);
				}
			}
			else if (seenLabel) {
				// If the node has two labels, its a leaf, add a new terminal node then.
				PennTreeNode p = stack.peek();
				PennTreeNode n = new PennTreeNode();
				n.setTokenIndex(i);
				i++;
				n.setLabel(t);
				p.addChild(n);
			}
			else {
				PennTreeNode n = stack.peek();
				n.setLabel(t);
				seenLabel = true;
			}
		}
		
		return root;
	}
	
	public static String toPennTree(PennTreeNode aNode)
	{
		StringBuilder sb = new StringBuilder();
		toPennTree(sb, aNode);
		return sb.toString().trim();
	}
	
	private static void toPennTree(StringBuilder aSb, PennTreeNode aNode)
	{
		if (!aNode.isTerminal()) {
			aSb.append('(');
		}
		
		aSb.append(aNode.getLabel());
		
		if (!aNode.isTerminal()) {
			aSb.append(' ');
			Iterator<PennTreeNode> i = aNode.getChildren().iterator();
			while (i.hasNext()) {
				toPennTree(aSb, i.next());
				if (i.hasNext()) {
					aSb.append(' ');
				}
			}
			aSb.append(')');
		}
	}
}
