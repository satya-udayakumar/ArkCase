/**
 *
 */
package com.armedia.acm.form.changecasestatus.service;

/*-
 * #%L
 * ACM Forms: Change Case Status
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

import com.armedia.acm.form.changecasestatus.model.ChangeCaseStatusForm;
import com.armedia.acm.form.changecasestatus.model.ChangeCaseStatusFormEvent;
import com.armedia.acm.form.config.ResolveInformation;
import com.armedia.acm.frevvo.config.FrevvoFormAbstractService;
import com.armedia.acm.frevvo.config.FrevvoFormName;
import com.armedia.acm.frevvo.model.UploadedFiles;
import com.armedia.acm.plugins.casefile.dao.CaseFileDao;
import com.armedia.acm.plugins.casefile.dao.ChangeCaseStatusDao;
import com.armedia.acm.plugins.casefile.model.CaseFile;
import com.armedia.acm.plugins.casefile.model.ChangeCaseStatus;
import com.armedia.acm.services.users.model.AcmUserActionName;

import org.json.JSONObject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
 * @author riste.tutureski
 */
public class ChangeCaseStatusService extends FrevvoFormAbstractService
{

    private Logger LOG = LogManager.getLogger(getClass());
    private CaseFileDao caseFileDao;
    private ChangeCaseStatusDao changeCaseStatusDao;
    private ApplicationEventPublisher applicationEventPublisher;

    /*
     * (non-Javadoc)
     * @see com.armedia.acm.frevvo.config.FrevvoFormService#get(java.lang.String)
     */
    @Override
    public Object get(String action)
    {
        Object result = null;

        if (action != null)
        {
            if ("init-form-data".equals(action))
            {
                result = initFormData();
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * @see com.armedia.acm.frevvo.config.FrevvoFormService#save(java.lang.String,
     * org.springframework.util.MultiValueMap)
     */
    @Override
    public boolean save(String xml, MultiValueMap<String, MultipartFile> attachments) throws Exception
    {

        String mode = getRequest().getParameter("mode");

        // Convert XML data to Object
        ChangeCaseStatusForm form = (ChangeCaseStatusForm) convertFromXMLToObject(cleanXML(xml), ChangeCaseStatusForm.class);

        if (form == null)
        {
            LOG.warn("Cannot unmarshall Close Case Form.");
            return false;
        }

        // Get CaseFile depends on the CaseFile ID
        CaseFile caseFile = getCaseFileDao().find(form.getInformation().getId());

        if (caseFile == null)
        {
            LOG.warn("Cannot find case file by given caseId=" + form.getInformation().getId());
            return false;
        }

        // Skip if the case is already closed or in "in approval" and if it's not edit mode
        if (("IN APPROVAL".equals(caseFile.getStatus())) && !"edit".equals(mode))
        {
            LOG.info("The case file is already in '" + caseFile.getStatus() + "' mode. No further action will be taken.");
            return true;
        }

        ChangeCaseStatusRequestFactory factory = new ChangeCaseStatusRequestFactory();
        ChangeCaseStatus changeCaseStatus = factory.formFromXml(form, getAuthentication());

        if ("edit".equals(mode))
        {
            String requestId = getRequest().getParameter("requestId");
            ChangeCaseStatus changeCaseStatusFromDatabase = null;

            try
            {
                Long changeCaseStatusId = Long.parseLong(requestId);
                changeCaseStatusFromDatabase = getChangeCaseStatusDao().find(changeCaseStatusId);
            }
            catch (Exception e)
            {
                LOG.warn("Close Case Request with id=" + requestId + " is not found. The new request will be recorded in the database.");
            }

            if (null != changeCaseStatusFromDatabase)
            {
                changeCaseStatus.setId(changeCaseStatusFromDatabase.getId());
                getChangeCaseStatusDao().delete(changeCaseStatusFromDatabase.getParticipants());
            }
        }

        ChangeCaseStatus savedRequest = getChangeCaseStatusDao().save(changeCaseStatus);

        if (!"edit".equals(mode))
        {
            // Record user action
            getUserActionExecutor().execute(savedRequest.getId(), AcmUserActionName.LAST_CHANGE_CASE_STATUS_CREATED,
                    getAuthentication().getName());
        }
        else
        {
            // Record user action
            getUserActionExecutor().execute(savedRequest.getId(), AcmUserActionName.LAST_CHANGE_CASE_STATUS_MODIFIED,
                    getAuthentication().getName());
        }

        // Update Status to "IN APPROVAL"
        if (!caseFile.getStatus().equals("IN APPROVAL") && !"edit".equals(mode))
        {
            caseFile.setStatus("IN APPROVAL");
            getCaseFileDao().save(caseFile);
        }

        // Save attachments (or update XML form and PDF form if the mode is "edit")
        String cmisFolderId = findFolderIdForAttachments(caseFile.getContainer(), caseFile.getObjectType(), caseFile.getId());
        UploadedFiles uploadedFiles = saveAttachments(attachments, cmisFolderId, FrevvoFormName.CASE_FILE.toUpperCase(),
                caseFile.getId());

        ChangeCaseStatusFormEvent event = new ChangeCaseStatusFormEvent(caseFile.getCaseNumber(), caseFile.getId(), savedRequest,
                uploadedFiles, mode, getAuthentication().getName(), getUserIpAddress(), true);
        getApplicationEventPublisher().publishEvent(event);

        return true;
    }

    @Override
    public String getFormName()
    {
        return FrevvoFormName.CHANGE_CASE_STATUS;
    }

    @Override
    public Class<?> getFormClass()
    {
        return ChangeCaseStatusForm.class;
    }

    private Object initFormData()
    {
        String mode = getRequest().getParameter("mode");
        ChangeCaseStatusForm changeCaseStatus = new ChangeCaseStatusForm();

        ResolveInformation information = new ResolveInformation();
        if (!"edit".equals(mode))
        {
            information.setDate(new Date());
        }

        information.setResolveOptions(getStandardLookupEntries("changeCaseStatuses"));

        changeCaseStatus.setResolutions(getStandardLookupEntries("changeCaseStatusResolutions"));

        changeCaseStatus.setInformation(information);

        JSONObject json = createResponse(changeCaseStatus);

        return json;
    }

    /**
     * @return the caseFileDao
     */
    public CaseFileDao getCaseFileDao()
    {
        return caseFileDao;
    }

    /**
     * @param caseFileDao
     *            the caseFileDao to set
     */
    public void setCaseFileDao(CaseFileDao caseFileDao)
    {
        this.caseFileDao = caseFileDao;
    }

    public ChangeCaseStatusDao getChangeCaseStatusDao()
    {
        return changeCaseStatusDao;
    }

    public void setChangeCaseStatusDao(ChangeCaseStatusDao changeCaseStatusDao)
    {
        this.changeCaseStatusDao = changeCaseStatusDao;
    }

    public ApplicationEventPublisher getApplicationEventPublisher()
    {
        return applicationEventPublisher;
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher)
    {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Object convertToFrevvoForm(Object obj, Object form)
    {
        // Implementation no needed so far
        return null;
    }
}
