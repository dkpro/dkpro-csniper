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
package de.tudarmstadt.ukp.csniper.webapp.support.wicket;

import org.apache.wicket.validation.validator.StringValidator;

import de.tudarmstadt.ukp.csniper.webapp.project.ProjectRepository;

/**
 * StringValidator which uses the actual column length of a DB column as the maximum length of the
 * component its used in.
 * 
 * @author Erik-Lân Do Dinh
 * 
 */
public class DbFieldMaxLengthValidator
	extends StringValidator
{
	private static final long serialVersionUID = 1L;

	public DbFieldMaxLengthValidator(ProjectRepository aRepository, String aTable, String aColumn)
	{
		super(0, aRepository.getDbColumnLength(aTable, aColumn));
	}
}
