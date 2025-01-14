package com.armedia.acm.plugins.casefile.model;

/*-
 * #%L
 * ACM Default Plugin: Case File
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

import com.armedia.acm.data.AcmEntity;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by nebojsha on 21.08.2015.
 */
@XmlRootElement
@Entity
@Table(name = "acm_queue")
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class AcmQueue implements Serializable, AcmEntity
{
    private static final long serialVersionUID = -3949175334101994270L;
    @Id
    @TableGenerator(name = "acm_queue_gen", table = "acm_queue_id", pkColumnName = "cm_seq_name", valueColumnName = "cm_seq_num", pkColumnValue = "acm_queue", initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "acm_queue_gen")
    @Column(name = "cm_queue_id")
    private Long id;
    @Column(name = "cm_name")
    private String name;
    @Column(name = "cm_display_order")
    private Integer displayOrder;
    @Column(name = "cm_queue_created", nullable = false, insertable = true, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Column(name = "cm_queue_creator", insertable = true, updatable = false)
    private String creator;
    @Column(name = "cm_queue_modified", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;
    @Column(name = "cm_queue_modifier")
    private String modifier;

    public AcmQueue(Long id, String name, Integer displayOrder)
    {
        this.id = id;
        this.name = name;
        this.displayOrder = displayOrder;
    }

    public AcmQueue()
    {
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Integer getDisplayOrder()
    {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder)
    {
        this.displayOrder = displayOrder;
    }

    @XmlTransient
    @Override
    public Date getCreated()
    {
        return created;
    }

    @Override
    public void setCreated(Date created)
    {
        this.created = created;
    }

    @XmlTransient
    @Override
    public String getCreator()
    {
        return creator;
    }

    @Override
    public void setCreator(String creator)
    {
        this.creator = creator;
    }

    @XmlTransient
    @Override
    public Date getModified()
    {
        return modified;
    }

    @Override
    public void setModified(Date modified)
    {
        this.modified = modified;
    }

    @XmlTransient
    @Override
    public String getModifier()
    {
        return modifier;
    }

    @Override
    public void setModifier(String modifier)
    {
        this.modifier = modifier;
    }

    @Override
    public String toString()
    {
        return "AcmQueue{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", displayOrder=" + displayOrder +
                ", created=" + created +
                ", creator='" + creator + '\'' +
                ", modified=" + modified +
                ", modifier='" + modifier + '\'' +
                '}';
    }
}
