package com.armedia.acm.service.outlook.dao.impl;

/*-
 * #%L
 * ACM Service: MS Outlook integration
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

import com.armedia.acm.core.exceptions.AcmOutlookConnectionFailedException;
import com.armedia.acm.core.exceptions.AcmOutlookCreateItemFailedException;
import com.armedia.acm.core.exceptions.AcmOutlookException;
import com.armedia.acm.core.exceptions.AcmOutlookFindItemsFailedException;
import com.armedia.acm.core.exceptions.AcmOutlookItemNotDeletedException;
import com.armedia.acm.core.exceptions.AcmOutlookItemNotFoundException;
import com.armedia.acm.core.exceptions.AcmOutlookModifyItemFailedException;
import com.armedia.acm.service.outlook.dao.OutlookDao;
import com.armedia.acm.service.outlook.model.AcmOutlookUser;
import com.armedia.acm.service.outlook.model.OutlookCalendarItem;
import com.armedia.acm.service.outlook.model.OutlookConfig;
import com.armedia.acm.service.outlook.model.OutlookContactItem;
import com.armedia.acm.service.outlook.model.OutlookFolder;
import com.armedia.acm.service.outlook.model.OutlookFolderPermission;
import com.armedia.acm.service.outlook.model.OutlookItem;
import com.armedia.acm.service.outlook.model.OutlookTaskItem;
import com.armedia.acm.service.outlook.service.impl.ExchangeConfigurationServiceImpl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.permission.folder.FolderPermissionLevel;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.EmailAddressKey;
import microsoft.exchange.webservices.data.core.enumeration.property.PhoneNumberKey;
import microsoft.exchange.webservices.data.core.enumeration.property.StandardUser;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.SortDirection;
import microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode;
import microsoft.exchange.webservices.data.core.enumeration.service.SendCancellationsMode;
import microsoft.exchange.webservices.data.core.enumeration.service.calendar.AffectedTaskOccurrence;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.Appointment;
import microsoft.exchange.webservices.data.core.service.item.Contact;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.item.Task;
import microsoft.exchange.webservices.data.core.service.schema.FolderSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.EmailAddress;
import microsoft.exchange.webservices.data.property.complex.FolderId;
import microsoft.exchange.webservices.data.property.complex.FolderPermission;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import microsoft.exchange.webservices.data.property.complex.recurrence.pattern.Recurrence;
import microsoft.exchange.webservices.data.property.complex.time.OlsonTimeZoneDefinition;
import microsoft.exchange.webservices.data.property.definition.PropertyDefinition;
import microsoft.exchange.webservices.data.search.FindFoldersResults;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.FolderView;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;

/**
 * Created by armdev on 4/20/15.
 */
public class ExchangeWebServicesOutlookDao implements OutlookDao
{
    private transient final Logger log = LogManager.getLogger(getClass());
    private final PropertySet standardProperties = new PropertySet(BasePropertySet.IdOnly, ItemSchema.Subject, ItemSchema.DateTimeSent,
            ItemSchema.DateTimeCreated, ItemSchema.DateTimeReceived, ItemSchema.LastModifiedTime, ItemSchema.Body, ItemSchema.Size);
    private final PropertySet folderProperties = new PropertySet(FolderSchema.Id, FolderSchema.DisplayName, FolderSchema.ParentFolderId,
            FolderSchema.Permissions);
    private Map<String, PropertyDefinition> sortFields;
    private ExchangeConfigurationServiceImpl exchangeConfigurationService;
    private OutlookConfig outlookConfig;

    @Override
    public ExchangeService connect(AcmOutlookUser user) throws AcmOutlookConnectionFailedException
    {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(user.getOutlookPassword(), "Password cannot be null");
        Objects.requireNonNull(user.getEmailAddress(), "E-mail address cannot be null");

        ExchangeCredentials credentials = new WebCredentials(user.getEmailAddress(), user.getOutlookPassword());

        String exchangeVersion = outlookConfig.getServerVersion();
        ExchangeVersion version;
        try
        {
            version = ExchangeVersion.valueOf(exchangeVersion);
        }
        catch (IllegalArgumentException e)
        {
            version = ExchangeVersion.Exchange2007_SP1;
            log.warn("Configured exchange version [{}] is not valid. Use default [{}]", exchangeVersion, version);
        }

        try
        {
            ExchangeService service = new ExchangeService(version);
            service.setCredentials(credentials);
            if (outlookConfig.getEnableAutoDiscovery())
            {
                service.autodiscoverUrl(user.getEmailAddress(), redirectionUrl -> true);
            }
            else
            {
                service.setUrl(URI.create(outlookConfig.getClientAccessServer()));
            }

            return service;
        }
        catch (Exception e)
        {
            log.error("Could not connect to Exchange: [{}]", e.getMessage(), e);
            throw new AcmOutlookConnectionFailedException(e.getMessage(), e);
        }
    }

