package com.armedia.acm.tool.zylab.model;

/*-
 * #%L
 * Tool Integrations: Arkcase ZyLAB Integration
 * %%
 * Copyright (C) 2014 - 2021 ArkCase LLC
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

import org.springframework.context.ApplicationEvent;

/**
 * Created by Aleksandar Acevski <aleksandar.acevski@armedia.com> on January, 2021
 */
public class ZylabProductionFileIncomingEvent extends ApplicationEvent
{
    Long matterId;
    String productionKey;

    public ZylabProductionFileIncomingEvent(Long matterId, String productionKey)
    {
        super(matterId);

        this.matterId = matterId;
        this.productionKey = productionKey;
    }

    public ZylabProductionFileIncomingEvent(ZylabProductionSyncDTO zylabProductionSyncDTO)
    {
        super(zylabProductionSyncDTO);

        this.matterId = zylabProductionSyncDTO.getMatterId();
        this.productionKey = zylabProductionSyncDTO.getProductionKey();
    }

    public Long getMatterId() {
        return matterId;
    }

    public String getProductionKey() {
        return productionKey;
    }
}
