package com.armedia.acm.plugins.alfrescorma.service;

/*-
 * #%L
 * ACM Extra Plugin: Alfresco RMA Integration
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

import com.armedia.acm.plugins.alfrescorma.exception.AlfrescoServiceException;
import com.armedia.acm.plugins.alfrescorma.model.AlfrescoRmaConfig;
import com.armedia.acm.plugins.ecm.model.EcmFile;
import com.armedia.acm.plugins.ecm.model.EcmFileDeclareRequestEvent;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.ApplicationListener;

/**
 * Created by armdev on 5/1/14.
 */
public class AcmFileDeclareRequestListener implements ApplicationListener<EcmFileDeclareRequestEvent>
{

    private transient Logger log = LogManager.getLogger(getClass());
    private AlfrescoRecordsService alfrescoRecordsService;

    @Override
    public void onApplicationEvent(EcmFileDeclareRequestEvent ecmFileDeclareRequestEvent)
    {
        AlfrescoRmaConfig rmaConfig = alfrescoRecordsService.getRmaConfig();
        boolean proceed = rmaConfig.getIntegrationEnabled() && rmaConfig.getDeclareFileRecordOnDeclareRequest();

        if (!proceed)
        {
            return;
        }

        if (!ecmFileDeclareRequestEvent.isSucceeded())
        {
            log.trace("Returning - file declaration request was not successful");
        }

        try
        {
            EcmFile ecmFile = ecmFileDeclareRequestEvent.getSource();
            String originator = ecmFile.getCustodian() != null ? ecmFile.getCustodian() : ecmFileDeclareRequestEvent.getUserId();
            getAlfrescoRecordsService().declareFileAsRecord(ecmFile.getContainer(),
                    ecmFileDeclareRequestEvent.getEventDate(), ecmFileDeclareRequestEvent.getParentObjectName(),
                    rmaConfig.getDefaultOriginatorOrg(),
                    originator, ecmFileDeclareRequestEvent.getEcmFileId(),
                    ecmFile.getStatus(), ecmFileDeclareRequestEvent.getObjectId());

        }
        catch (AlfrescoServiceException e)
        {
            log.error("Could not declare file as record: {}", e.getMessage(), e);
        }
    }

    public AlfrescoRecordsService getAlfrescoRecordsService()
    {
        return alfrescoRecordsService;
    }

    public void setAlfrescoRecordsService(AlfrescoRecordsService alfrescoRecordsService)
    {
        this.alfrescoRecordsService = alfrescoRecordsService;
    }

}
