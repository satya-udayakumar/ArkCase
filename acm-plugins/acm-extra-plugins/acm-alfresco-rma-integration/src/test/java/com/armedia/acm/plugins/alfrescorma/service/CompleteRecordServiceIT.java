package com.armedia.acm.plugins.alfrescorma.service;

/*-
 * #%L
 * ACM Extra Plugin: Alfresco RMA Integration
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.armedia.acm.camelcontext.context.CamelContextManager;
import com.armedia.acm.plugins.ecm.model.EcmFileConstants;
import com.armedia.acm.web.api.MDCConstants;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/spring/spring-alfresco-records-service-test.xml",
        "/spring/spring-library-alfresco-service.xml",
        "/spring/spring-library-alfresco-services-requiring-ecm.xml",
        "/spring/spring-library-alfresco-rma-integration.xml",
        "/spring/spring-library-acm-encryption.xml",
        "/spring/spring-library-property-file-manager.xml",
        "/spring/spring-library-ecm-file.xml",
        "/spring/spring-library-ecm-tika.xml",
        "/spring/spring-library-data-source.xml",
        "/spring/spring-library-context-holder.xml",
        "/spring/spring-library-pdf-utilities.xml",
        "/spring/spring-library-search.xml",
        "/spring/spring-library-user-service.xml",
        "/spring/spring-library-core-api.xml",
        "/spring/spring-library-data-access-control.xml",
        "/spring/spring-library-particpants.xml",
        "/spring/spring-library-activiti-configuration.xml",
        "/spring/spring-library-drools-rule-monitor.xml",
        "/spring/spring-library-object-lock.xml",
        "/spring/spring-library-object-converter.xml",
        "/spring/spring-library-ecm-file-lock.xml",
        "/spring/spring-library-service-data.xml",
        "/spring/spring-library-user-login.xml",
        "/spring/spring-library-plugin-manager.xml",
        "/spring/spring-library-audit-service.xml",
        "/spring/spring-library-authentication-token.xml",
        "/spring/spring-library-configuration.xml",
        "/spring/spring-library-acm-email.xml",
        "/spring/spring-library-camel-context.xml",
        "/spring/spring-test-quartz-scheduler.xml",
        "/spring/spring-library-websockets.xml",
        "/spring/spring-library-activemq.xml",
        "/spring/spring-library-folder-watcher.xml"
})
public class CompleteRecordServiceIT
{
    static
    {
        String userHomePath = System.getProperty("user.home");
        System.setProperty("acm.configurationserver.propertyfile", userHomePath + "/.arkcase/acm/conf.yml");
        System.setProperty("configuration.server.url", "http://localhost:9999");
        System.setProperty("javax.net.ssl.trustStore", userHomePath + "/.arkcase/acm/private/arkcase.ts");
        System.setProperty("javax.net.ssl.trustStorePassword", "password");
        System.setProperty("application.profile.reversed", "runtime");
    }

    private transient final Logger LOG = LogManager.getLogger(getClass());
    @Autowired
    private CamelContextManager camelContextManager;
    @Autowired
    @Qualifier("declareRecordService")
    private AlfrescoService<String> declareRecordService;
    @Autowired
    @Qualifier("setRecordMetadataService")
    private AlfrescoService<String> setRecordMetadataService;
    @Autowired
    @Qualifier("findFolderService")
    private AlfrescoService<Folder> findFolderService;
    @Autowired
    @Qualifier("createOrFindRecordFolderOrRecordCategoryService")
    private AlfrescoService<String> findRecordFolderService;
    @Autowired
    @Qualifier("moveToRecordFolderService")
    private AlfrescoService<String> moveToRecordFolderService;
    @Autowired
    @Qualifier("completeRecordService")
    private AlfrescoService<String> service;
    private String ecmFileId;
    private CmisFileWriter cmisFileWriter = new CmisFileWriter();

    @Before
    public void setUp() throws Exception
    {
        MDC.put(MDCConstants.EVENT_MDC_REQUEST_ALFRESCO_USER_ID_KEY, "admin");
        MDC.put(MDCConstants.EVENT_MDC_REQUEST_ID_KEY, UUID.randomUUID().toString());

        Document testFile = cmisFileWriter.writeTestFile(camelContextManager);
        ecmFileId = testFile.getProperty(EcmFileConstants.REPOSITORY_VERSION_ID).getValue();
    }

    @Test
    public void completeRecord() throws Exception
    {
        assertNotNull(declareRecordService);

        Map<String, Object> declareRecordContext = new HashMap<>();

        LOG.info("Working with ecmFileId {}", ecmFileId);
        declareRecordContext.put("ecmFileId", ecmFileId);

        String actedOnId = declareRecordService.service(declareRecordContext);

        assertEquals(ecmFileId, actedOnId);

        Map<String, Object> metadataContext = new HashMap<>();
        metadataContext.put("ecmFileId", ecmFileId);
        metadataContext.put("publicationDate", new Date());
        metadataContext.put("originator", "Jerry Garcia");
        metadataContext.put("originatingOrganization", "Grateful Dead");
        metadataContext.put("dateReceived", new Date());

        String metadataId = setRecordMetadataService.service(metadataContext);
        assertEquals(ecmFileId, metadataId);

        // find a category folder
        Map<String, Object> categoryContext = new HashMap<>();
        categoryContext.put("folderPath", "Complaints");
        Folder folder = findFolderService.service(categoryContext);

        // create a record folder
        Map<String, Object> recordFolderContext = new HashMap<>();
        recordFolderContext.put("parentFolder", folder);
        recordFolderContext.put("type", "Record Folder");
        String folderName = UUID.randomUUID().toString();
        recordFolderContext.put("recordFolderName", folderName);
        String recordFolderId = findRecordFolderService.service(recordFolderContext);

        // move record
        Map<String, Object> moveContext = new HashMap<>();
        moveContext.put("ecmFileId", ecmFileId);
        moveContext.put("recordFolderId", recordFolderId);
        String movedId = moveToRecordFolderService.service(moveContext);

        assertEquals(ecmFileId, movedId);

        // complete the record
        Map<String, Object> context = new HashMap<>();
        context.put("ecmFileId", ecmFileId);
        String completeMessage = service.service(context);
        assertTrue(completeMessage.contains(ecmFileId));

    }
}
