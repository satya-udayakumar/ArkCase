package com.armedia.acm.plugins.ecm.web.api;

/*-
 * #%L
 * ACM Service: Enterprise Content Management
 * %%
 * Copyright (C) 2014 - 2019 ArkCase LLC
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

import com.armedia.acm.core.exceptions.AcmObjectNotFoundException;
import com.armedia.acm.core.exceptions.AcmUserActionFailedException;
import com.armedia.acm.plugins.ecm.exception.LinkAlreadyExistException;
import com.armedia.acm.plugins.ecm.model.EcmFile;
import com.armedia.acm.plugins.ecm.model.EcmFileConstants;
import com.armedia.acm.plugins.ecm.model.FileDTO;
import com.armedia.acm.plugins.ecm.model.MoveCopyFileDto;
import com.armedia.acm.plugins.ecm.service.EcmFileService;
import com.armedia.acm.plugins.ecm.service.FileEventPublisher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @author aleksandar.bujaroski
 */
@Controller
@RequestMapping({ "/api/v1/service/ecm", "/api/latest/service/ecm" })
public class CopyFileAsLinkAPIController
{
    private transient final Logger log = LogManager.getLogger(getClass());
    private EcmFileService fileService;
    private FileEventPublisher fileEventPublisher;

    @PreAuthorize("hasPermission(#in.id, 'FILE', 'read|group-read|write|group-write') and hasPermission(#in.folderId, 'FOLDER', 'write|group-write')")
    @RequestMapping(value = "/copyToAnotherContainerAsLink/{targetObjectType}/{targetObjectId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public FileDTO copyFileAsLink(@RequestBody MoveCopyFileDto in, @PathVariable("targetObjectType") String targetObjectType,
            @PathVariable("targetObjectId") Long targetObjectId, Authentication authentication, HttpSession session)
            throws AcmUserActionFailedException, LinkAlreadyExistException
    {

        log.info("File with id: {} will be copy into folder with id: {}", in.getId(), in.getFolderId());
        String ipAddress = (String) session.getAttribute(EcmFileConstants.IP_ADDRESS_ATTRIBUTE);
        EcmFile source = getFileService().findById(in.getId());
        try
        {
            EcmFile copyFile = getFileService().copyFileAsLink(in.getId(), targetObjectId, targetObjectType, in.getFolderId());

            log.info("Successfully created link for File with id: {} to the location with id: {}", in.getId() + in.getFolderId());

            getFileEventPublisher().publishFileCopiedAsLinkEvent(copyFile, source, authentication, ipAddress, true);
            FileDTO fileDTO = new FileDTO();
            fileDTO.setNewFile(copyFile);
            fileDTO.setOriginalId(Long.toString(in.getId()));
            fileDTO.setLink(copyFile.isLink());
            return fileDTO;
        }
        catch (AcmUserActionFailedException e)
        {

            log.error("Exception occurred while creating link for file with id: {}  to the location with id: {}",
                    in.getId(), in.getFolderId());
            getFileEventPublisher().publishFileCopiedAsLinkEvent(source, null, authentication, ipAddress, false);
            throw e;
        }
        catch (AcmObjectNotFoundException e)
        {
            log.debug("File with id: {} not found in the DB", in.getId());
            getFileEventPublisher().publishFileCopiedAsLinkEvent(source, null, authentication, ipAddress, false);
            throw new AcmUserActionFailedException(EcmFileConstants.USER_ACTION_COPY_FILE, EcmFileConstants.OBJECT_FILE_TYPE, in.getId(),
                    "File not found.", e);
        }
        catch (LinkAlreadyExistException e)
        {

            log.error("Link for file {} already exist in current directory", source.getFileName());

            getFileEventPublisher().publishFileCopiedAsLinkEvent(source, null, authentication, ipAddress, false);
            throw e;
        }
    }

    public EcmFileService getFileService()
    {
        return fileService;
    }

    public void setFileService(EcmFileService fileService)
    {
        this.fileService = fileService;
    }

    public FileEventPublisher getFileEventPublisher()
    {
        return fileEventPublisher;
    }

    public void setFileEventPublisher(FileEventPublisher fileEventPublisher)
    {
        this.fileEventPublisher = fileEventPublisher;
    }
}
