package com.armedia.acm.plugins.consultation.model;

/*-
 * #%L
 * ACM Default Plugin: Consultation
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

import com.armedia.acm.pluginmanager.service.AcmPluginConfigBean;
import com.armedia.acm.plugins.ecm.service.SupportsFileTypes;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.beans.factory.annotation.Value;

import java.util.Set;

/**
 * Created by Vladimir Cherepnalkovski <vladimir.cherepnalkovski@armedia.com> on May, 2020
 */
public class ConsultationConfig implements SupportsFileTypes, AcmPluginConfigBean
{
    @JsonProperty("consultation.plugin.auto_create_calendar_folder")
    @Value("${consultation.plugin.auto_create_calendar_folder}")
    private Boolean autoCreateCalendarFolder;

    @JsonProperty("consultation.plugin.delete_calendar_folder_after_consultation_closed")
    @Value("${consultation.plugin.delete_calendar_folder_after_consultation_closed}")
    private Boolean deleteCalendarFolderAfterConsultationClosed;

    @JsonProperty("consultation.plugin.folder.structure")
    @Value("${consultation.plugin.folder.structure}")
    private String folderStructure;

    @JsonProperty("consultation.plugin.status.closed")
    @Value("${consultation.plugin.status.closed}")
    private String statusClosed;

    @JsonProperty("consultation.plugin.search.tree.filter")
    @Value("${consultation.plugin.search.tree.filter}")
    private String searchTreeFilter;

    @JsonProperty("consultation.plugin.search.tree.sort")
    @Value("${consultation.plugin.search.tree.sort}")
    private String searchTreeSort;

    @JsonProperty("consultation.plugin.search.tree.searchQuery")
    @Value("${consultation.plugin.search.tree.searchQuery}")
    private String searchTreeSearchQuery;

    @JsonProperty("consultation.plugin.fileTypes")
    @Value("${consultation.plugin.fileTypes}")
    private String supportedFileTypes;

    public Boolean getAutoCreateCalendarFolder()
    {
        return autoCreateCalendarFolder;
    }

    public void setAutoCreateCalendarFolder(Boolean autoCreateCalendarFolder)
    {
        this.autoCreateCalendarFolder = autoCreateCalendarFolder;
    }

    public Boolean getDeleteCalendarFolderAfterConsultationClosed()
    {
        return deleteCalendarFolderAfterConsultationClosed;
    }

    public void setDeleteCalendarFolderAfterConsultationClosed(Boolean deleteCalendarFolderAfterConsultationClosed)
    {
        this.deleteCalendarFolderAfterConsultationClosed = deleteCalendarFolderAfterConsultationClosed;
    }

    public String getFolderStructure()
    {
        return folderStructure;
    }

    public void setFolderStructure(String folderStructure)
    {
        this.folderStructure = folderStructure;
    }

    public String getStatusClosed()
    {
        return statusClosed;
    }

    public void setStatusClosed(String statusClosed)
    {
        this.statusClosed = statusClosed;
    }

    public String getSearchTreeSearchQuery()
    {
        return searchTreeSearchQuery;
    }

    public void setSearchTreeSearchQuery(String searchTreeSearchQuery)
    {
        this.searchTreeSearchQuery = searchTreeSearchQuery;
    }

    public String getSupportedFileTypes()
    {
        return supportedFileTypes;
    }

    public void setSupportedFileTypes(String supportedFileTypes)
    {
        this.supportedFileTypes = supportedFileTypes;
    }

    @Override
    public Set<String> getFileTypes()
    {
        return getFileTypes(supportedFileTypes);
    }

    @Override
    public String getSearchTreeFilter()
    {
        return searchTreeFilter;
    }

    public void setSearchTreeFilter(String searchTreeFilter)
    {
        this.searchTreeFilter = searchTreeFilter;
    }

    @Override
    public String getSearchTreeQuery()
    {
        return searchTreeSearchQuery;
    }

    @Override
    public String getSearchTreeSort()
    {
        return searchTreeSort;
    }

    public void setSearchTreeSort(String searchTreeSort)
    {
        this.searchTreeSort = searchTreeSort;
    }
}
