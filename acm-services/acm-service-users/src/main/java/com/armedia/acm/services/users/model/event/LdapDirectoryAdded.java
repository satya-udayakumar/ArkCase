package com.armedia.acm.services.users.model.event;

/*-
 * #%L
 * Tool Integrations: Spring Child Context Holder
 * %%
 * Copyright (C) 2014 - 2020 ArkCase LLC
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

import com.armedia.acm.core.model.AcmEvent;

/**
 * @author mario.gjurcheski
 *
 */
public class LdapDirectoryAdded extends AcmEvent
{

    /**
     * Create a new ApplicationEvent.
     *
     * @param source
     *            the component that published the event (never {@code null})
     * @param name
     */
    public LdapDirectoryAdded(Object source, String name)
    {
        super(source);
        setEventDescription(name);
    }
}
