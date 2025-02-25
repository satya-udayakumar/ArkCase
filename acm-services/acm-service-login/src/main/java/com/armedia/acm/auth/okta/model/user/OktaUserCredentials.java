package com.armedia.acm.auth.okta.model.user;

/*-
 * #%L
 * ACM Service: User Login and Authentication
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

import com.armedia.acm.auth.okta.model.ProviderType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonRootName("credentials")
public class OktaUserCredentials
{
    @JsonProperty("password.value")
    private String password;

    @JsonProperty("recovery_question.question")
    private String recovery_question;

    @JsonProperty("recovery_question.answer")
    private String recovery_answer;

    @JsonProperty("provider.type")
    private ProviderType provider;

    @JsonProperty("provider.name")
    private String providerName;

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getRecovery_question()
    {
        return recovery_question;
    }

    public void setRecovery_question(String recovery_question)
    {
        this.recovery_question = recovery_question;
    }

    public String getRecovery_answer()
    {
        return recovery_answer;
    }

    public void setRecovery_answer(String recovery_answer)
    {
        this.recovery_answer = recovery_answer;
    }

    public ProviderType getProvider()
    {
        return provider;
    }

    public void setProvider(ProviderType provider)
    {
        this.provider = provider;
    }

    public String getProviderName()
    {
        return providerName;
    }

    public void setProviderName(String providerName)
    {
        this.providerName = providerName;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this)
                .append("recovery_question", recovery_question)
                .append("provider", provider)
                .append("providerName", providerName)
                .toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        OktaUserCredentials that = (OktaUserCredentials) o;

        return new EqualsBuilder()
                .append(password, that.password)
                .append(recovery_question, that.recovery_question)
                .append(recovery_answer, that.recovery_answer)
                .append(provider, that.provider)
                .append(providerName, that.providerName)
                .isEquals();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
                .append(password)
                .append(recovery_question)
                .append(recovery_answer)
                .append(provider)
                .append(providerName)
                .toHashCode();
    }
}
