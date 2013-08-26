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
package de.tudarmstadt.ukp.csniper.webapp.evaluation;

import static java.util.Collections.singletonList;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.ScrollableResults;
import org.hibernate.ejb.HibernateQuery;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;

import bak.pcj.map.LongKeyOpenHashMap;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.SortableEvaluationResultDataProvider.ResultFilter;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.CachedParse;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationItem;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.EvaluationResult;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.Query;
import de.tudarmstadt.ukp.csniper.webapp.evaluation.model.SampleSet;
import de.tudarmstadt.ukp.csniper.webapp.project.model.AnnotationType;
import de.tudarmstadt.ukp.csniper.webapp.statistics.model.AggregatedEvaluationResult;
import de.tudarmstadt.ukp.dkpro.core.api.resources.ResourceUtils;

public class EvaluationRepository
{
    private Log log = LogFactory.getLog(getClass());

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Write the query to the database if it does not exist.
     */
    @Transactional
    public Query recordQuery(String aEngine, String aQuery, String aCollectionId, String aType,
            String aComment, String aUser)
    {
        try {
            // try to fetch the query from db
            return getQuery(aEngine, aQuery, aCollectionId, aType, aUser);
        }
        catch (NoResultException e) {
            // if the query is not in db, write it
            Query q = new Query(aEngine, aQuery, aCollectionId, aType, aUser);
            q.setComment(aComment);
            writeQuery(q);
            return q;
        }
    }

    @Transactional
    public Query getQuery(String aEngine, String aQuery, String aCollectionId, String aType,
            String aUser)
    {
        return entityManager
                .createQuery(
                        "FROM Query WHERE engine = :engine AND query = :query AND collectionId = :collectionId AND type = :type AND userId = :userId",
                        Query.class).setParameter("engine", aEngine).setParameter("query", aQuery)
                .setParameter("collectionId", aCollectionId).setParameter("type", aType)
                .setParameter("userId", aUser).getSingleResult();
    }

    @Transactional
    public void writeQuery(Query aQuery)
    {
        entityManager.persist(aQuery);
    }

    @Transactional
    public List<Query> listQueries()
    {
        return entityManager.createQuery("FROM Query ORDER BY type", Query.class).getResultList();
    }

    @Transactional
    public List<Query> listUniqueQueries()
    {
        return entityManager
                .createQuery(
                        "SELECT DISTINCT new Query(engine, query, collectionId, type) FROM Query ORDER BY type",
                        Query.class).getResultList();
    }

    @Transactional
    public List<Query> listQueries(String aEngine, String aCollectionId, AnnotationType aType,
            String aUser)
    {
        return entityManager
                .createQuery(
                        "FROM Query WHERE engine = :engine AND collectionId = :collectionId AND type = :type AND userId = :userId ORDER BY type",
                        Query.class).setParameter("engine", aEngine)
                .setParameter("collectionId", aCollectionId).setParameter("type", aType.getName())
                .setParameter("userId", aUser).getResultList();
    }

    @Transactional
    public SampleSet recordSampleSet(String aName, String aCollectionId, String aType,
            String aComment, String aUser, List<EvaluationItem> aItems)
    {
        try {
            // try to fetch the sampleset from db
            return getSampleSet(aName, aCollectionId, aType, aUser);
        }
        catch (NoResultException e) {
            // if the sampleset is not in db, write it
            SampleSet s = new SampleSet(aName, aCollectionId, aType, aUser);
            s.setComment(aComment);
            s.setItems(aItems);
            writeSampleSet(s);
            return s;
        }
    }

    @Transactional
    public SampleSet getSampleSet(String aName, String aCollectionId, String aType, String aUser)
    {
        return entityManager
                .createQuery(
                        "FROM SampleSet WHERE name = :name AND collectionId = :collectionId AND type = :type AND userId = :userId",
                        SampleSet.class).setParameter("name", aName)
                .setParameter("collectionId", aCollectionId).setParameter("type", aType)
                .setParameter("userId", aUser).getSingleResult();
    }

