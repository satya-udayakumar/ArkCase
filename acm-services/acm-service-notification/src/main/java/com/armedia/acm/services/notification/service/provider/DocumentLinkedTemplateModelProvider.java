package com.armedia.acm.services.notification.service.provider;

/*-
 * #%L
 * ACM Service: Notification
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

import com.armedia.acm.core.provider.TemplateModelProvider;
import com.armedia.acm.plugins.ecm.model.EcmFileVersion;
import com.armedia.acm.services.authenticationtoken.service.AuthenticationTokenService;
import com.armedia.acm.services.notification.model.Notification;
import com.armedia.acm.services.notification.service.provider.model.DocumentLinkedModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentLinkedTemplateModelProvider implements TemplateModelProvider<DocumentLinkedModel>
{

    private AuthenticationTokenService authenticationTokenService;

    @Override
    public DocumentLinkedModel getModel(Object object)
    {
        Notification notification = (Notification) object;
        List<EcmFileVersion> fileVersions = notification.getFiles();
        List<String> links = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        if (fileVersions.size() > 0)
        {
            fileVersions = fileVersions.stream()
                    .filter(filversion -> filversion.getVersionTag().equals(filversion.getFile().getActiveVersionTag()))
                    .collect(Collectors.toList());
            for (int i = 0; i < fileVersions.size(); i++)
            {
                fileNames.add(fileVersions.get(i).getFile().getFileName() + fileVersions.get(i).getFile().getFileActiveVersionNameExtension());
                links.add("ecmFileId=" + fileVersions.get(i).getFile().getFileId() + "&version=&acm_email_ticket="
                        + authenticationTokenService.generateAndSaveAuthenticationToken(fileVersions.get(i).getId(),
                                notification.getEmailAddresses(), null));
            }
        }
        return new DocumentLinkedModel(links, fileNames, notification.getRelatedObjectType(), notification.getRelatedObjectNumber());
    }

    @Override
    public Class<DocumentLinkedModel> getType()
    {
        return DocumentLinkedModel.class;
    }

    public void setAuthenticationTokenService(AuthenticationTokenService authenticationTokenService)
    {
        this.authenticationTokenService = authenticationTokenService;
    }
}