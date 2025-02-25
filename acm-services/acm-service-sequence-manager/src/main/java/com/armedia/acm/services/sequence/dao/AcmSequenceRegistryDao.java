package com.armedia.acm.services.sequence.dao;

/*-
 * #%L
 * ACM Service: Sequence Manager
 * %%
 * Copyright (C) 2014 - 2018 ArkCase LLC
 * %%
 * This file is part of the ArkCase software. 
 * 
 * If the software was purchased under a paid ArkCase license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import com.armedia.acm.data.AcmAbstractDao;
import com.armedia.acm.services.sequence.exception.AcmSequenceException;
import com.armedia.acm.services.sequence.model.AcmSequenceRegistry;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.LockTimeoutException;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PessimisticLockException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sasko.tanaskoski
 *
 */
public class AcmSequenceRegistryDao extends AcmAbstractDao<AcmSequenceRegistry>
{
    private final Logger log = LogManager.getLogger(getClass());

    @Override
    protected Class<AcmSequenceRegistry> getPersistenceClass()
    {
        return AcmSequenceRegistry.class;
    }

    public Integer removeSequenceRegistry(String sequenceValue)
    {
        String queryText = "DELETE FROM " +
                "AcmSequenceRegistry sequenceRegistry " +
                "WHERE sequenceRegistry.sequenceValue = :sequenceValue";

        Query query = getEm().createQuery(queryText);

        query.setParameter("sequenceValue", sequenceValue);
        return query.executeUpdate();
    }

    public Integer removeSequenceRegistry(String sequenceName, String sequencePartName)
    {
        String queryText = "DELETE FROM " +
                "AcmSequenceRegistry sequenceRegistry " +
                "WHERE sequenceRegistry.sequenceName = :sequenceName " +
                "AND sequenceRegistry.sequencePartName = :sequencePartName";

        Query query = getEm().createQuery(queryText);

        query.setParameter("sequenceName", sequenceName);
        query.setParameter("sequencePartName", sequencePartName);
        return query.executeUpdate();
    }

    @Retryable(maxAttempts = 30, value = PessimisticLockException.class, backoff = @Backoff(delay = 100))
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AcmSequenceRegistry getSequenceRegistry(String sequenceName, String sequencePartName, FlushModeType flushModeType) throws InterruptedException, AcmSequenceException {
        AcmSequenceRegistry sequenceRegistry = null;
        String queryText = "SELECT sequenceRegistry " +
                "FROM AcmSequenceRegistry sequenceRegistry " +
                "WHERE sequenceRegistry.sequenceName = :sequenceName " +
                "AND sequenceRegistry.sequencePartName = :sequencePartName " +
                "ORDER BY sequenceRegistry.sequencePartValue";

        TypedQuery<AcmSequenceRegistry> query = getEm().createQuery(queryText, AcmSequenceRegistry.class);
        query.setFlushMode(flushModeType);
        query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        query.setHint("javax.persistence.lock.timeout", "3000");
        query.setMaxResults(1);

        query.setParameter("sequenceName", sequenceName);
        query.setParameter("sequencePartName", sequencePartName);

        try
        {
            sequenceRegistry = query.getSingleResult();
        }
        catch (PessimisticLockException | LockTimeoutException e)
        {
            log.trace("Error locking entry", e);
        }
        catch (NoResultException nre)
        {
            log.trace("No SequenceRegistry with sequence name [{}], sequence part name [{}]", sequenceName,
                    sequencePartName);
        }

        if(sequenceRegistry != null){
            log.info("Removing Sequence Entity for [{}] [{}]", sequenceRegistry.getSequenceName(), sequenceRegistry.getSequencePartName());
            try
            {
                removeSequenceRegistry(sequenceRegistry.getSequenceValue());
            }
            catch (Exception e)
            {
                throw new AcmSequenceException(
                        String.format("Unable to remove Sequence Entity [%s] [%s]", sequenceRegistry.getSequenceName(),
                                sequenceRegistry.getSequencePartName()),
                        e);
            }
        }

        return sequenceRegistry;
    }

    public List<AcmSequenceRegistry> getSequenceRegistryList()
    {
        String queryText = "SELECT sequenceRegistry " +
                "FROM AcmSequenceRegistry sequenceRegistry";

        TypedQuery<AcmSequenceRegistry> query = getEm().createQuery(queryText, AcmSequenceRegistry.class);

        List<AcmSequenceRegistry> sequenceRegistryList = query.getResultList();
        if (null == sequenceRegistryList)
        {
            sequenceRegistryList = new ArrayList<AcmSequenceRegistry>();
        }
        return sequenceRegistryList;
    }

    @Override
    public String getSupportedObjectType()
    {
        return "ACM_SEQUENCE_REGISTRY";
    }

}
