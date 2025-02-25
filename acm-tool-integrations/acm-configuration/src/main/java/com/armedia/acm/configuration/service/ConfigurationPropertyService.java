package com.armedia.acm.configuration.service;

/*-
 * #%L
 * ACM Tool Integrations: Configuration Library
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

import com.armedia.acm.configuration.core.ConfigurationContainer;
import com.armedia.acm.configuration.model.ConfigurationClientConfig;
import com.armedia.acm.core.DynamicApplicationConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("configurationPropertyService")
public class ConfigurationPropertyService implements InitializingBean
{
    private final ConfigurationContainer configurationContainer;

    private final RestTemplate configRestTemplate;

    private final ConfigurableEnvironment configurableEnvironment;

    private final ConfigurationClientConfig configurationClientConfig;

    private final ObjectMapper objectMapper;

    public ConfigurationPropertyService(ConfigurationContainer configurationContainer,
                                        RestTemplate configRestTemplate,
                                        ConfigurableEnvironment configurableEnvironment,
                                        ConfigurationClientConfig configurationClientConfig,
                                        @Qualifier("sourceObjectMapper") ObjectMapper objectMapper)
    {
        this.configurationContainer = configurationContainer;
        this.configRestTemplate = configRestTemplate;
        this.configurableEnvironment = configurableEnvironment;
        this.configurationClientConfig = configurationClientConfig;
        this.objectMapper = objectMapper;
    }

    private String updatePropertiesEndpoint;

    private String removePropertiesEndpoint;

    private String resetPropertiesEndpoint;

    private String resetFilePropertiesEndpoint;

    private static final Logger log = LogManager.getLogger(ConfigurationPropertyService.class);

    @Override
    public void afterPropertiesSet()
    {
        String updatePath = (String) configurableEnvironment.getPropertySources()
                .get("bootstrap").getProperty("configuration.server.update.path");
        String removePath = (String) configurableEnvironment.getPropertySources()
                .get("bootstrap").getProperty("configuration.server.remove.path");
        String serverUrl = (String) configurableEnvironment.getPropertySources()
                .get("bootstrap").getProperty("configuration.server.url");
        String resetPath = (String) configurableEnvironment.getPropertySources()
                .get("bootstrap").getProperty("configuration.server.reset.path");
        updatePropertiesEndpoint = String.format("%s%s", serverUrl, updatePath);
        removePropertiesEndpoint = String.format("%s%s", serverUrl, removePath);
        resetPropertiesEndpoint = String.format("%s%s%s", serverUrl, "/config", resetPath);
        resetFilePropertiesEndpoint = String.format("%s%s", resetPropertiesEndpoint, "/{applicationName}");
    }

    public Object getProperty(String propertyKey)
    {
        return configurationContainer.getProperty(propertyKey);
    }

    public Map<String, Object> getObjectDynamicProperties(Object config)
    {
        if (config == null)
        {
            return new HashMap<>();
        }

        Class configClass = AopUtils.getTargetClass(config);

        if (DynamicApplicationConfig.class.isAssignableFrom(configClass))
        {
            return ((DynamicApplicationConfig) config).getManagedProperties();
        }

        return getProperties(config);
    }

    public Map<String, Object> getProperties(Object propertyConfig)
    {
        if (propertyConfig == null)
        {
            return new HashMap<>();
        }

        Class targetClass = AopUtils.getTargetClass(propertyConfig);

        try
        {
            String json = objectMapper.writerFor(targetClass)
                    .writeValueAsString(propertyConfig);
            return objectMapper.readerFor(HashMap.class).readValue(json);
        }
        catch (IOException e)
        {
            log.warn("Can't convert configuration object [{}] to map of properties", targetClass);
            return new HashMap<>();
        }
    }

    /**
     * Updates properties in arkcase.yaml
     */
    public void updateProperties(Map<String, Object> properties) throws ConfigurationPropertyException
    {
        updateProperties(properties, configurationClientConfig.getDefaultApplicationName());
    }

    /**
     * Updates properties sent with the map 'properties' in the file
     * with name 'applicationName'
     * 
     * @param properties
     * @param applicationName
     * @throws ConfigurationPropertyException
     */
    public void updateProperties(Map<String, Object> properties, String applicationName) throws ConfigurationPropertyException
    {
        try
        {
            Map<String, String> params = new HashMap<>();
            params.put("applicationName", applicationName);
            configRestTemplate.postForEntity(updatePropertiesEndpoint, properties, ResponseEntity.class, params);
        }
        catch (RestClientException e)
        {
            log.warn("Failed to update property due to {}", e.getMessage());
            throw new ConfigurationPropertyException("Failed to update configuration");
        }
    }

    /**
     * Updates properties in arkcase.yaml
     */
    public void updateProperties(Object propertyConfig) throws ConfigurationPropertyException
    {
        updateProperties(propertyConfig, configurationClientConfig.getDefaultApplicationName());
    }

    /**
     * Updates properties sent through the object 'propertyConfig' in the file
     * with name 'applicationName'
     * 
     * @param propertyConfig
     * @param applicationName
     * @throws ConfigurationPropertyException
     */
    public void updateProperties(Object propertyConfig, String applicationName) throws ConfigurationPropertyException
    {
        try
        {
            Map<String, String> params = new HashMap<>();
            params.put("applicationName", applicationName);
            configRestTemplate.postForEntity(updatePropertiesEndpoint, getObjectDynamicProperties(propertyConfig), ResponseEntity.class,
                    params);
        }
        catch (RestClientException e)
        {
            log.warn("Failed to update property due to {}", e.getMessage());
            throw new ConfigurationPropertyException("Failed to update configuration", e);
        }
    }

    /**
     * Removes properties sent with the map 'properties' in the file
     * with name 'applicationName'
     *
     * @param properties
     * @throws ConfigurationPropertyException
     */
    public void removeRuntimeProperties(List<String> properties) throws ConfigurationPropertyException
    {
        try
        {
            Map<String, String> params = new HashMap<>();
            params.put("applicationName", configurationClientConfig.getDefaultApplicationName());
            configRestTemplate.postForEntity(removePropertiesEndpoint, properties, ResponseEntity.class, params);
        }
        catch (RestClientException e)
        {
            log.warn("Failed to remove property due to {}", e.getMessage());
            throw new ConfigurationPropertyException("Failed to update configuration", e);
        }
    }

    public void resetPropertiesToDefault() throws ConfigurationPropertyException
    {
        try
        {
            configRestTemplate.delete(resetPropertiesEndpoint, ResponseEntity.class);
        }
        catch (RestClientException e)
        {
            log.warn("Failed to reset properties due to {}", e.getMessage());
            throw new ConfigurationPropertyException("Failed to reset configuration", e);
        }
    }

    public void resetFilePropertiesToDefault(String applicationName) throws ConfigurationPropertyException
    {
        try
        {
            Map<String, String> params = new HashMap<>();
            params.put("applicationName", applicationName);
            configRestTemplate.delete(resetFilePropertiesEndpoint, params);
        }
        catch (RestClientException e)
        {
            log.warn("Failed to reset properties due to {}", e.getMessage());
            throw new ConfigurationPropertyException("Failed to reset configuration", e);
        }
    }
}
