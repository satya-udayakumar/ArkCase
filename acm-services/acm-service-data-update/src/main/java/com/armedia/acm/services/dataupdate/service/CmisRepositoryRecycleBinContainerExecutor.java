package com.armedia.acm.services.dataupdate.service;

/*-
 * #%L
 * ACM Service: Data Update Service
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

import com.armedia.acm.camelcontext.arkcase.cmis.ArkCaseCMISConstants;
import com.armedia.acm.core.exceptions.AcmCreateObjectFailedException;
import com.armedia.acm.plugins.ecm.model.RecycleBinConstants;
import com.armedia.acm.plugins.ecm.service.RecycleBinItemService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CmisRepositoryRecycleBinContainerExecutor implements AcmDataUpdateExecutor
{
    private RecycleBinItemService recycleBinItemService;
    private final Logger log = LogManager.getLogger(getClass());

    @Override
    public String getUpdateId()
    {
        return "default-cmis-recycle-bin-container-created";
    }

    @Override
    public void execute()
    {
        try
        {
            getRecycleBinItemService().getOrCreateContainerForRecycleBin(RecycleBinConstants.OBJECT_TYPE,
                    ArkCaseCMISConstants.DEFAULT_CMIS_REPOSITORY_ID);
        }
        catch (AcmCreateObjectFailedException e)
        {
            log.error("Error on creating Recycle Bin container");
        }
    }

    public RecycleBinItemService getRecycleBinItemService()
    {
        return recycleBinItemService;
    }

    public void setRecycleBinItemService(RecycleBinItemService recycleBinItemService)
    {
        this.recycleBinItemService = recycleBinItemService;
    }
}
