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

import org.apache.commons.lang.StringUtils;

public enum Mark
{
	NA(""), CORRECT("Correct"), WRONG("Wrong"), CHECK("Check"), PRED_CORRECT("(Correct)"), PRED_WRONG("(Wrong)");

	private String title;

	private Mark(String aTitle)
	{
		title = aTitle;
	}

	public Mark next()
	{
		switch(this) {
		case NA:
		case PRED_CORRECT:
			return CORRECT;
		case CORRECT:
		case PRED_WRONG:
			return WRONG;
		case WRONG:
			return CHECK;
		case CHECK:
			return NA;
		default:
			return null;
		}
	}

	public static Mark fromString(String aTitle)
	{
		if (StringUtils.isBlank(aTitle)) {
			return NA;
		}
		else {
			aTitle = aTitle.toLowerCase();
			if (aTitle.equals("correct")) {
			    return CORRECT;
			}
            else if (aTitle.equals("wrong")) {
                return WRONG;
            }
            else if (aTitle.equals("check")) {
                return CHECK;
            }
            else if (aTitle.equals("(correct)")) {
                return PRED_CORRECT;
            }
            else if (aTitle.equals("(wrong)")) {
                return PRED_WRONG;
            }
            else {
                throw new IllegalArgumentException("There is no Mark titled [" + aTitle + "].");
            }
		}
	}

	public String getTitle()
	{
		return title;
	}
}
