package com.armedia.acm.plugins.person.model;

/*-
 * #%L
 * ACM Default Plugin: Person
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

import com.armedia.acm.core.AcmObject;
import com.armedia.acm.data.AcmEntity;
import com.armedia.acm.data.converter.BooleanToStringConverter;
import com.armedia.acm.data.converter.LocalDateConverter;
import com.armedia.acm.plugins.addressable.model.ContactMethod;
import com.armedia.acm.plugins.addressable.model.PostalAddress;
import com.armedia.acm.plugins.ecm.model.AcmContainer;
import com.armedia.acm.plugins.ecm.model.AcmContainerEntity;
import com.armedia.acm.plugins.ecm.model.EcmFile;
import com.armedia.acm.services.config.lookups.model.StandardLookupEntry;
import com.armedia.acm.services.config.lookups.service.LookupDao;
import com.armedia.acm.services.labels.service.TranslationService;
import com.armedia.acm.services.participants.model.AcmAssignedObject;
import com.armedia.acm.services.participants.model.AcmParticipant;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.rometools.utils.Strings;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by armdev on 4/7/14.
 */
@XmlRootElement
@Entity
@Table(name = "acm_person")
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "className", defaultImpl = Person.class)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "cm_class_name", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("com.armedia.acm.plugins.person.model.Person")
@JsonIdentityInfo(generator = JSOGGenerator.class)
public class Person implements Serializable, AcmEntity, AcmObject, AcmContainerEntity, AcmAssignedObject
{
    private static final long serialVersionUID = 7413755227864370548L;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "person", orphanRemoval = true)
    List<PersonOrganizationAssociation> organizationAssociations = new ArrayList<>();
    @Id
    @TableGenerator(name = "acm_person_gen", table = "acm_person_id", pkColumnName = "cm_seq_name", valueColumnName = "cm_seq_num", pkColumnValue = "acm_person", initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "acm_person_gen")
    @Column(name = "cm_person_id")
    private Long id;
    @Column(name = "cm_person_title")
    private String title;
    @Column(name = "cm_person_company_name")
    private String company;
    @Column(name = "cm_person_status")
    private String status;
    @Column(name = "cm_given_name")
    @Size(min = 1)
    private String givenName;
    @Column(name = "cm_middle_name")
    private String middleName;
    @Column(name = "cm_family_name")
    @Size(min = 1)
    private String familyName;
    @Column(name = "cm_person_hair_color")
    private String hairColor;
    @Column(name = "cm_person_eye_color")
    private String eyeColor;
    @Column(name = "cm_person_height_inches")
    private Long heightInInches;
    @Column(name = "cm_person_weight_pounds")
    private Long weightInPounds;
    @Column(name = "cm_person_date_of_birth")
    @Convert(converter = LocalDateConverter.class)
    private LocalDate dateOfBirth;
    @Column(name = "cm_place_of_birth")
    private String placeOfBirth;
    @Column(name = "cm_person_date_married")
    @Convert(converter = LocalDateConverter.class)
    private LocalDate dateMarried;
    @Column(name = "cm_person_created", nullable = false, insertable = true, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Column(name = "cm_person_creator", insertable = true, updatable = false)
    private String creator;
    @Column(name = "cm_person_modified", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;
    @Column(name = "cm_person_modifier")
    private String modifier;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "acm_person_postal_address", joinColumns = {
            @JoinColumn(name = "cm_person_id", referencedColumnName = "cm_person_id") }, inverseJoinColumns = {
                    @JoinColumn(name = "cm_address_id", referencedColumnName = "cm_address_id") })
    private List<PostalAddress> addresses = new ArrayList<>();
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "acm_person_contact_method", joinColumns = {
            @JoinColumn(name = "cm_person_id", referencedColumnName = "cm_person_id") }, inverseJoinColumns = {
                    @JoinColumn(name = "cm_contact_method_id", referencedColumnName = "cm_contact_method_id") })
    @OrderBy(value = "id")
    private List<ContactMethod> contactMethods = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "acm_person_security_tag", joinColumns = @JoinColumn(name = "cm_person_id", referencedColumnName = "cm_person_id"))
    @Column(name = "cm_security_tag")
    private List<String> securityTags = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "person")
    private List<PersonAlias> personAliases = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "acm_person_identification", joinColumns = {
            @JoinColumn(name = "cm_person_id", referencedColumnName = "cm_person_id") }, inverseJoinColumns = {
                    @JoinColumn(name = "cm_identification_id", referencedColumnName = "cm_identification_id", unique = true) })
    private List<Identification> identifications = new ArrayList<>();
    @ManyToMany(cascade = {
            CascadeType.DETACH,
            CascadeType.MERGE,
            CascadeType.REFRESH,
            CascadeType.REMOVE })
    @JoinTable(name = "acm_person_organization", joinColumns = {
            @JoinColumn(name = "cm_person_id", referencedColumnName = "cm_person_id") }, inverseJoinColumns = {
                    @JoinColumn(name = "cm_organization_id", referencedColumnName = "cm_organization_id") })
    private List<Organization> organizations = new ArrayList<>();
    @Column(name = "cm_class_name")
    private String className = this.getClass().getName();
    /**
     * Container folder where the case file's attachments/content files are stored.
     */
    @OneToOne
    @JoinColumn(name = "cm_container_id")
    private AcmContainer container;
    /**
     * id for EcmFile which picture is default
     */
    @OneToOne
    @JoinColumn(name = "cm_default_picture_id")
    private EcmFile defaultPicture;
    @Column(name = "cm_object_type", updatable = false)
    private String objectType = PersonOrganizationConstants.PERSON_OBJECT_TYPE;
    /**
     * ContactMethod which is default as phone
     */
    @OneToOne
    @JoinColumn(name = "cm_default_phone")
    private ContactMethod defaultPhone;

    /**
     * ContactMethod which is default as fax
     */
    @OneToOne
    @JoinColumn(name = "cm_default_fax")
    private ContactMethod defaultFax;

    /**
     * ContactMethod which is default as email
     */
    @OneToOne
    @JoinColumn(name = "cm_default_email")
    private ContactMethod defaultEmail;
    /**
     * PostalAddress which is default
     */
    @OneToOne
    @JoinColumn(name = "cm_default_address")
    private PostalAddress defaultAddress;
    /**
     * ContactMethod which is default as url
     */
    @OneToOne
    @JoinColumn(name = "cm_default_url")
    private ContactMethod defaultUrl;
    /**
     * PersonAlias which is default from personAliases
     */
    @OneToOne
    @JoinColumn(name = "cm_default_alias")
    private PersonAlias defaultAlias;
    /**
     * Identification which is default from identifications
     */
    @OneToOne
    @JoinColumn(name = "cm_default_identification")
    private Identification defaultIdentification;
    @Lob
    @Column(name = "cm_details")
    private String details;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumns({
            @JoinColumn(name = "cm_object_id"),
            @JoinColumn(name = "cm_object_type", referencedColumnName = "cm_object_type") })
    private List<AcmParticipant> participants = new ArrayList<>();

    @Column(name = "cm_person_restricted_flag", nullable = false)
    @Convert(converter = BooleanToStringConverter.class)
    private Boolean restricted = Boolean.FALSE;

    @Column(name = "cm_ldap_user_id")
    private String ldapUserId;

    @Column(name = "cm_anonymous_flag")
    private boolean anonymousFlag;

    @Transient
    private static LookupDao lookupDao;

    @Transient
    private static TranslationService translationService;

    public static void setLookupDao(LookupDao lookupDao)
    {
        Person.lookupDao = lookupDao;
    }

    public static void setTranslationService(TranslationService translationService)
    {
        Person.translationService = translationService;
    }

    @PrePersist
    protected void beforeInsert()
    {
        if (getStatus() == null || getStatus().trim().isEmpty())
        {
            setStatus("ACTIVE");
        }
        if (getTitle() == null || getTitle().trim().isEmpty())
        {
            setTitle("-");
        }

        setupChildPointers();
    }

    @PreUpdate
    protected void beforeUpdate()
    {
        setupChildPointers();
    }

    private void setupChildPointers()
    {
        for (PersonAlias pa : getPersonAliases())
        {
            pa.setPerson(this);
        }

        for (PersonOrganizationAssociation poa : getOrganizationAssociations())
        {
            poa.setPerson(this);
        }
        if (getDefaultOrganization() != null)
        {
            getDefaultOrganization().setPerson(this);
        }

        for (AcmParticipant ap : getParticipants())
        {
            ap.setObjectId(getId());
            ap.setObjectType(getObjectType());
        }
        if (getContainer() != null)
        {
            getContainer().setContainerObjectId(getId());
            getContainer().setContainerObjectType(getObjectType());
            getContainer().setContainerObjectTitle("Person-" + getId());
        }
    }

    @Override
    @XmlTransient
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    @XmlTransient
    public String getGivenName()
    {
        return givenName;
    }

    public void setGivenName(String givenName)
    {
        this.givenName = givenName;
    }

    public String getMiddleName()
    {
        return middleName;
    }

    public void setMiddleName(String middleName)
    {
        this.middleName = middleName;
    }

    @XmlTransient
    public String getFamilyName()
    {
        return familyName;
    }

    public void setFamilyName(String familyName)
    {
        this.familyName = familyName;
    }

    @Transient
    @JsonIgnore
    private String requesterPositionTranslated;

    public String getRequesterPositionTranslated()
    {
        requesterPositionTranslated = translatedPersonTitle();
        return requesterPositionTranslated;
    }

    @Transient
    @JsonIgnore
    private String titleTranslated;

    public String getTitleTranslated()
    {
        titleTranslated = translatedPersonTitle();
        return titleTranslated;
    }

    public String translatedPersonTitle()
    {
        if (Strings.isBlank(getTitle()) || getTitle().equals("-"))
            return null;
        List<StandardLookupEntry> lookupEntries = (List<StandardLookupEntry>) lookupDao.getLookupByName("personTitles").getEntries();
        String labelKey = lookupEntries.stream()
                .filter(standardLookupEntry -> standardLookupEntry.getKey().equals(getTitle()))
                .findFirst()
                .map(StandardLookupEntry::getValue)
                .orElse(getTitle());
        return translationService.translate(labelKey);
    }

    /**
     * Get full person name
     *
     * @return
     */
    @XmlTransient
    @JsonIgnore
    public String getFullName()
    {
        StringBuilder sb = new StringBuilder();

        if (getGivenName() != null)
        {
            sb.append(getGivenName()).append(" ");
        }
        if (getMiddleName() != null)
        {
            sb.append(getMiddleName()).append(" ");
        }
        if (getFamilyName() != null)
        {
            sb.append(getFamilyName());
        }
        return sb.toString().trim();
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
    public String getObjectType()
    {
        return objectType;
    }

    @Override
    @XmlTransient
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    @XmlTransient
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    @XmlTransient
    public String getCompany()
    {
        return company;
    }

    public void setCompany(String company)
    {
        this.company = company;
    }

    @XmlTransient
    public List<PostalAddress> getAddresses()
    {
        return addresses;
    }

    public void setAddresses(List<PostalAddress> addresses)
    {
        this.addresses = addresses;
    }

    @XmlTransient
    public List<ContactMethod> getContactMethods()
    {
        return contactMethods;
    }

    public void setContactMethods(List<ContactMethod> contactMethods)
    {
        this.contactMethods = contactMethods;
    }

    @XmlTransient
    public List<String> getSecurityTags()
    {
        return securityTags;
    }

    public void setSecurityTags(List<String> securityTags)
    {
        this.securityTags = securityTags;
    }

    @XmlTransient
    public List<PersonAlias> getPersonAliases()
    {
        return personAliases;
    }

    public void setPersonAliases(List<PersonAlias> personAliases)
    {
        this.personAliases = personAliases;
        for (PersonAlias pa : personAliases)
        {
            pa.setPerson(this);
        }
    }

    @XmlTransient
    public String getHairColor()
    {
        return hairColor;
    }

    public void setHairColor(String hairColor)
    {
        this.hairColor = hairColor;
    }

    @XmlTransient
    public String getEyeColor()
    {
        return eyeColor;
    }

    public void setEyeColor(String eyeColor)
    {
        this.eyeColor = eyeColor;
    }

    @XmlTransient
    public Long getHeightInInches()
    {
        return heightInInches;
    }

    public void setHeightInInches(Long heightInInches)
    {
        this.heightInInches = heightInInches;
    }

    @XmlTransient
    public Long getWeightInPounds()
    {
        return weightInPounds;
    }

    public void setWeightInPounds(Long weightInPounds)
    {
        this.weightInPounds = weightInPounds;
    }

    @XmlTransient
    public LocalDate getDateOfBirth()
    {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth)
    {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPlaceOfBirth()
    {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth)
    {
        this.placeOfBirth = placeOfBirth;
    }

    @XmlTransient
    public LocalDate getDateMarried()
    {
        return dateMarried;
    }

    public void setDateMarried(LocalDate dateMarried)
    {
        this.dateMarried = dateMarried;
    }

    @XmlTransient
    public List<Organization> getOrganizations()
    {
        return organizations;
    }

    public void setOrganizations(List<Organization> organizations)
    {
        this.organizations = organizations;
    }

    @XmlTransient
    public List<Identification> getIdentifications()
    {
        return identifications;
    }

    public void setIdentifications(List<Identification> identifications)
    {
        this.identifications = identifications;
    }

    public Person returnBase()
    {
        return this;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    @Override
    public AcmContainer getContainer()
    {
        return container;
    }

    @Override
    public void setContainer(AcmContainer container)
    {
        this.container = container;
    }

    public EcmFile getDefaultPicture()
    {
        return defaultPicture;
    }

    public void setDefaultPicture(EcmFile defaultPictureId)
    {
        this.defaultPicture = defaultPictureId;
    }

    public ContactMethod getDefaultPhone()
    {
        return defaultPhone;
    }

    public void setDefaultPhone(ContactMethod defaultPhone)
    {
        this.defaultPhone = defaultPhone;
    }

    public ContactMethod getDefaultEmail()
    {
        return defaultEmail;
    }

    public void setDefaultEmail(ContactMethod defaultEmail)
    {
        this.defaultEmail = defaultEmail;
    }

    public ContactMethod getDefaultFax()
    {
        return defaultFax;
    }

    public void setDefaultFax(ContactMethod defaultFax)
    {
        this.defaultFax = defaultFax;
    }

    public boolean getAnonymousFlag()
    {
        return anonymousFlag;
    }

    public void setAnonymousFlag(boolean anonymousFlag)
    {
        this.anonymousFlag = anonymousFlag;
    }

    public PostalAddress getDefaultAddress()
    {
        return defaultAddress;
    }

    public void setDefaultAddress(PostalAddress defaultAddress)
    {
        this.defaultAddress = defaultAddress;
    }

    public ContactMethod getDefaultUrl()
    {
        return defaultUrl;
    }

    public void setDefaultUrl(ContactMethod defaultUrl)
    {
        this.defaultUrl = defaultUrl;
    }

    public PersonAlias getDefaultAlias()
    {
        return defaultAlias;
    }

    public void setDefaultAlias(PersonAlias defaultAlias)
    {
        this.defaultAlias = defaultAlias;
    }

    public Identification getDefaultIdentification()
    {
        return defaultIdentification;
    }

    public void setDefaultIdentification(Identification defaultIdentification)
    {
        this.defaultIdentification = defaultIdentification;
    }

    public PersonOrganizationAssociation getDefaultOrganization()
    {
        return organizationAssociations.stream().filter(association -> association.isDefaultOrganization()).findFirst().orElse(null);
    }

    public void setDefaultOrganization(PersonOrganizationAssociation personOrganizationAssociation)
    {
        organizationAssociations
                .forEach(association -> association.setDefaultOrganization(association.equals(personOrganizationAssociation)));
    }

    public String getDetails()
    {
        return details;
    }

    public void setDetails(String details)
    {
        this.details = details;
    }

    public List<PersonOrganizationAssociation> getOrganizationAssociations()
    {
        return organizationAssociations;
    }

    public void setOrganizationAssociations(List<PersonOrganizationAssociation> organizationAssociations)
    {
        this.organizationAssociations = organizationAssociations;
    }

    @Override
    public List<AcmParticipant> getParticipants()
    {
        return participants;
    }

    @Override
    public void setParticipants(List<AcmParticipant> participants)
    {
        this.participants = participants;
    }

    @Override
    public Boolean getRestricted()
    {
        return restricted;
    }

    public void setRestricted(Boolean restricted)
    {
        this.restricted = restricted;
    }

    public String getLdapUserId()
    {
        return ldapUserId;
    }

    public void setLdapUserId(String ldapUserId)
    {
        this.ldapUserId = ldapUserId;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof Person))
        {
            return false;
        }
        return getId() == ((Person) obj).getId();
    }
}
