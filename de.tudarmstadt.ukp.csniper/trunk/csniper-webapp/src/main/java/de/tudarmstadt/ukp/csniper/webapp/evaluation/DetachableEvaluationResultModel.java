/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.tudarmstadt.ukp.csniper.webapp.evaluation;

import org.apache.wicket.model.LoadableDetachableModel;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationResult;

/**
 * detachable model for an instance of contact
 * 
 * @author igor
 * 
 */
public class DetachableEvaluationResultModel extends LoadableDetachableModel<EvaluationResult>
{
	private final long id;
	private EvaluationResult result;
	
	/**
	 * @param c
	 */
	public DetachableEvaluationResultModel(EvaluationResult aResult)
	{
		this(aResult.getId());
		result = aResult;
	}

	/**
	 * @param id
	 */
	private DetachableEvaluationResultModel(long id)
	{
		if (id == 0)
		{
//			throw new IllegalArgumentException();
		}
		this.id = id;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return Long.valueOf(id).hashCode();
	}

	/**
	 * used for dataview with ReuseIfModelsEqualStrategy item reuse strategy
	 * 
	 * @see org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		else if (obj == null)
		{
			return false;
		}
		else if (obj instanceof DetachableEvaluationResultModel)
		{
			DetachableEvaluationResultModel other = (DetachableEvaluationResultModel)obj;
//			return other.id == id;
			return other.result.equals(result);
		}
		return false;
	}

	/**
	 * @see org.apache.wicket.model.LoadableDetachableModel#load()
	 */
	@Override
	public EvaluationResult load()
	{
		return result;
	}
}
