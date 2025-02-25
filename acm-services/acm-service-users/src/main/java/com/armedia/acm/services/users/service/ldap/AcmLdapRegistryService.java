package com.armedia.acm.services.users.service.ldap;

/*-
 * #%L
 * ACM Service: Users
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

import java.util.Map;

/**
 * @author mario.gjurcheski
 *
 */
public interface AcmLdapRegistryService
{

    /**
     * synchronize the ldap configuration beans
     */
    void sync();

    /**
     * synchronizes the ldap group beans when ldap configuration is updated.
     *
     */
    void registerLdapGroupConfigBeanDefinition();

    /**
     * synchronizes the ldap user beans when ldap configuration is updated.
     *
     */
    void registerLdapUserConfigBeanDefinition();

    /**
     * synchronizes the ldap directory beans when ldap configuration is updated.
     *
     */
    void registerLdapDirectoryConfigBeanDefinition();

    /**
     * Creates ldap user configuration in config server.
     *
     * @params directoryType,
     *         props, properties
     */
    Map<String, Object> getLdapUserConfig(String id, String directoryType);

    /**
     * Creates ldap group configuration in config server.
     *
     * @params directoryType,
     *         props, properties
     */
    Map<String, Object> getLdapGroupConfig(String id, String directoryType);

    /**
     * Creates ldap directory configuration in config server.
     *
     * @params directoryId,
     *         properties
     */
    void createLdapDirectoryConfig(String id, Map<String, Object> properties);

    /**
     * Deletes ldap directory configuration in config server.
     *
     * @params directoryId,
     */
    void deleteLdapDirectoryConfig(String id);
}
