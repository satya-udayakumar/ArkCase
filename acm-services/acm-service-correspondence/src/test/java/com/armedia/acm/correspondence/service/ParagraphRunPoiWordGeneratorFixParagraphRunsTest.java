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

import static org.junit.Assert.assertEquals;

import com.armedia.acm.core.model.ApplicationConfig;
import com.armedia.acm.correspondence.utils.ParagraphRunPoiWordGenerator;
import com.armedia.acm.objectonverter.ObjectConverter;
import com.armedia.acm.plugins.ecm.dao.EcmFileDao;

import com.armedia.acm.services.templateconfiguration.service.CorrespondenceMergeFieldManager;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.easymock.EasyMockSupport;
import org.junit.Before;
import org.junit.Test;

/**
 * @author aleksandar.bujaroski
 */

public class ParagraphRunPoiWordGeneratorFixParagraphRunsTest extends EasyMockSupport
{

    private ParagraphRunPoiWordGenerator unit;

    private CorrespondenceService mockCorrespondenceService;
    private EcmFileDao mockEcmFileDao;
    private ObjectConverter mockObjectConverter;
    private CorrespondenceMergeFieldManager mockMergeFieldManager;
    private ApplicationConfig mockAppConfig;

    @Before
    public void setUp() throws Exception
    {

        mockCorrespondenceService = createMock(CorrespondenceService.class);
        mockEcmFileDao = createMock(EcmFileDao.class);
        mockObjectConverter = createMock(ObjectConverter.class);
        mockMergeFieldManager = createMock(CorrespondenceMergeFieldManager.class);
        mockAppConfig = createMock(ApplicationConfig.class);

        unit = new ParagraphRunPoiWordGenerator();
        unit.setCorrespondenceService(mockCorrespondenceService);
        unit.setEcmFileDao(mockEcmFileDao);
        unit.setObjectConverter(mockObjectConverter);
        unit.setMergeFieldManager(mockMergeFieldManager);
        unit.setAppConfig(mockAppConfig);
    }

    @Test
    public void fixOneLineBrokenRun()
    {
        // Blank Document
        XWPFDocument document = new XWPFDocument();

        // Create Paragraph
        XWPFParagraph paragraph = document.createParagraph();

        // create oneLine brokenRun
        XWPFRun run = paragraph.createRun();
        run.setText("${id}");
        unit.fixParagraphRuns(paragraph);

        assertEquals(paragraph.getRuns().size(), 3);
        assertEquals(paragraph.getRuns().get(0).getText(0), "${");
        assertEquals(paragraph.getRuns().get(1).getText(0), "id");
        assertEquals(paragraph.getRuns().get(2).getText(0), "}");
    }

    @Test
    public void fixMultilineBrokenRunt()
    {
        // Blank Document
        XWPFDocument document = new XWPFDocument();

        // Create Paragraph
        XWPFParagraph paragraph = document.createParagraph();

        // create multiline brokenRun
        paragraph.createRun().setText("${");
        paragraph.createRun().setText("par");
        paragraph.createRun().setText("agraph");
        paragraph.createRun().setText("}: ${");

        unit.fixParagraphRuns(paragraph);

        assertEquals(paragraph.getRuns().size(), 5);
        assertEquals(paragraph.getRuns().get(0).getText(0).trim(), "${");
        assertEquals(paragraph.getRuns().get(1).getText(0).trim(), "paragraph");
        assertEquals(paragraph.getRuns().get(2).getText(0).trim(), "}:");
        assertEquals(paragraph.getRuns().get(3).getText(0).trim(), ":");
        assertEquals(paragraph.getRuns().get(4).getText(0).trim(), "${");
    }

}
