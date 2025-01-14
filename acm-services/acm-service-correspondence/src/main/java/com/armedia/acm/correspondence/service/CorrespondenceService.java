package com.armedia.acm.correspondence.service;

/*-
 * #%L
 * ACM Service: Correspondence Library
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
import com.armedia.acm.core.exceptions.AcmUserActionFailedException;
import com.armedia.acm.core.provider.TemplateModelProvider;
import com.armedia.acm.data.AcmAbstractDao;
import com.armedia.acm.data.AcmEntity;
import com.armedia.acm.plugins.ecm.model.EcmFile;
import com.armedia.acm.services.notification.model.Notification;
import com.armedia.acm.services.templateconfiguration.model.CorrespondenceMergeField;
import com.armedia.acm.services.templateconfiguration.model.Template;

import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author darko.dimitrievski
 */
public interface CorrespondenceService
{
    /**
     * For use from MVC controllers and any other client with an Authentication object.
     *
     * @param authentication
     * @param templateName
     * @param parentObjectType
     * @param parentObjectId
     * @param targetCmisFolderId
     * @return EcmFile
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws AcmCreateObjectFailedException
     */
    EcmFile generate(Authentication authentication, String templateName, String parentObjectType, Long parentObjectId,
            String targetCmisFolderId)
            throws IOException, IllegalArgumentException, AcmCreateObjectFailedException, AcmUserActionFailedException;

    EcmFile generate(Authentication authentication, String templateName, String parentObjectType, Long parentObjectId,
            String targetCmisFolderId, Boolean isManual)
            throws IOException, IllegalArgumentException, AcmCreateObjectFailedException, AcmUserActionFailedException;

    /**
     * Helper method for use from Activiti and other clients with no direct access to an Authentication, but in the call
     * stack of a Spring MVC authentication... so there is an Authentication in the Spring Security context holder.
     */
    EcmFile generate(String templateName, String parentObjectType, Long parentObjectId, String targetCmisFolderId)
            throws IOException, IllegalArgumentException, AcmCreateObjectFailedException, AcmUserActionFailedException;

    List<Template> getAllTemplates();

    List<Template> getActiveVersionTemplates();

    List<Template> getActivatedActiveVersionTemplatesByObjectType(String objectType);

    List<Template> getActiveVersionTemplatesByTemplateType(String objectType);

    /**
     * @param templateId
     * @return
     */
    List<Template> getTemplateVersionsById(String templateId);

    /**
     * @param templateId
     * @return
     */
    Optional<Template> getActiveTemplateById(String templateId);

    /**
     * @param templateId
     * @param templateVersion
     * @return
     */
    Optional<Template> getTemplateByIdAndVersion(String templateId, String templateVersion);

    /**
     * @param templateId
     * @param templateFilename
     * @return
     */

    Optional<Template> getTemplateByIdAndFilename(String templateId, String templateFilename);

    /**
     * @param templateId
     * @param templateVersion
     * @return
     * @throws IOException
     */
    Optional<Template> deleteTemplateByIdAndVersion(String templateId, String templateVersion) throws IOException;

    List<CorrespondenceMergeField> getMergeFields();

    /**
     * @param objectType
     * @return
     * @throws IOException,
     *             CorrespondenceMergeFieldVersionException
     */
    List<CorrespondenceMergeField> getMergeFieldsByType(String objectType);

    /**
     * @param mergeFieldId
     * @return
     * @throws IOException,
     *             CorrespondenceMergeFieldVersionException
     */
    List<CorrespondenceMergeField> getMergeFieldByMergeFieldId(String mergeFieldId);

    /**
     * @param mergeField
     * @return
     * @throws IOException,
     *             CorrespondenceMergeFieldVersionException
     */
    void deleteMergeFields(String mergeFieldId) throws IOException;

    /**
     * @param newMergeField
     * @return
     */
    void addMergeField(CorrespondenceMergeField newMergeField) throws IOException;

    /**
     * @param mergeFields
     * @param auth
     * @return
     * @throws IOException
     */
    List<CorrespondenceMergeField> saveMergeFieldsData(List<CorrespondenceMergeField> mergeFields, Authentication auth)
            throws IOException;

    /**
     * @param template
     * @throws IOException
     */
    Optional<Template> updateTemplate(Template template) throws IOException;

    /**
     * Generating Dao objects depending from the object type
     *
     * @param objectType
     */
    AcmAbstractDao<AcmEntity> getAcmAbstractDao(String objectType);

    /**
     * Listing all template model providers
     */
    Map<String, String> listTemplateModelProviders();

    /**
     * Listing all declared fields for given template model provider classpath
     */
    String getTemplateModelProviderDeclaredFields(String classPath);

    /**
     * Finding templateModelProvider class by name
     */
    TemplateModelProvider getTemplateModelProvider(Class templateModelProviderClass);

    /**
     * Translating merge terms by objectId and objectType
     */
    Notification convertMergeTerms(String templateName, String templateContent, String objectType, String objectId, List<Long> fileIds);
}