    @Transactional
    public CachedParse getCachedParse(EvaluationItem aItem)
    {
        return getCachedParse(aItem.getCollectionId(), aItem.getDocumentId(),
                aItem.getBeginOffset(), aItem.getEndOffset());
    }

    @Transactional
    public CachedParse getCachedParse(String aCollectionId, String aDocumentId, long aBeginOffset,
            long aEndOffset)
    {
        try {
            return entityManager
                    .createQuery(
                            "FROM CachedParse WHERE collectionId = :collectionId AND "
                                    + "documentId = :documentId AND beginOffset = :beginOffset "
                                    + "AND endOffset = :endOffset", CachedParse.class)
                    .setParameter("collectionId", aCollectionId)
                    .setParameter("documentId", aDocumentId)
                    .setParameter("beginOffset", aBeginOffset)
                    .setParameter("endOffset", aEndOffset).getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public int[][] listCachedParsesPages(String aCollectionId, int aPageSize)
    {
        List<int[]> pages = new ArrayList<int[]>();
        ScrollableResults results = null;
        try {
            String queryString = "SELECT id FROM CachedParse WHERE collectionId = :collectionId";
            org.hibernate.Query query = ((HibernateQuery) entityManager.createQuery(queryString)).getHibernateQuery();
            query.setParameter("collectionId", aCollectionId);
            results = query.scroll();
            results.beforeFirst();
            int row = 0;
            int[] curPage = new int[] {-1, -1};
            boolean hasNext = results.next();
            while (hasNext) {
                int id = results.getLong(0).intValue();
                // Record start of page
                if ((row % aPageSize) == 0) {
                    curPage[0] = id;
                }
                
                // Step ahead
                hasNext = results.next();
                row++;
                
                // Record end of page when end of page or end of results is reached
                if (((row % aPageSize) == (aPageSize - 1)) || !hasNext) {
                    curPage[1] = id;
                    pages.add(curPage);
                    curPage = new int[] {-1, -1};
                }
            }
        }
        finally {
            if (results != null) {
                results.close();
            }
        }
        return pages.toArray(new int[pages.size()][2]);
    }
    
    @Transactional
    public List<CachedParse> listCachedParses(String aCollectionId, int aStartId, int aEndId)
    {
        return entityManager
                .createQuery(
                        "FROM CachedParse WHERE collectionId = :collectionId AND id >= :startId AND id < :endId",
                        CachedParse.class).setParameter("collectionId", aCollectionId)
                .setParameter("startId", (long) aStartId).setParameter("endId", (long) aEndId)
                .getResultList();
    }

    @Transactional
    public long getCachedParsesCount(String aCollectionId)
    {
        return entityManager
                .createQuery("SELECT COUNT(*) FROM CachedParse WHERE collectionId = :collectionId",
                        Long.class).setParameter("collectionId", aCollectionId).getSingleResult();
    }

    @Transactional
    public EvaluationItem getEvaluationItem(long aItemId)
    {
        try {
            return entityManager
                    .createQuery("FROM EvaluationItem WHERE id = :itemId", EvaluationItem.class)
                    .setParameter("itemId", aItemId).getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public EvaluationResult getEvaluationResult(long aItemId, String aUserId)
    {
        try {
            return entityManager
                    .createQuery(
                            "FROM EvaluationResult WHERE item.id = :itemId AND userId = :userId",
                            EvaluationResult.class).setParameter("itemId", aItemId)
                    .setParameter("userId", aUserId).getSingleResult();
        }
        catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public void writeSampleSet(SampleSet aSampleSet)
    {
        entityManager.persist(aSampleSet);
    }

    @Transactional
    public void updateSampleSet(SampleSet aSampleset, List<EvaluationItem> aItems)
    {
        aSampleset.addItems(aItems);
        entityManager.merge(aSampleset);
    }

    @Transactional
    public List<SampleSet> listSampleSets()
    {
        return entityManager.createQuery("FROM SampleSet ORDER BY type", SampleSet.class)
                .getResultList();
    }

    @Transactional
    public List<SampleSet> listSampleSets(String aCollectionId, String aType, String aUser)
    {
        return entityManager
                .createQuery(
                        "FROM SampleSet WHERE collectionId = :collectionId AND type = :type AND userId = :userId ORDER BY type",
                        SampleSet.class).setParameter("collectionId", aCollectionId)
                .setParameter("type", aType).setParameter("userId", aUser).getResultList();
    }

    /**
     * Persist the given items. If they already exist in the database, replace the item in the list
     * with the item from the database. Transient data (e.g. match offsets) is preserved.
     * 
     * @param aItems
     */
    @Transactional
    public List<EvaluationItem> writeEvaluationItems(List<EvaluationItem> aItems)
    {
        return writeEvaluationItems(aItems, true);
    }

    /**
     * Persist the given items. If they already exist in the database, replace the item in the list
     * with the item from the database. Transient data (e.g. match offsets) is preserved.
     * 
     * @param aItems
     * @param aCreate
     *            true = missing evaluation items are created and returned; false = missing
     *            evaluation items are not created and returned
     */
    @Transactional
    public List<EvaluationItem> writeEvaluationItems(List<EvaluationItem> aItems, boolean aCreate)
    {
        long start = System.currentTimeMillis();

        log.info("Building index on in-memory items");
        List<EvaluationItem> result = new ArrayList<EvaluationItem>(aItems.size());
        LinkedMultiValueMap<String, EvaluationItem> idx = new LinkedMultiValueMap<String, EvaluationItem>();
        for (EvaluationItem i : aItems) {
            idx.add(i.getCollectionId() + "-" + i.getDocumentId() + "-" + i.getType(), i);
        }

        TypedQuery<EvaluationItem> query = entityManager.createQuery(
                "FROM EvaluationItem WHERE collectionId = :collectionId AND documentId = "
                        + ":documentId AND type = :type", EvaluationItem.class);

        log.info("Merging with in-database items in " + idx.size() + " chunks");
        ProgressMeter progress = new ProgressMeter(idx.size());
        for (List<EvaluationItem> items : idx.values()) {
            progress.next();
            EvaluationItem ref = items.get(0);
            List<EvaluationItem> pItems = query.setParameter("collectionId", ref.getCollectionId())
                    .setParameter("documentId", ref.getDocumentId())
                    .setParameter("type", ref.getType()).getResultList();

            Comparator<EvaluationItem> cmp = new Comparator<EvaluationItem>()
            {
                @Override
                public int compare(EvaluationItem aO1, EvaluationItem aO2)
                {
                    if (aO1.getBeginOffset() > aO2.getBeginOffset()) {
                        return 1;
                    }
                    else if (aO1.getBeginOffset() < aO2.getBeginOffset()) {
                        return -1;
                    }
                    else if (aO1.getEndOffset() > aO2.getEndOffset()) {
                        return 1;
                    }
                    else if (aO1.getEndOffset() < aO2.getEndOffset()) {
                        return -1;
                    }
                    else {
                        return 0;
                    }
                }
            };

            Collections.sort(pItems, cmp);

            for (EvaluationItem item : items) {
                int i = Collections.binarySearch(pItems, item, cmp);
                if (i < 0) {
                    if (aCreate) {
                        entityManager.persist(item);
                        result.add(item);
                    }
                }
                else {
                    EvaluationItem pItem = pItems.get(i);
                    pItem.copyTransientData(item);
                    result.add(pItem);
                }
            }
            
            log.info(progress);
        }

        log.info("writeEvaluationItems for " + aItems.size() + " items completed in "
                + (System.currentTimeMillis() - start) + " ms");

        return result;

        // String query = "FROM EvaluationItem WHERE collectionId = :collectionId AND documentId = "
        // +
        // ":documentId AND type = :type AND beginOffset = :beginOffset AND endOffset = :endOffset";
        // for (ListIterator<EvaluationItem> li = aItems.listIterator(); li.hasNext();) {
        // EvaluationItem item = li.next();
        // try {
        // EvaluationItem pItem = entityManager.createQuery(query, EvaluationItem.class)
        // .setParameter("collectionId", item.getCollectionId())
        // .setParameter("documentId", item.getDocumentId())
        // .setParameter("type", item.getType())
        // .setParameter("beginOffset", item.getBeginOffset())
        // .setParameter("endOffset", item.getEndOffset()).getSingleResult();
        //
        // // if item already exists, use that instead of persisting the new
        // pItem.copyTransientData(item);
        // li.set(pItem);
        // }
        // catch (NoResultException e) {
        // // persist item if not exists
        // if (aCreate) {
        // entityManager.persist(item);
        // }
        // }
        // }
    }

    @Transactional
    public List<EvaluationItem> listEvaluationItems(String aCollectionId, String aType)
    {
        return entityManager
                .createQuery(
                        "FROM EvaluationItem WHERE collectionId = :collectionId AND type = :type",
                        EvaluationItem.class).setParameter("collectionId", aCollectionId)
                .setParameter("type", aType).getResultList();
    }

    @Transactional
    public List<EvaluationItem> listEvaluationItems(List<String> aCollectionIds,
            List<AnnotationType> aTypes)
    {
        List<String> types = new ArrayList<String>();
        for (AnnotationType at : aTypes) {
            types.add(at.getName());
        }
        return entityManager
                .createQuery(
                        "FROM EvaluationItem WHERE collectionId IN :collectionIds AND type IN :types",
                        EvaluationItem.class).setParameter("collectionIds", aCollectionIds)
                .setParameter("types", types).getResultList();
    }

    /**
     * Persist the given results. If they already exist in the database, replace the result in the
     * list with the result from the database. Transient data (e.g. match offsets) is preserved.
     */
    @Transactional
    public void writeEvaluationResults(List<EvaluationResult> aResults)
    {
        long start = System.currentTimeMillis();

        Set<String> users = new HashSet<String>();
        for (EvaluationResult r : aResults) {
            users.add(r.getUserId());
        }

        for (String user : users) {
            // Build index on in-memory results
            log.info("Building index on in-memory results (" + aResults.size() + " results)");
            List<Long> itemIds = new ArrayList<Long>(aResults.size());
            for (EvaluationResult r : aResults) {
                if (user.equals(r.getUserId())) {
                    itemIds.add(r.getItem().getId());
                }
            }

            // Build index on persisted results
            TypedQuery<EvaluationResult> query = entityManager.createQuery(
                    "FROM EvaluationResult WHERE userId = :userId AND item_id in (:itemIds)",
                    EvaluationResult.class);
            query.setParameter("userId", user);
            LongKeyOpenHashMap pResults = new LongKeyOpenHashMap(aResults.size());
            if (itemIds.size() > 0) {
                // Big Performance problem with setParameterList()
                // https://hibernate.onjira.com/browse/HHH-766
                int chunkSize = 500;
                log.info("Fetching in-database results in "
                        + ((aResults.size() / chunkSize) + (aResults.size() % chunkSize == 0 ? 0
                                : 1)) + " chunks");
                for (int i = 0; i < itemIds.size(); i += chunkSize) {
                    query.setParameter("itemIds",
                            itemIds.subList(i, Math.min(i + chunkSize, itemIds.size())));
                    List<EvaluationResult> pr = query.getResultList();
                    for (EvaluationResult r : pr) {
                        pResults.put(r.getItem().getId(), r);
                    }
                }
            }

            // Merge information from the database
            log.info("Merging");
            for (ListIterator<EvaluationResult> li = aResults.listIterator(); li.hasNext();) {
                EvaluationResult mResult = li.next();

                // only replace for current user
                if (!mResult.getUserId().equals(user)) {
                    continue;
                }

                EvaluationResult pResult = (EvaluationResult) pResults.get(mResult.getItem()
                        .getId());
                if (pResult != null) {
                    // if result already exists, use that instead of persisting the new
                    pResult.getItem().copyTransientData(mResult.getItem());
                    li.set(pResult);
                }
                else if (mResult.getId() != 0) {
                    li.set(entityManager.merge(mResult));
                }
                else {
                    // if results does not exist, persist it
                    entityManager.persist(mResult);
                }
            }
        }

        log.info("writeEvaluationResults for " + aResults.size() + " items completed in "
                + (System.currentTimeMillis() - start) + " ms");
    }

    @Transactional
    public void writeCachedParse(CachedParse aParses)
    {
        try {
            entityManager.persist(aParses);
        }
        catch (EntityExistsException e) {
            // well, if it exists, don't persist...
        }
    }

    @Transactional
    public List<EvaluationResult> listEvaluationResults(final String aUserId, final String aType,
            final int start, final int limit, ResultFilter aFilter)
    {
        final StringBuffer query = new StringBuffer();
        query.append("FROM EvaluationResult WHERE userId = ?1 AND item.type = ?2 ");
        if (aFilter == ResultFilter.TODO) {
            query.append("AND length(result) = 0 ");
        }
        else if (aFilter == ResultFilter.ASSESSED) {
            query.append("AND length(result) > 0 ");
        }
        /*
         * query.append("ORDER BY "); query.append(orderBy); query.append(isAscending ? " ASC " :
         * " DESC ");
         */

        return entityManager.createQuery(query.toString(), EvaluationResult.class)
                .setParameter(1, aUserId).setParameter(2, aType).setFirstResult(start)
                .setMaxResults(limit).getResultList();
    }

    @Transactional
    public List<EvaluationResult> listEvaluationResults(String aUserId, String aType,
            ResultFilter aFilter)
    {
        StringBuffer query = new StringBuffer();
        query.append("FROM EvaluationResult ");
        query.append("WHERE userId = :userId ");
        query.append("AND item.type = :type ");
        if (aFilter == ResultFilter.TODO) {
            query.append("AND length(result) = 0 ");
        }
        else if (aFilter == ResultFilter.ASSESSED) {
            query.append("AND length(result) > 0 ");
        }
        return entityManager.createQuery(query.toString(), EvaluationResult.class)
                .setParameter("userId", aUserId).setParameter("type", aType).getResultList();
    }

    private String loadQuery(String aLocation)
    {
        InputStream is = null;
        try {
            is = ResourceUtils.resolveLocation(aLocation, null, null).openStream();
            return IOUtils.toString(is);
        }
        catch (IOException e) {
            throw new DataAccessResourceFailureException("Unable to load query from [" + aLocation
                    + "]", e);
        }
        finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Transactional
    public List<EvaluationResult> listDisputedEvaluationResults(String aCollectionId,
            AnnotationType aType, String aUserId)
    {
        String query = loadQuery("classpath:/AggregatedQuery.sql");

        Collection<String> aUsers = listUsers();

        javax.persistence.Query q = entityManager.createNativeQuery(query);
        q.setParameter("collectionIds", singletonList(aCollectionId));
        q.setParameter("types", singletonList(aType.getName()));
        q.setParameter("users", aUsers);
        q.setParameter("usersL", aUsers.size());
        q.setParameter("userThreshold", 0);
        q.setParameter("confidenceThreshold", 0);

        @SuppressWarnings("unchecked")
        List<Object[]> list = q.getResultList();
        List<EvaluationResult> results = new ArrayList<EvaluationResult>();
        for (Object[] obj : list) {
            long itemId = ((Number) obj[0]).longValue();
            int correct = ((Number) obj[1]).intValue();
            int wrong = ((Number) obj[2]).intValue();

            // Check if there is any dispute. No thresholds here, simple dispute is enough.
            if (!(correct > 0 && wrong > 0)) {
                continue;
            }

            EvaluationResult result = getEvaluationResult(itemId, aUserId);
            if (result != null) {
                results.add(result);
            }
        }

        log.info("Found [" + list.size() + "] results of which [" + results.size()
                + "] are disputed and apply to [" + aUserId + "]");

        return results;
    }

    @Transactional
    public List<EvaluationResult> listEvaluationResults(String aCollectionId, AnnotationType aType,
            String aUserId)
    {
        StringBuffer query = new StringBuffer();
        query.append("FROM EvaluationResult ");
        query.append("WHERE item.collectionId = :collectionId ");
        query.append("AND item.type = :type ");
        query.append("AND userId = :userId ");
        query.append("AND length(result) > 0"); // all non-empty results

        return entityManager.createQuery(query.toString(), EvaluationResult.class)
                .setParameter("collectionId", aCollectionId).setParameter("type", aType.getName())
                .setParameter("userId", aUserId).getResultList();
    }

    @Transactional
    public List<EvaluationResult> listEvaluationResults(String aUserId, SampleSet aSampleSet)
    {
        final StringBuffer query = new StringBuffer();
        query.append("FROM EvaluationResult ");
        query.append("WHERE userId = :userId ");
        query.append("AND item ");
        query.append("IN (SELECT i FROM SampleSet s JOIN s.items i WHERE s.id = :samplesetId)");

        return entityManager.createQuery(query.toString(), EvaluationResult.class)
                .setParameter("userId", aUserId).setParameter("samplesetId", aSampleSet.getId())
                .getResultList();
    }

    @Transactional
    public EvaluationItem updateEvaluationItem(EvaluationItem aItem)
    {
        return entityManager.merge(aItem);
    }

    @Transactional
    public EvaluationResult updateEvaluationResult(EvaluationResult aResult)
    {
        return entityManager.merge(aResult);
    }

    public List<String> listCollections()
    {
        return entityManager.createQuery(
                "SELECT DISTINCT collectionId FROM EvaluationItem ORDER BY collectionId",
                String.class).getResultList();
    }

    public List<String> listTypes()
    {
        return entityManager.createQuery("SELECT DISTINCT type FROM EvaluationItem ORDER BY type",
                String.class).getResultList();
    }

    public List<String> listUsers()
    {
        return entityManager.createQuery(
                "SELECT DISTINCT userId FROM EvaluationResult ORDER BY userId", String.class)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<AggregatedEvaluationResult> listAggregatedResults(List<EvaluationItem> aItems,
            Collection<String> aUsers, double aUserThreshold, double aConfidenceThreshold)
    {
        if (aItems.isEmpty() || aUsers.isEmpty()) {
            return new ArrayList<AggregatedEvaluationResult>();
        }

        String query = loadQuery("classpath:/AggregatedQueryByItem.sql");

        javax.persistence.Query q = entityManager.createNativeQuery(query);
        List<String> itemIds = new ArrayList<String>();
        for (EvaluationItem item : aItems) {
            itemIds.add(Long.toString(item.getId()));
        }
        // q.setParameter("itemIds", itemIds);
        q.setParameter("users", aUsers);
        q.setParameter("usersL", aUsers.size());
        q.setParameter("userThreshold", aUserThreshold);
        q.setParameter("confidenceThreshold", aConfidenceThreshold);

        List<AggregatedEvaluationResult> aers = new ArrayList<AggregatedEvaluationResult>();
        // Big Performance problem with setParameterList()
        // https://hibernate.onjira.com/browse/HHH-766
        int chunkSize = 500;
        log.info("Fetching in-database results in "
                + ((aItems.size() / chunkSize) + (aItems.size() % chunkSize == 0 ? 0 : 1))
                + " chunks");
        ProgressMeter progress = new ProgressMeter(aItems.size());
        for (int i = 0; i < aItems.size(); i += chunkSize) {
            q.setParameter("itemIds", itemIds.subList(i, Math.min(i + chunkSize, itemIds.size())));

            // System.out.println("IDS: " + itemIds.subList(i, Math.min(i + chunkSize,
            // itemIds.size())));
            //System.out.println("USERS: " + aUsers);

            List<Object[]> list = q.getResultList();
            for (Object[] obj : list) {
                obj[0] = getEvaluationItem(Long.parseLong(obj[0].toString()));
                aers.add(new AggregatedEvaluationResult(obj, aUsers));
            }
            progress.setDone(i);
            log.info(progress);
        }
        return aers;
    }

    @Transactional
    public List<AggregatedEvaluationResult> listAggregatedResults(
            Collection<String> aCollectionIds, Collection<AnnotationType> aTypes,
            Collection<String> aUsers, double aUserThreshold, double aConfidenceThreshold)
    {
        if (aCollectionIds.isEmpty() || aTypes.isEmpty() || aUsers.isEmpty()) {
            return new ArrayList<AggregatedEvaluationResult>();
        }

        String query = loadQuery("classpath:/AggregatedQuery.sql");

        // long start = System.currentTimeMillis();
        javax.persistence.Query q = entityManager.createNativeQuery(query);
        q.setParameter("collectionIds", aCollectionIds);
        List<String> types = new ArrayList<String>();
        for (AnnotationType at : aTypes) {
            types.add(at.getName());
        }
        q.setParameter("types", types);
        q.setParameter("users", aUsers);
        q.setParameter("usersL", aUsers.size());
        q.setParameter("userThreshold", aUserThreshold);
        q.setParameter("confidenceThreshold", aConfidenceThreshold);

        @SuppressWarnings("unchecked")
        List<Object[]> list = q.getResultList();
        List<AggregatedEvaluationResult> aers = new ArrayList<AggregatedEvaluationResult>();
        for (Object[] obj : list) {
            obj[0] = getEvaluationItem(Long.parseLong(obj[0].toString()));
            aers.add(new AggregatedEvaluationResult(obj, aUsers));
        }
        // long stop = System.currentTimeMillis();
        // System.out.println("Time1 " + (stop - start));

        return aers;
    }

    public String test()
    {
        return entityManager.createNativeQuery("SELECT (2-0.00) / 3").getSingleResult().toString();
    }

    /**
     * Fetch all the results that the current annotator has not yet evaluated but at least one of
     * the other specified annotators already did evaluate.
     * 
     * @param aMyUser
     * @param aOtherUsers
     * @return
     */
    public List<EvaluationItem> listEvaluationResultsMissing(final String aCollectionId,
            final String aType, final String aMyUser, final Collection<String> aOtherUsers)
    {
        // String q = "select * from EvaluationResult where userId = :myUser and result = '' " +
        // "and item_id in (select distinct item_id from EvaluationResult where " +
        // "userId != :myUser and result != '' and userId in (:otherUsers))";

        String q = "from EvaluationItem where collectionId = :collectionId and type = :type and id "
                + "in (select distinct item from "
                + "EvaluationResult where userId != :myUser and result != '' and userId in "
                + "(:otherUsers)) and not (id in (select distinct item from EvaluationResult "
                + "where userId = :myUser and result != ''))";
        TypedQuery<EvaluationItem> query = entityManager.createQuery(q, EvaluationItem.class);
        query.setParameter("type", aType);
        query.setParameter("myUser", aMyUser);
        query.setParameter("otherUsers", aOtherUsers);
        query.setParameter("collectionId", aCollectionId);
        return query.getResultList();
    }

    public AnnotationType getType(String aTypeName)
    {
        return entityManager
                .createQuery("FROM AnnotationType WHERE name=:name", AnnotationType.class)
                .setParameter("name", aTypeName).getSingleResult();
    }
}
