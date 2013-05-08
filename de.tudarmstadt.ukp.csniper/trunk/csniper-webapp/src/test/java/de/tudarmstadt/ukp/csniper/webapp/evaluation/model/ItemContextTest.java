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
package de.tudarmstadt.ukp.csniper.webapp.evaluation.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.ItemContext;

public class ItemContextTest
{
	@Test
	public void testWithMatch()
	{
		//                       1    1    2    2
		//             0    5    0    5    0    5   
		String text = " left unit match unit right ";
		ItemContext c = new ItemContext(text, 0, text.length(), 6, 21);
		c.setMatch(11, 16);
		assertEquals(" left ", c.getLeft());
		assertEquals("unit ", c.getUnitLeft());
		assertEquals("match", c.getMatch());
		assertEquals(" unit", c.getUnitRight());
		assertEquals(" right ", c.getRight());
	}

	@Test
	public void testWitouthMatch()
	{
		//                       1    1    2    2
		//             0    5    0    5    0    5   
		String text = " left unit match unit right ";
		ItemContext c = new ItemContext(text, 0, text.length(), 6, 21);
		assertEquals(" left ", c.getLeft());
		assertEquals("", c.getUnitLeft());
		assertEquals("unit match unit", c.getMatch());
		assertEquals("", c.getUnitRight());
		assertEquals(" right ", c.getRight());
	}
}
