package com.armedia.acm.plugins.ecm.service.sync.impl;

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

import com.armedia.acm.core.exceptions.AcmCreateObjectFailedException;
import com.armedia.acm.core.exceptions.AcmObjectNotFoundException;
import com.armedia.acm.core.exceptions.AcmUserActionFailedException;
import com.armedia.acm.data.AuditPropertyEntityAdapter;
import com.armedia.acm.plugins.ecm.dao.AcmFolderDao;
import com.armedia.acm.plugins.ecm.dao.EcmFileDao;
import com.armedia.acm.plugins.ecm.model.AcmContainer;
import com.armedia.acm.plugins.ecm.model.AcmFolder;
import com.armedia.acm.plugins.ecm.model.EcmFile;
import com.armedia.acm.plugins.ecm.model.EcmFileConstants;
import com.armedia.acm.plugins.ecm.model.sync.EcmEvent;
import com.armedia.acm.plugins.ecm.model.sync.EcmEventType;
import com.armedia.acm.plugins.ecm.service.AcmFolderService;
import com.armedia.acm.plugins.ecm.service.EcmFileService;
import com.armedia.acm.plugins.ecm.utils.FolderAndFilesUtils;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import javax.persistence.NoResultException;

/**
 * @author ivana.shekerova on 12/21/2018.
 */
public class EcmFileMovedEventHandler implements ApplicationListener<EcmEvent>
{

    private transient final Logger log = LoggerFactory.getLogger(getClass());
    private AcmFolderService folderService;
    private AuditPropertyEntityAdapter auditPropertyEntityAdapter;
    private AcmFolderDao folderDao;
    private EcmFileDao fileDao;
    private EcmFileService fileService;
    private FolderAndFilesUtils folderAndFilesUtils;

    public EcmFile onEcmFileMoved(EcmEvent ecmEvent)
    {
        getAuditPropertyEntityAdapter().setUserId(ecmEvent.getUserId());

        AcmFolder sourceParentFolder = lookupArkCaseFolder(ecmEvent.getSourceParentNodeId());
        AcmFolder targetParentFolder = lookupArkCaseFolder(ecmEvent.getTargetParentNodeId());

        if (sourceParentFolder != null && targetParentFolder != null)
        {
            return moveFile(ecmEvent, sourceParentFolder, targetParentFolder);
        }
        else if (sourceParentFolder != null && targetParentFolder == null)
        {
            deleteFile(ecmEvent, sourceParentFolder);
        }
        else if (sourceParentFolder == null && targetParentFolder != null)
        {
            return uploadFile(ecmEvent, targetParentFolder);
        }
        return null;
    }

    private EcmFile uploadFile(EcmEvent ecmEvent, AcmFolder targetParentFolder)
    {
        AcmContainer container = lookupArkCaseContainer(targetParentFolder.getId());
        if (container == null)
        {
            log.debug("Can't find container for the new file with id {}, exiting.", ecmEvent.getNodeId());
            return null;
        }
        String cmisRepositoryId = getFolderService().getCmisRepositoryId(targetParentFolder);
        Document cmisDocument = lookupCmisDocument(cmisRepositoryId, ecmEvent.getNodeId());
        if (cmisDocument == null)
        {
            log.error("No document to be loaded - exiting.");
            return null;
        }
        EcmFile addedToArkCase = null;
        try
        {
            addedToArkCase = getFileService().upload(
                    ecmEvent.getNodeName(),
                    findFileType(cmisDocument),
                    "Document",
                    cmisDocument.getContentStream().getStream(),
                    cmisDocument.getContentStreamMimeType(),
                    ecmEvent.getNodeName(),
                    new UsernamePasswordAuthenticationToken(ecmEvent.getUserId(), ecmEvent.getUserId()),
                    targetParentFolder.getCmisFolderId(),
                    container.getContainerObjectType(),
                    container.getContainerObjectId(),
                    targetParentFolder.getCmisRepositoryId(),
                    cmisDocument);
        }
        catch (AcmCreateObjectFailedException | AcmUserActionFailedException e)
        {
            log.error("Could not add file with CMIS ID [{}] to ArkCase: {}", ecmEvent.getNodeId(), e.getMessage(), e);
        }
        return addedToArkCase;
    }

    private void deleteFile(EcmEvent ecmEvent, AcmFolder sourceParentFolder)
    {
        // delete the file, since target folder is not in arkcase
        try
        {
            EcmFile arkCaseFile = lookupArkCaseFile(ecmEvent.getNodeId(), sourceParentFolder.getId());
            if (arkCaseFile != null)
            {
                getFileService().deleteFileInArkcase(arkCaseFile);
                log.info("Deleted file with CMIS ID [{}]", ecmEvent.getNodeId());
            }
        }
        catch (AcmUserActionFailedException | AcmObjectNotFoundException e)
        {
            log.error("Could not delete file with CMIS ID [{}] to ArkCase: {}", ecmEvent.getNodeId(), e.getMessage(), e);
        }
    }