    @Override
    public FindItemsResults<Item> findItems(ExchangeService service, WellKnownFolderName wellKnownFolderName,
            PropertySet extraFieldsToRetrieve, int start, int maxItems, String sortProperty, boolean sortAscending, SearchFilter filter)
            throws AcmOutlookFindItemsFailedException
    {

        Folder folder = getFolder(service, wellKnownFolderName);
        return findItems(service, folder, extraFieldsToRetrieve, start, maxItems, sortProperty, sortAscending, filter);
    }

    @Override
    public FindItemsResults<Item> findItems(ExchangeService service, String folderId, PropertySet extraFieldsToRetrieve, int start,
            int maxItems, String sortProperty, boolean sortAscending, SearchFilter filter) throws AcmOutlookFindItemsFailedException
    {

        Folder folder = getFolder(service, folderId);
        return findItems(service, folder, extraFieldsToRetrieve, start, maxItems, sortProperty, sortAscending, filter);
    }

    private FindItemsResults<Item> findItems(ExchangeService service, Folder folder, PropertySet extraFieldsToRetrieve, int start,
            int maxItems, String sortProperty, boolean sortAscending, SearchFilter filter) throws AcmOutlookFindItemsFailedException
    {
        try
        {
            log.debug("finding items");

            Objects.requireNonNull(service, "Service cannot be null");

            ItemView view = new ItemView(maxItems, start);

            PropertyDefinition orderBy = sortProperty == null || sortProperty.trim().isEmpty() || !getSortFields().containsKey(sortProperty)
                    ? ItemSchema.DateTimeReceived
                    : getSortFields().get(sortProperty);

            SortDirection sortDirection = sortAscending ? SortDirection.Ascending : SortDirection.Descending;

            view.getOrderBy().add(orderBy, sortDirection);

            FindItemsResults<Item> findResults = filter == null ? service.findItems(folder.getId(), view)
                    : service.findItems(folder.getId(), filter, view);

            PropertySet allProperties = new PropertySet();

            allProperties.addRange(standardProperties);

            allProperties.addRange(extraFieldsToRetrieve);

            if (!findResults.getItems().isEmpty())
            {
                service.loadPropertiesForItems(findResults.getItems(), allProperties);
            }

            return findResults;
        }
        catch (Exception e)
        {
            log.error("Could not list items: " + e.getMessage(), e);
            throw new AcmOutlookFindItemsFailedException(e.getMessage(), e);
        }

    }

    @Override
    public OutlookCalendarItem createCalendarAppointment(ExchangeService service, Folder folder, OutlookCalendarItem calendarItem)
            throws AcmOutlookCreateItemFailedException
    {
        try
        {

            Appointment appointment = new Appointment(service);
            appointment.setSubject(calendarItem.getSubject());
            appointment.setBody(MessageBody.getMessageBodyFromText(calendarItem.getBody()));

            OlsonTimeZoneDefinition timeZoneDefinition = new OlsonTimeZoneDefinition(TimeZone.getTimeZone("UTC"));

            appointment.setStartTimeZone(timeZoneDefinition);
            appointment.setStart(calendarItem.getStartDate());
            appointment.setEnd(calendarItem.getEndDate());

            // Adding extended property to the appointment if there is extended property in the provided item
            if (calendarItem.getExtendedPropertyDefinition() != null)
            {
                appointment.setExtendedProperty(calendarItem.getExtendedPropertyDefinition(), calendarItem.getExtendedPropertyValue());
            }

            if (calendarItem.getAllDayEvent() != null && calendarItem.getAllDayEvent())
            {
                appointment.setIsAllDayEvent(calendarItem.getAllDayEvent());
            }

            if (calendarItem.getRecurring() != null && calendarItem.getRecurring())
            {
                appointment.setRecurrence(new Recurrence.DailyPattern(calendarItem.getStartDate(), calendarItem.getRecurringInterval()));
                appointment.getRecurrence().setStartDate(calendarItem.getStartDate());
                appointment.getRecurrence().setEndDate(calendarItem.getRecurringEndDate());
            }

            appointment.save(folder.getId());
            fillCreatedItemDetails(calendarItem, appointment);
        }
        catch (Exception e)
        {
            throw new AcmOutlookCreateItemFailedException("Error creating appointment!", e);
        }
        return calendarItem;
    }

