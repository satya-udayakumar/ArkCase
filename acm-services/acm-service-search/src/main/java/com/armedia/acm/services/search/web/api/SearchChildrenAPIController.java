package com.armedia.acm.services.search.web.api;

/*-
 * #%L
 * ACM Service: Search
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

import com.armedia.acm.services.search.service.ChildDocumentsSearchService;
import com.armedia.acm.services.search.exception.SolrException;
import com.armedia.acm.services.search.model.solr.SolrCore;
import com.armedia.acm.services.search.service.ExecuteSolrQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping({ "/api/v1/plugin/search", "/api/latest/plugin/search" })
public class SearchChildrenAPIController
{

    private ExecuteSolrQuery executeSolrQuery;

    private ChildDocumentsSearchService childDocumentsSearchService;

    @RequestMapping(value = "/children", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String children(
            @RequestParam(value = "parentType", required = true) String parentType,
            @RequestParam(value = "parentId", required = true) Long parentId,
            @RequestParam(value = "childType", required = false, defaultValue = "") String childType,
            @RequestParam(value = "activeOnly", required = false, defaultValue = "false") boolean activeOnly,
            @RequestParam(value = "exceptDeletedOnly", required = false, defaultValue = "true") boolean exceptDeletedOnly,
            @RequestParam(value = "extra", required = false) List<String> extra,
            @RequestParam(value = "s", required = false, defaultValue = "") String sort,
            @RequestParam(value = "start", required = false, defaultValue = "0") int startRow,
            @RequestParam(value = "n", required = false, defaultValue = "10") int maxRows,
            Authentication authentication) throws SolrException
    {
        return getChildDocumentsSearchService().searchChildren(parentType, parentId, childType, activeOnly,
                exceptDeletedOnly, extra, sort, startRow, maxRows, authentication);
    }

    @RequestMapping(value = "/children/advanced", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String advancedChildren(
            @RequestParam(value = "parentType", required = true) String parentType,
            @RequestParam(value = "parentId", required = true) Long parentId,
            @RequestParam(value = "childType", required = false, defaultValue = "") String childType,
            @RequestParam(value = "activeOnly", required = false, defaultValue = "false") boolean activeOnly,
            @RequestParam(value = "exceptDeletedOnly", required = false, defaultValue = "true") boolean exceptDeletedOnly,
            @RequestParam(value = "extra", required = false) List<String> extra,
            @RequestParam(value = "s", required = false, defaultValue = "") String sort,
            @RequestParam(value = "start", required = false, defaultValue = "0") int startRow,
            @RequestParam(value = "n", required = false, defaultValue = "10") int maxRows,
            Authentication authentication) throws SolrException
    {
        return getChildDocumentsSearchService().searchChildren(parentType, parentId, childType, activeOnly,
                exceptDeletedOnly, extra, sort, startRow, maxRows, authentication);
    }

    public ExecuteSolrQuery getExecuteSolrQuery()
    {
        return executeSolrQuery;
    }

    public void setExecuteSolrQuery(ExecuteSolrQuery executeSolrQuery)
    {
        this.executeSolrQuery = executeSolrQuery;
    }

    public ChildDocumentsSearchService getChildDocumentsSearchService()
    {
        return childDocumentsSearchService;
    }

    public void setChildDocumentsSearchService(ChildDocumentsSearchService childDocumentsSearchService)
    {
        this.childDocumentsSearchService = childDocumentsSearchService;
    }
}
