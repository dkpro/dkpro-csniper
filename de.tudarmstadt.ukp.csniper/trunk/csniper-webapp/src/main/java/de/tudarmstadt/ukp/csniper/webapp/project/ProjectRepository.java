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
package de.tudarmstadt.ukp.csniper.webapp.project;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.AdditionalColumn;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.project.model.AnnotationType;
import de.tudarmstadt.ukp.csniper.webapp.project.model.Project;

public class ProjectRepository
{
	private Log log = LogFactory.getLog(getClass());

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public List<Project> listProjects()
	{
		return entityManager.createQuery("FROM Project", Project.class).getResultList();
	}

	@Transactional
	public Project readProject(long aId)
	{
		return entityManager.find(Project.class, aId);
	}

	@Transactional
	public Project writeProject(Project aProject)
	{
		// Only need to persist if it has not been saved to the DB yet. If it already has been
		// saved (ID != null), JPA will automatically take care of flushing changes to the DB.
		if (aProject.getId() == 0) {
			entityManager.persist(aProject);
			return aProject;
		}
		else {
			return entityManager.merge(aProject);
		}
	}

	@Transactional
	public List<AnnotationType> listAnnotationTypes()
	{
		return entityManager.createQuery("FROM AnnotationType", AnnotationType.class)
				.getResultList();
	}

    @Transactional
	public AnnotationType readAnnotationType(String aName)
	{
		try {
			return entityManager
					.createQuery("FROM AnnotationType WHERE name = :name", AnnotationType.class)
					.setParameter("name", aName).getSingleResult();
		}
		catch (NoResultException e) {
			AnnotationType t = new AnnotationType();
			t.setName(aName);
			return t;
		}
	}

	@Transactional
	public AnnotationType readAnnotationType(long aId)
	{
		return entityManager.find(AnnotationType.class, aId);
	}

	@Transactional
	public AnnotationType writeAnnotationType(AnnotationType aProject)
	{
		if (aProject.getId() == 0) {
			entityManager.persist(aProject);
			return aProject;
		}
		else {
			return entityManager.merge(aProject);
		}
	}

	/**
	 * For cancel operations, to detach an object which would otherwise still be stored in the
	 * persistence context.
	 */
	@Transactional
	public void refreshEntity(Object aEntity)
	{
		entityManager.refresh(aEntity);
	}

    @Transactional
	public int countEntriesWithAdditionalColumn(String aUserId, AnnotationType aType,
			AdditionalColumn aAdditionalColumn)
	{
		int count = 0;
		// TODO this is kind of ugly
		List<String> query = new ArrayList<String>();
		query.add("FROM EvaluationResult AS er");
		query.add("WHERE userId = :userId");
		query.add("AND er.item.type = :type");
		query.add("AND er.additionalColumns.size > 0");
		List<EvaluationResult> results = entityManager
				.createQuery(StringUtils.join(query, " "), EvaluationResult.class)
				.setParameter("userId", aUserId).setParameter("type", aType.getName())
				.getResultList();
		for (EvaluationResult r : results) {
			if (r.getAdditionalColumns().containsKey(aAdditionalColumn)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Gets the maximum column length.<br>
	 * Why is this method located here? For convenience reasons - we already have access to
	 * projectRepository on the relevant pages (EvaluationPage, AnnotationTypePage).
	 * 
	 * @param aColumn
	 *            the column for which the maximum length shall be returned
	 * @return the maximum length of the specified column in the specified table
	 */
    @Transactional
	public int getDbColumnLength(String aEntityName, String aColumn)
	{
		BigInteger length = new BigInteger("255");

		List<String> query = new ArrayList<String>();
		query.add("SELECT CHARACTER_MAXIMUM_LENGTH");
		query.add("FROM INFORMATION_SCHEMA.COLUMNS");
		query.add("WHERE table_schema = 'csniper'");
		query.add("AND table_name = '" + aEntityName + "'");
		query.add("AND column_name = '" + aColumn + "'");

		try {
			length = (BigInteger) entityManager.createNativeQuery(StringUtils.join(query, " "))
					.getSingleResult();
		}
		catch (NoResultException e) {
			// log.debug("No results for query: " + StringUtils.join(query, " "));
		}

		if (length.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
			return Integer.MAX_VALUE;
		}
		else {
			return length.intValue();
		}
	}
}