    @Override
    public OutlookTaskItem createTaskItem(ExchangeService service, Folder folder, OutlookTaskItem taskItem)
            throws AcmOutlookCreateItemFailedException
    {
        try
        {
            Task task = new Task(service);

            task.setSubject(taskItem.getSubject());
            task.setBody(MessageBody.getMessageBodyFromText(taskItem.getBody()));

            task.setPercentComplete(taskItem.getPercentComplete());

            task.setDueDate(taskItem.getDueDate());
            task.setStartDate(taskItem.getStartDate());
            task.save(folder.getId());

            fillCreatedItemDetails(taskItem, task);
        }
        catch (Exception e)
        {
            throw new AcmOutlookCreateItemFailedException("Error creating task!", e);
        }
        return taskItem;
    }

    @Override
    public OutlookContactItem createContactItem(ExchangeService service, Folder folder, OutlookContactItem contactItem)
            throws AcmOutlookCreateItemFailedException
    {
        try
        {
            Contact contact = new Contact(service);
            contact.setSurname(contactItem.getSurname());
            contact.setSubject(contactItem.getSubject());
            contact.setCompanyName(contactItem.getCompanyName());
            contact.setDisplayName(contactItem.getDisplayName());

            if (contactItem.getPrimaryTelephone() != null)
            {
                contact.getPhoneNumbers().setPhoneNumber(PhoneNumberKey.PrimaryPhone, contactItem.getPrimaryTelephone());
            }

            if (contactItem.getEmailAddress1() != null)
            {
                contact.getEmailAddresses().setEmailAddress(EmailAddressKey.EmailAddress1,
                        new EmailAddress(contactItem.getEmailAddress1()));
            }
            if (contactItem.getEmailAddress2() != null)
            {
                contact.getEmailAddresses().setEmailAddress(EmailAddressKey.EmailAddress2,
                        new EmailAddress(contactItem.getEmailAddress2()));
            }

            contact.save(folder.getId());

            fillCreatedItemDetails(contactItem, contact);
        }
        catch (Exception e)
        {
            throw new AcmOutlookCreateItemFailedException("Error creating contact!", e);
        }
        return contactItem;
    }

    private void fillCreatedItemDetails(OutlookItem outlookItem, Item item) throws ServiceLocalException
    {
        outlookItem.setId(item.getId().getUniqueId());
    }

    @Override
    public void deleteItem(ExchangeService service, String itemId, DeleteMode deleteMode)
    {
        try
        {
            Item item = Item.bind(service, new ItemId(itemId));
            item.delete(deleteMode);
        }
        catch (Exception e)
        {
            throw new AcmOutlookException("Error deleting item with id = " + itemId, e);
        }
    }

    @Override
    public void deleteAppointmentItem(ExchangeService service, String itemId, boolean recurring, DeleteMode deleteMode)
    {
        try
        {
            Appointment item = null;

            if (recurring)
            {
                service.deleteItem(new ItemId(itemId), deleteMode, SendCancellationsMode.SendOnlyToAll,
                        AffectedTaskOccurrence.AllOccurrences);
            }
            else
            {
                item = Appointment.bind(service, new ItemId(itemId));
                item.delete(deleteMode);
            }
        }
        catch (Exception e)
        {
            throw new AcmOutlookException("Error deleting item with id = " + itemId, e);
        }
    }

    @Override
    public OutlookFolder createFolder(ExchangeService service, String owner, WellKnownFolderName parentFolderName, OutlookFolder newFolder)
            throws AcmOutlookCreateItemFailedException
    {
        Folder parent = getFolder(service, parentFolderName);
        return createFolder(service, owner, parent, newFolder);
    }

    @Override
    public OutlookFolder createFolder(ExchangeService service, String owner, String parentFolderId, OutlookFolder newFolder)
            throws AcmOutlookCreateItemFailedException
    {
        Folder parent = getFolder(service, parentFolderId);
        return createFolder(service, owner, parent, newFolder);
    }

