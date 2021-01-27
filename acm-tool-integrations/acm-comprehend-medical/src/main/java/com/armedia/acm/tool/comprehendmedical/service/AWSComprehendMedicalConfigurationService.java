package com.armedia.acm.tool.comprehendmedical.service;

/*-
 * #%L
 * acm-transcribe-tool
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

import com.armedia.acm.configuration.service.ConfigurationPropertyService;
import com.armedia.acm.tool.comprehendmedical.model.AWSComprehendMedicalConfiguration;

/**
 * Created by Riste Tutureski <riste.tutureski@armedia.com> on 05/12/2020
 */
public class AWSComprehendMedicalConfigurationService
{
    private AWSComprehendMedicalConfiguration awsComprehendMedicalConfiguration;

    private ConfigurationPropertyService configurationPropertyService;

    public void saveAWSProperties(AWSComprehendMedicalConfiguration awsComprehendMedicalConfiguration)
    {
        configurationPropertyService.updateProperties(awsComprehendMedicalConfiguration);
    }

    public AWSComprehendMedicalConfiguration loadAWSProperties()
    {
        return awsComprehendMedicalConfiguration;
    }

    public AWSComprehendMedicalConfiguration getAwsComprehendMedicalConfiguration()
    {
        return awsComprehendMedicalConfiguration;
    }

    public void setAwsComprehendMedicalConfiguration(AWSComprehendMedicalConfiguration awsComprehendMedicalConfiguration)
    {
        this.awsComprehendMedicalConfiguration = awsComprehendMedicalConfiguration;
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