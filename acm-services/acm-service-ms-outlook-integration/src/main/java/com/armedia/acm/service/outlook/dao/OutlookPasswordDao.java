package com.armedia.acm.service.outlook.dao;

/*-
 * #%L
 * ACM Service: MS Outlook integration
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
import com.armedia.acm.service.outlook.model.OutlookDTO;
import com.armedia.acm.service.outlook.model.OutlookPassword;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

public class OutlookPasswordDao extends AcmAbstractDao<OutlookPassword>
{

    private transient final Logger log = LogManager.getLogger(getClass());
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void saveOutlookPassword(Authentication authentication, OutlookDTO in)
    {
        // must use a native update, since we don't want to add a password property to an entity class; since
        // if we did that, the password would end up being passed around in POJOs and JSON objects.

        String sql = "UPDATE acm_outlook_password " +
                "SET cm_outlook_password = ?1 " +
                "WHERE cm_user_id = ?2 ";

        int updated = updatePassword(authentication, in, sql);

        if (updated == 0)
        {
            // no existing password, so we have to insert the row
            sql = "INSERT INTO acm_outlook_password (cm_outlook_password, cm_user_id) VALUES (?1 , ?2) ";
            updated = updatePassword(authentication, in, sql);

            if (updated == 0)
            {
                throw new IllegalStateException("Could not set Outlook password for user '" + authentication.getName() + "'");
            }

        }

    }

    private int updatePassword(Authentication authentication, OutlookDTO in, String sql)
    {
        Query q = getEm().createNativeQuery(sql);
        q.setParameter(1, in.getOutlookPassword());
        q.setParameter(2, authentication.getName().toLowerCase());

        return q.executeUpdate();
    }

    @Transactional
    public OutlookDTO retrieveOutlookPassword(Authentication authentication)
    {
        // must use a native query, since we don't want to add a password property to an entity class; since
        // if we did that, the password would end up being passed around in POJOs and JSON objects.

        String sql = "SELECT cm_outlook_password " +
                "FROM acm_outlook_password op " +
                "WHERE op.cm_user_id = ?1 ";

        Query q = getEm().createNativeQuery(sql);
        q.setParameter(1, authentication.getName().toLowerCase());

        try
        {
            Object o = q.getSingleResult();

            log.debug("Query result is of type: " + (o == null ? "null" : o.getClass().getName()));

            String password = (String) o;

            OutlookDTO retval = new OutlookDTO();
            retval.setOutlookPassword(password);

            return retval;
        }
        catch (PersistenceException pe)
        {
            throw new IllegalStateException("No outlook password for user '" + authentication.getName() + "'");
        }
    }

    public OutlookPassword findByUserId(String userId)
    {
        userId = userId.toLowerCase();
        return getEntityManager().find(OutlookPassword.class, userId);
    }

    public EntityManager getEntityManager()
    {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }

    @Override
    protected Class<OutlookPassword> getPersistenceClass()
    {
        return OutlookPassword.class;
    }
}