    private OutlookFolder createFolder(ExchangeService service, String owner, Folder parentFolder, OutlookFolder newFolder)
            throws AcmOutlookCreateItemFailedException
    {
        try
        {
            Folder folder = new Folder(service);
            folder.setDisplayName(newFolder.getDisplayName());
            // add default permissions
            folder.getPermissions().add(new FolderPermission(StandardUser.Anonymous, FolderPermissionLevel.None));
            if (outlookConfig.getDefaultAccess() != null)
            {
                folder.getPermissions()
                        .add(new FolderPermission(StandardUser.Default, FolderPermissionLevel.valueOf(outlookConfig.getDefaultAccess())));
            }
            else
            {
                folder.getPermissions().add(new FolderPermission(StandardUser.Default, FolderPermissionLevel.None));
            }
            folder.getPermissions().add(new FolderPermission(owner, FolderPermissionLevel.Owner));

            Set<String> addedAlready = new HashSet<>();
            addedAlready.add(owner);
            // add extra permissions
            if (newFolder.getPermissions() != null && !newFolder.getPermissions().isEmpty())
            {
                List<OutlookFolderPermission> addThese = new ArrayList<>();
                newFolder.getPermissions().stream().filter(maybeAdd -> !addedAlready.contains(maybeAdd.getEmail())).forEach(maybeAdd -> {
                    addedAlready.add(maybeAdd.getEmail());
                    addThese.add(maybeAdd);
                });
                addFolderPermissions(folder, addThese);
            }
            if (!StringUtils.isEmpty(parentFolder.getFolderClass()))
            {
                folder.setFolderClass(parentFolder.getFolderClass());
            }
            folder.save(parentFolder.getId());
            newFolder.setId(folder.getId().getUniqueId());
        }
        catch (Exception e)
        {
            log.error("Can't create folder.", e);
            throw new AcmOutlookCreateItemFailedException("Can't create folder.", e);
        }

        return newFolder;
    }

    @Override
    public void deleteFolder(ExchangeService service, String folderId, DeleteMode deleteMode) throws AcmOutlookItemNotFoundException
    {
        try
        {
            Folder folder = Folder.bind(service, new FolderId(folderId));
            ItemView view = new ItemView(10_000);
            FindItemsResults<Item> folderItems = folder.findItems(view);
            for (Item item : folderItems.getItems())
            {
                String itemId = item.getId().getUniqueId();
                service.deleteItem(new ItemId(itemId), deleteMode, SendCancellationsMode.SendOnlyToAll,
                        AffectedTaskOccurrence.AllOccurrences);
            }
            service.deleteFolder(new FolderId(folderId), deleteMode);
        }
        catch (Exception e)
        {
            log.warn("Folder can't be deleted with id={} and delete_mode={}", folderId, deleteMode);
            throw new AcmOutlookItemNotDeletedException("Folder can't be deleted with id=" + folderId + " and delete_mode=" + deleteMode,
                    e);
        }
    }

    @Override
    public FindFoldersResults findFolders(ExchangeService service, String parentFolderId, int start, int maxItems, String sortProperty,
            boolean sortAscending) throws AcmOutlookFindItemsFailedException
    {
        Folder f = getFolder(service, parentFolderId);
        return findFolders(service, f, start, maxItems, sortProperty, sortAscending);
    }

    @Override
    public FindFoldersResults findFolders(ExchangeService service, WellKnownFolderName wellKnownFolderName, int start, int maxItems,
            String sortProperty, boolean sortAscending) throws AcmOutlookFindItemsFailedException
    {
        Folder f = getFolder(service, wellKnownFolderName);
        return findFolders(service, f, start, maxItems, sortProperty, sortAscending);
    }

    private FindFoldersResults findFolders(ExchangeService service, Folder folder, int start, int maxItems, String sortProperty,
            boolean sortAscending) throws AcmOutlookFindItemsFailedException
    {
        FolderView view = new FolderView(maxItems, start);

        try
        {
            FindFoldersResults findResults = service.findFolders(folder.getId(), view);

            return findResults;
        }
        catch (Exception e)
        {
            log.error("Could not list folders: " + e.getMessage(), e);
            throw new AcmOutlookFindItemsFailedException(e.getMessage(), e);
        }
    }

    @Override
    public void addFolderPermissions(ExchangeService service, String folderId, List<OutlookFolderPermission> permissions)
            throws AcmOutlookException
    {
        Folder folder = getFolder(service, folderId);
        try
        {
            addFolderPermissions(folder, permissions);
            folder.update();
        }
        catch (Exception e)
        {
            log.warn("Can't add permissions on folder with id={}", folderId);
            throw new AcmOutlookModifyItemFailedException("Can't add permissions on folder with id" + folderId, e);
        }
    }

    @Override
    public void addFolderPermission(ExchangeService service, String folderId, OutlookFolderPermission permission)
            throws AcmOutlookItemNotFoundException
    {
        Folder folder = getFolder(service, folderId);
        try
        {
            folder.getPermissions().add(getFolderPermission(permission));
            folder.update();
        }
        catch (Exception e)
        {
            log.warn("Can't add permission = {} on folder with id = {}", permission, folderId);
            throw new AcmOutlookModifyItemFailedException("Can't add permission on folder with id" + folderId, e);
        }
    }

