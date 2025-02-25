package gov.foia.model.provider;

/*-
 * #%L
 * ACM Standard Application: Freedom of Information Act
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

import com.armedia.acm.core.model.ApplicationConfig;
import com.armedia.acm.core.provider.TemplateModelProvider;
import com.armedia.acm.correspondence.model.FormattedMergeTerm;
import com.armedia.acm.correspondence.model.FormattedRun;
import com.armedia.acm.plugins.person.service.PersonAssociationService;
import com.armedia.acm.plugins.profile.model.UserOrg;
import com.armedia.acm.plugins.profile.service.UserOrgService;
import com.armedia.acm.plugins.task.model.AcmTask;
import com.armedia.acm.plugins.task.service.TaskDao;
import com.armedia.acm.services.config.lookups.model.StandardLookupEntry;
import com.armedia.acm.services.config.lookups.service.LookupDao;
import com.armedia.acm.services.exemption.exception.GetExemptionCodeException;
import com.armedia.acm.services.exemption.model.ExemptionCode;
import com.armedia.acm.services.note.dao.NoteDao;
import com.armedia.acm.services.note.model.Note;
import com.armedia.acm.services.notification.model.Notification;
import com.armedia.acm.services.users.dao.UserDao;
import com.armedia.acm.services.users.model.AcmUser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import gov.foia.dao.FOIAExemptionCodeDao;
import gov.foia.dao.FOIARequestDao;
import gov.foia.model.FOIARequest;
import gov.foia.model.FOIATaskRequestModel;
import gov.foia.service.FOIAExemptionService;

public class FOIATaskRequestTemplateModelProvider implements TemplateModelProvider<FOIATaskRequestModel>
{

    private FOIARequestDao foiaRequestDao;
    private ApplicationConfig applicationConfig;
    private UserDao userDao;
    private UserOrgService userOrgService;
    private PersonAssociationService personAssociationService;
    private NoteDao noteDao;
    private TaskDao taskDao;
    private FOIAExemptionService foiaExemptionService;
    private FOIAExemptionCodeDao foiaExemptionCodeDao;
    private LookupDao lookupDao;

    private final Logger log = LoggerFactory.getLogger(getClass().getName());

    List<ExemptionCode> exemptionCodesOnExemptDoc;

    @Override
    public FOIATaskRequestModel getModel(Object object)
    {
        FOIARequest request = null;
        AcmTask task = null;
        if (object instanceof Notification)
        {
            if (((Notification) object).getParentType().equals("TASK"))
            {
                task = getTaskDao().findById(((Notification) object).getParentId());
            }
            else
            {
                request = getFoiaRequestDao().find(((Notification) object).getParentId());
            }

        }
        else
        {
            if (object instanceof AcmTask)
            {
                task = (AcmTask) object;
            }
            else
            {
                request = (FOIARequest) object;
            }

        }
        FOIATaskRequestModel model = new FOIATaskRequestModel();
        FormattedMergeTerm exemptionCodesAndDescription = new FormattedMergeTerm();
        FormattedMergeTerm exemptionCodesOnExemptDocument = new FormattedMergeTerm();
        FormattedMergeTerm redactionsOnReleasedDocument = new FormattedMergeTerm();
        if (task != null)
        {
            task.setTaskNotes(noteDao.listNotes("GENERAL", task.getId(), task.getObjectType()).stream().map(Note::getNote)
                    .collect(Collectors.joining("\n\n")));
            model.setTaskContact(getPersonAssociationService().getPersonsInAssociatonsByPersonType("TASK", task.getId(), "Contact Person")
                    .stream().findFirst().orElse(null));
            if (task.getParentObjectId() != null)
            {
                request = foiaRequestDao.find(task.getParentObjectId());
                if (request != null)
                {
                    exemptionCodesAndDescription = setRequestFieldsAndGetExemptionCodes(request);
                    exemptionCodesOnExemptDocument = setExemptionCodesOnExemptDocument(request);
                    redactionsOnReleasedDocument = setRedactionsOnReleasedDocument(request);
                }
            }
        }
        else
        {
            exemptionCodesAndDescription = setRequestFieldsAndGetExemptionCodes(request);
            exemptionCodesOnExemptDocument = setExemptionCodesOnExemptDocument(request);
            redactionsOnReleasedDocument = setRedactionsOnReleasedDocument(request);
        }

        model.setTask(task);
        model.setRequest(request);
        model.setExemptionCodesAndDescription(exemptionCodesAndDescription.getRuns() == null || redactionsOnReleasedDocument.getRuns().isEmpty() ? null : exemptionCodesAndDescription);
        model.setRedactions(redactionsOnReleasedDocument.getRuns() == null || redactionsOnReleasedDocument.getRuns().isEmpty() ? null : redactionsOnReleasedDocument);
        model.setExemptions(exemptionCodesOnExemptDocument.getRuns() == null || exemptionCodesOnExemptDocument.getRuns().isEmpty() ? null : exemptionCodesOnExemptDocument);
        return model;
    }

    private FormattedMergeTerm setRequestFieldsAndGetExemptionCodes(FOIARequest request)
    {
        request.setApplicationConfig(applicationConfig);
        String assigneeLdapID = request.getAssigneeLdapId();
        AcmUser assignee = null;
        FormattedMergeTerm exemptionCodesAndDescription = new FormattedMergeTerm();
        if (assigneeLdapID != null)
        {
            assignee = userDao.findByUserId(assigneeLdapID);
            UserOrg userOrg = userOrgService.getUserOrgForUserId(assigneeLdapID);
            if (userOrg != null)
            {
                request.setAssigneeTitle(userOrg.getTitle());
                request.setAssigneePhone(userOrg.getOfficePhoneNumber());
            }
            request.setAssigneeFullName(assignee.getFirstName() + " " + assignee.getLastName());
        }

        try
        {
            List<ExemptionCode> exemptionCodes = foiaExemptionService.getExemptionCodes(request.getId(), request.getObjectType());
            if (exemptionCodes != null)
            {
                List<StandardLookupEntry> lookupEntries = (List<StandardLookupEntry>) getLookupDao().getLookupByName("annotationTags")
                        .getEntries();
                Map<String, String> codeDescriptions = lookupEntries.stream()
                        .collect(Collectors.toMap(StandardLookupEntry::getKey, StandardLookupEntry::getValue));
                List<FormattedRun> runs = new ArrayList<>();
                for (ExemptionCode exCode : exemptionCodes)
                {
                    foiaExemptionService.createAndStyleRunsForCorrespondenceLetters(codeDescriptions, runs, exCode);
                }
                exemptionCodesAndDescription.setRuns(runs);
            }
        }
        catch (GetExemptionCodeException e)
        {
            log.error("Unable to get exemption codes for objectId: {}" + request.getId(), e);
            e.printStackTrace();
        }
        return exemptionCodesAndDescription;
    }

    private FormattedMergeTerm setExemptionCodesOnExemptDocument(FOIARequest request)
    {
        FormattedMergeTerm exemptionCodesForExemptFiles = new FormattedMergeTerm();
        exemptionCodesOnExemptDoc = foiaExemptionCodeDao.getExemptionCodesForExemptFilesForRequest(request.getId(),
                request.getObjectType()).stream().filter(distinctByProperty(ExemptionCode::getExemptionCode)).collect(Collectors.toList());
        if (exemptionCodesOnExemptDoc != null)
        {
            List<StandardLookupEntry> lookupEntries = (List<StandardLookupEntry>) getLookupDao().getLookupByName("annotationTags")
                    .getEntries();
            Map<String, String> codeDescriptions = lookupEntries.stream()
                    .collect(Collectors.toMap(StandardLookupEntry::getKey, StandardLookupEntry::getValue));
            List<FormattedRun> runs = new ArrayList<>();
            for (ExemptionCode exCode : exemptionCodesOnExemptDoc)
            {
                foiaExemptionService.createAndStyleRunsForCorrespondenceLetters(codeDescriptions, runs, exCode);
            }
            exemptionCodesForExemptFiles.setRuns(runs);
        }
        return exemptionCodesForExemptFiles;
    }

    private FormattedMergeTerm setRedactionsOnReleasedDocument(FOIARequest request)
    {
        FormattedMergeTerm redactionsForReleasedDocuments = new FormattedMergeTerm();
        List<ExemptionCode> exemptionCodes = foiaExemptionCodeDao.getResponseFolderExemptionCodesForRequest(request).stream()
                .filter(distinctByProperty(ExemptionCode::getExemptionCode)).collect(Collectors.toList());
        if (exemptionCodes != null)
        {
            List<String> mappedExemptionCodesOnExemptDoc = exemptionCodesOnExemptDoc.stream().map(ExemptionCode::getExemptionCode).collect(Collectors.toList());
            exemptionCodes = exemptionCodes.stream().filter(element-> !mappedExemptionCodesOnExemptDoc.contains(element.getExemptionCode())).collect(Collectors.toList());

            List<StandardLookupEntry> lookupEntries = (List<StandardLookupEntry>) getLookupDao().getLookupByName("annotationTags")
                    .getEntries();
            Map<String, String> codeDescriptions = lookupEntries.stream()
                    .collect(Collectors.toMap(StandardLookupEntry::getKey, StandardLookupEntry::getValue));
            List<FormattedRun> runs = new ArrayList<>();
            for (ExemptionCode exCode : exemptionCodes)
            {
                foiaExemptionService.createAndStyleRunsForCorrespondenceLetters(codeDescriptions, runs, exCode);
            }
            redactionsForReleasedDocuments.setRuns(runs);
        }
        return redactionsForReleasedDocuments;
    }

    private static <T> Predicate<T> distinctByProperty(Function<? super T, ?> propertyExtractor)
    {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(propertyExtractor.apply(t));
    }

    @Override
    public Class<FOIATaskRequestModel> getType()
    {
        return FOIATaskRequestModel.class;
    }

    public FOIARequestDao getFoiaRequestDao()
    {
        return foiaRequestDao;
    }

    public void setFoiaRequestDao(FOIARequestDao foiaRequestDao)
    {
        this.foiaRequestDao = foiaRequestDao;
    }

    public ApplicationConfig getApplicationConfig()
    {
        return applicationConfig;
    }

    public void setApplicationConfig(ApplicationConfig applicationConfig)
    {
        this.applicationConfig = applicationConfig;
    }

    public UserDao getUserDao()
    {
        return userDao;
    }

    public void setUserDao(UserDao userDao)
    {
        this.userDao = userDao;
    }

    public UserOrgService getUserOrgService()
    {
        return userOrgService;
    }

    public void setUserOrgService(UserOrgService userOrgService)
    {
        this.userOrgService = userOrgService;
    }

    public PersonAssociationService getPersonAssociationService()
    {
        return personAssociationService;
    }

    public void setPersonAssociationService(PersonAssociationService personAssociationService)
    {
        this.personAssociationService = personAssociationService;
    }

    public NoteDao getNoteDao()
    {
        return noteDao;
    }

    public void setNoteDao(NoteDao noteDao)
    {
        this.noteDao = noteDao;
    }

    public TaskDao getTaskDao()
    {
        return taskDao;
    }

    public void setTaskDao(TaskDao taskDao)
    {
        this.taskDao = taskDao;
    }

    public FOIAExemptionService getFoiaExemptionService()
    {
        return foiaExemptionService;
    }

    public void setFoiaExemptionService(FOIAExemptionService foiaExemptionService)
    {
        this.foiaExemptionService = foiaExemptionService;
    }

    public LookupDao getLookupDao()
    {
        return lookupDao;
    }

    public void setLookupDao(LookupDao lookupDao)
    {
        this.lookupDao = lookupDao;
    }

    public FOIAExemptionCodeDao getFoiaExemptionCodeDao()
    {
        return foiaExemptionCodeDao;
    }

    public void setFoiaExemptionCodeDao(FOIAExemptionCodeDao foiaExemptionCodeDao)
    {
        this.foiaExemptionCodeDao = foiaExemptionCodeDao;
    }
}
