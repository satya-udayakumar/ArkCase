package com.armedia.acm.plugins.consultation.pipeline.postsave;

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

import com.armedia.acm.core.exceptions.AcmCreateObjectFailedException;
import com.armedia.acm.core.exceptions.AcmUserActionFailedException;
import com.armedia.acm.plugins.consultation.model.Consultation;
import com.armedia.acm.plugins.consultation.pipeline.ConsultationPipelineContext;
import com.armedia.acm.plugins.ecm.model.AcmMultipartFile;
import com.armedia.acm.plugins.ecm.service.EcmFileService;
import com.armedia.acm.services.pipeline.exception.PipelineProcessException;
import com.armedia.acm.services.pipeline.handler.PipelineHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

/**
 * Created by Vladimir Cherepnalkovski <vladimir.cherepnalkovski@armedia.com> on May, 2020
 */
public class ConsultationUploadAttachmentsHandler implements PipelineHandler<Consultation, ConsultationPipelineContext>
{
    private transient final Logger log = LogManager.getLogger(getClass());
    private EcmFileService ecmFileService;

    @Override
    public void execute(Consultation entity, ConsultationPipelineContext pipelineContext) throws PipelineProcessException
    {
        List<AcmMultipartFile> files = null;

        if (pipelineContext.hasProperty("attachmentFiles"))
        {
            files = (List<AcmMultipartFile>) pipelineContext.getPropertyValue("attachmentFiles");
        }

        if (files != null)
        {
            for (AcmMultipartFile file : files)
            {
                if (file != null)
                {

                    String folderId = entity.getContainer().getAttachmentFolder() == null
                            ? entity.getContainer().getFolder().getCmisFolderId()
                            : entity.getContainer().getAttachmentFolder().getCmisFolderId();

                    log.debug("Uploading document for Consultation [{}] as [{}]", entity.getId(), file.getOriginalFilename());

                    try
                    {
                        getEcmFileService().upload(file.getOriginalFilename(), "attachment", "Document", file.getInputStream(),
                                file.getContentType(),
                                file.getOriginalFilename(), pipelineContext.getAuthentication(),
                                folderId, entity.getObjectType(), entity.getId());
                    }
                    catch (AcmCreateObjectFailedException | AcmUserActionFailedException | IOException e)
                    {
                        log.error(String.format("Could not upload attachment files for Consultation: %s", entity.getConsultationNumber()));
                    }
                }
            }
        }
    }

    @Override
    public void rollback(Consultation entity, ConsultationPipelineContext pipelineContext) throws PipelineProcessException
    {

    }

    public EcmFileService getEcmFileService()
    {
        return ecmFileService;
    }

    public void setEcmFileService(EcmFileService ecmFileService)
    {
        this.ecmFileService = ecmFileService;
    }
}
