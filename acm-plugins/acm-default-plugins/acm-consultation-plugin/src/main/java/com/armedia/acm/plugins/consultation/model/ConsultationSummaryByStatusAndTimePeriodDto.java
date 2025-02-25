package com.armedia.acm.plugins.consultation.model;

/*-
 * #%L
 * ACM Default Plugin: Consultation
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

import java.util.List;

/**
 * Created by Vladimir Cherepnalkovski <vladimir.cherepnalkovski@armedia.com> on May, 2020
 */
public class ConsultationSummaryByStatusAndTimePeriodDto
{
    private String timePeriod;
    private List<ConsultationsByStatusDto> consultationsByStatusDtos;

    public String getTimePeriod()
    {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod)
    {
        this.timePeriod = timePeriod;
    }

    public List<ConsultationsByStatusDto> getConsultationsByStatusDtos()
    {
        return consultationsByStatusDtos;
    }

    public void setConsultationsByStatusDtos(List<ConsultationsByStatusDto> consultationsByStatusDtos)
    {
        this.consultationsByStatusDtos = consultationsByStatusDtos;
    }
}
