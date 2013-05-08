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
package de.tudarmstadt.ukp.csniper.webapp.security.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.tudarmstadt.ukp.csniper.webapp.security.model.User;

@Repository
public class UserDaoImpl
	extends AbstractDao<User, String>
	implements UserDao
{
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	@Transactional
	public boolean exists(final String aUsername)
	{
		return entityManager
				.createQuery("FROM " + User.class.getName() + " o WHERE o.username = :username")
				.setParameter("username", aUsername).getResultList().size() > 0;
	}

	@Override
	@Transactional
	public int delete(String aUsername)
	{
		User toDelete = get(aUsername);
		if (toDelete == null) {
			return 0;
		}
		else {
			delete(toDelete);
			return 1;
		}
	}

	@Override
	@Transactional
	public void delete(User aUser)
	{
		entityManager.remove(entityManager.merge(aUser));
	}

	@Override
	@Transactional
	public User get(String aUsername)
	{
		if (!exists(aUsername)) {
			return null;
		}
		return entityManager
				.createQuery("FROM " + User.class.getName() + " o WHERE o.username = :username",
						User.class).setParameter("username", aUsername).getSingleResult();
	}

	@Override
	@Transactional
	public List<User> list()
	{
		return entityManager.createQuery("FROM " + User.class.getName(), User.class)
				.getResultList();
	}

	@Override
	@Transactional
	public List<User> list(User aFilter)
	{
		CriteriaQuery<User> query = queryByExample(aFilter, "username", true);
		return entityManager.createQuery(query).getResultList();
	}

	@Override
	@Transactional
	public List<User> list(User aFilter, int aOffset, int aCount)
	{
		CriteriaQuery<User> query = queryByExample(aFilter, "username", true);
		return entityManager.createQuery(query).setFirstResult(aOffset).setMaxResults(aCount)
				.getResultList();
	}
}
