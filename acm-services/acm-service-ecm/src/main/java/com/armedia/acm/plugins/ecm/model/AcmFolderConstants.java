package com.armedia.acm.plugins.ecm.model;

/*-
 * #%L
 * ACM Service: Enterprise Content Management
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

/**
 * Created by marjan.stefanoski on 03.04.2015.
 */
public interface AcmFolderConstants
{
    String OBJECT_FOLDER_TYPE = "FOLDER";

    String CONTAINER_OBJECT_TYPE = "objectType";
    String CONTAINER_OBJECT_TITLE = "objectTitle";

    String USER_ACTION_RENAME_FOLDER = "RENAME";
    String USER_ACTION_ADD_NEW_FOLDER = "ADD NEW";
    String USER_ACTION_CREATE_FOLDER_BY_PATH = " CREATE";
    String USER_ACTION_LIST_FOLDER = "LIST";
    String USER_ACTION_DELETE_FOLDER = "DELETE";
    String USER_ACTION_MOVE_FOLDER = "MOVE";
    String USER_ACTION_COPY_FOLDER = "COPY";
    String USER_ACTION_GET_FOLDER = "GET";
    String USER_ACTION_COPY_FOLDER_AS_LINK = "LINK FOLDER";
    String READ_ONLY = "READ ONLY";

    String EVENT_TYPE_FOLDER_MOVED = "com.armedia.acm.folder.moved";
    String EVENT_TYPE_FOLDER_COPIED = "com.armedia.acm.folder.copied";
    String EVENT_TYPE_FOLDER_COPIED_AS_LINK = "com.armedia.acm.folder.copiedAsLink";

    int ZERO = 0;

    String SUCCESS_FOLDER_DELETE_MSG = "Folder deleted successfully";

    String IP_ADDRESS_ATTRIBUTE = "acm_ip_address";

    String CMIS_OBJECT_TYPE_ID_FOLDER = "cmis:folder";
    String CMIS_OBJECT_TYPE_ID_FILE = "cmis:document";

    String FOLDER_STRUCTURE_KEY_NAME = "name";
    String FOLDER_STRUCTURE_KEY_CHILDREN = "children";
    String FOLDER_STRUCTURE_KEY_ATTACHMENT = "attachment";

    // Multiple CMIS configurations constants
    String CONFIGURATION_REFERENCE = "configRef";
}