    private EcmFile moveFile(EcmEvent ecmEvent, AcmFolder sourceParentFolder, AcmFolder targetParentFolder)
    {
        EcmFile arkCaseFile = lookupArkCaseFile(ecmEvent.getNodeId(), sourceParentFolder.getId());
        if (arkCaseFile != null)
        {
            try
            {
                EcmFile movedFile = getFileService().moveFileInArkcase(arkCaseFile, targetParentFolder, ecmEvent.getTargetParentNodeType());
                log.info("Moved file to ArkCase with CMIS ID [{}] and ArkCase ID [{}]", ecmEvent.getNodeId(), movedFile.getId());
                return movedFile;
            }
            catch (AcmUserActionFailedException | AcmObjectNotFoundException | AcmCreateObjectFailedException e)
            {
                log.error("Could not move file with CMIS ID [{}] to ArkCase: {}", ecmEvent.getNodeId(), e.getMessage(), e);
            }
        }
        return arkCaseFile;
    }

    /**
     * So subtypes can set the file type as needed.
     *
     * @param cmisDocument
     * @return
     */
    protected String findFileType(Document cmisDocument)
    {
        return "other";
    }

    protected Document lookupCmisDocument(String cmisRepositoryId, String nodeId)
    {
        try
        {
            CmisObject object = getFileService().findObjectById(cmisRepositoryId, nodeId);
            if (object != null && object instanceof Document)
            {
                return (Document) object;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            log.warn("Could not lookup CMIS document for node with id {}", nodeId);
            return null;
        }
    }

    protected AcmContainer lookupArkCaseContainer(Long parentFolderId)
    {
        try
        {
            AcmContainer found = getFolderService().findContainerByFolderId(parentFolderId);
            log.debug("ArkCase has container for folder with id {}", parentFolderId);
            return found;
        }
        catch (AcmObjectNotFoundException e)
        {
            log.warn("No container in ArkCase for folder: {}", parentFolderId);
            return null;
        }
    }

    protected EcmFile lookupArkCaseFile(String nodeId, Long parentFolderId)
    {
        try
        {
            EcmFile found = getFileDao().findByCmisFileIdAndFolderId(nodeId, parentFolderId);
            log.debug("ArkCase has file with CMIS ID {}: folder id is {}", nodeId, found.getId());
            return found;
        }
        catch (NoResultException e)
        {
            log.warn("No such file in ArkCase: {}", nodeId);
            return null;
        }
    }

    protected AcmFolder lookupArkCaseFolder(String folderCmisId)
    {
        try
        {
            AcmFolder found = getFolderDao().findByCmisFolderId(folderCmisId);
            log.debug("ArkCase has folder with CMIS ID {}: folder id is {}", folderCmisId, found.getId());
            return found;
        }
        catch (NoResultException e)
        {
            log.warn("No such folder in ArkCase: {}", folderCmisId);
            return null;
        }
    }

    protected boolean isMoveFileEvent(EcmEvent ecmEvent)
    {
        return EcmEventType.MOVE.equals(ecmEvent.getEcmEventType()) &&
                EcmFileConstants.ECM_SYNC_NODE_TYPE_DOCUMENT.equals(ecmEvent.getNodeType());
    }

    @Override
    public void onApplicationEvent(EcmEvent ecmEvent)
    {
        if (isMoveFileEvent(ecmEvent))
        {
            onEcmFileMoved(ecmEvent);
        }
    }

    public AcmFolderService getFolderService()
    {
        return folderService;
    }

    public void setFolderService(AcmFolderService folderService)
    {
        this.folderService = folderService;
    }

    public AuditPropertyEntityAdapter getAuditPropertyEntityAdapter()
    {
        return auditPropertyEntityAdapter;
    }

    public void setAuditPropertyEntityAdapter(AuditPropertyEntityAdapter auditPropertyEntityAdapter)
    {
        this.auditPropertyEntityAdapter = auditPropertyEntityAdapter;
    }

    public AcmFolderDao getFolderDao()
    {
        return folderDao;
    }

    public void setFolderDao(AcmFolderDao folderDao)
    {
        this.folderDao = folderDao;
    }

    public EcmFileDao getFileDao()
    {
        return fileDao;
    }

    public void setFileDao(EcmFileDao fileDao)
    {
        this.fileDao = fileDao;
    }

    public EcmFileService getFileService()
    {
        return fileService;
    }

    public void setFileService(EcmFileService fileService)
    {
        this.fileService = fileService;
    }

    public FolderAndFilesUtils getFolderAndFilesUtils()
    {
        return folderAndFilesUtils;
    }

    public void setFolderAndFilesUtils(FolderAndFilesUtils folderAndFilesUtils)
    {
        this.folderAndFilesUtils = folderAndFilesUtils;
    }
}