    @Override
    public void removeFolderPermissions(ExchangeService service, String folderId, List<OutlookFolderPermission> permissions)
            throws AcmOutlookItemNotFoundException
    {
        for (OutlookFolderPermission ofp : permissions)
        {
            removeFolderPermission(service, folderId, ofp);
        }
    }

    @Override
    public void removeFolderPermission(ExchangeService service, String folderId, OutlookFolderPermission permission)
            throws AcmOutlookItemNotFoundException
    {
        Folder folder = getFolder(service, folderId);
        try
        {
            FolderPermission toBeRemoved = getFolderPermission(permission);
            for (int i = folder.getPermissions().getItems().size() - 1; i >= 0; i--)
            {
                FolderPermission p = folder.getPermissions().getItems().get(i);
                if (p.getUserId().getPrimarySmtpAddress() != null
                        && p.getUserId().getPrimarySmtpAddress().equals(toBeRemoved.getUserId().getPrimarySmtpAddress()))
                {
                    folder.getPermissions().removeAt(i);
                }
            }
            folder.update();
        }
        catch (Exception e)
        {
            log.warn("Can't remove permission = {} on folder with id={}", permission, folderId);
            throw new AcmOutlookModifyItemFailedException("Can't remove permission on folder with id" + folderId, e);
        }
    }

    @Override
    public Folder getFolder(ExchangeService service, WellKnownFolderName wellKnownFolderName) throws AcmOutlookItemNotFoundException
    {
        Folder folder;
        try
        {
            folder = Folder.bind(service, wellKnownFolderName);
        }
        catch (Exception e)
        {
            log.warn("Folder not found with id={}", wellKnownFolderName);
            throw new AcmOutlookItemNotFoundException("Folder not found with name=" + wellKnownFolderName.name(), e);
        }
        return folder;
    }

    @Override
    public Folder getFolder(ExchangeService service, String folderId) throws AcmOutlookItemNotFoundException
    {
        Folder folder;
        try
        {
            folder = Folder.bind(service, new FolderId(folderId), folderProperties);
        }
        catch (Exception e)
        {
            log.warn("Folder not found with id={}", folderId);
            throw new AcmOutlookItemNotFoundException("Folder not found with id=" + folderId, e);
        }
        return folder;
    }

    private void addFolderPermissions(Folder folder, List<OutlookFolderPermission> permissions) throws Exception
    {
        for (OutlookFolderPermission ofp : permissions)
        {
            folder.getPermissions().add(getFolderPermission(ofp));
        }
    }

    private FolderPermission getFolderPermission(OutlookFolderPermission ofp)
    {
        FolderPermission fp = new FolderPermission(ofp.getEmail(), ofp.getLevel());
        if (ofp.getLevel().equals(FolderPermissionLevel.Custom))
        {
            // Write
            fp.setCanCreateItems(ofp.canCreateItems());
            fp.setCanCreateSubFolders(ofp.canCreateSubFolders());
            fp.setEditItems(ofp.getEditItems());

            // Delete items
            fp.setDeleteItems(ofp.getDeleteItems());

            // Read
            fp.setReadItems(ofp.getReadItems());

            // Other
            fp.setIsFolderContact(ofp.isFolderContact());
            fp.setIsFolderOwner(ofp.isFolderOwner());
            fp.setIsFolderVisible(ofp.isFolderVisible());
        }
        return fp;
    }

    public Map<String, PropertyDefinition> getSortFields()
    {
        return sortFields;
    }

    public void setSortFields(Map<String, PropertyDefinition> sortFields)
    {
        this.sortFields = sortFields;
    }

    /**
     * @return the exchangeConfigurationService
     */
    public ExchangeConfigurationServiceImpl getExchangeConfigurationService()
    {
        return exchangeConfigurationService;
    }

    /**
     * @param exchangeConfigurationService
     *            the exchangeConfigurationService to set
     */
    public void setExchangeConfigurationService(ExchangeConfigurationServiceImpl exchangeConfigurationService)
    {
        this.exchangeConfigurationService = exchangeConfigurationService;
    }

    public OutlookConfig getOutlookConfig()
    {
        return outlookConfig;
    }

    public void setOutlookConfig(OutlookConfig outlookConfig)
    {
        this.outlookConfig = outlookConfig;
    }
}
