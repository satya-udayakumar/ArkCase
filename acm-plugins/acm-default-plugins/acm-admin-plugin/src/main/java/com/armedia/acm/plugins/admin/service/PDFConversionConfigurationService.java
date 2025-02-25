package com.armedia.acm.plugins.admin.service;

/*-
 * #%L
 * ACM Default Plugin: admin
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

import com.armedia.acm.configuration.service.ConfigurationPropertyService;
import com.armedia.acm.plugins.admin.model.PdfConversionConfig;

import java.util.Map;

public class PDFConversionConfigurationService
{
    private PdfConversionConfig pdfConversionConfig;
    private ConfigurationPropertyService configurationPropertyService;

    public void saveProperties(Map<String, Object> properties)
    {
        configurationPropertyService.updateProperties(properties);
    }

    public Map<String, Object> loadProperties()
    {
        return configurationPropertyService.getProperties(pdfConversionConfig);
    }

    public Boolean isResponseFolderConversionEnabled()
    {
        return pdfConversionConfig.getResponseFolderConversion();
    }

    public PdfConversionConfig getPdfConversionConfig()
    {
        return pdfConversionConfig;
    }

    public void setPdfConversionConfig(PdfConversionConfig pdfConversionConfig)
    {
        this.pdfConversionConfig = pdfConversionConfig;
    }

    public ConfigurationPropertyService getConfigurationPropertyService()
    {
        return configurationPropertyService;
    }

    public void setConfigurationPropertyService(ConfigurationPropertyService configurationPropertyService)
    {
        this.configurationPropertyService = configurationPropertyService;
    }
}
