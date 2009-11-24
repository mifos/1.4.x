/*
 * Copyright (c) 2005-2009 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */

package org.mifos.application.office.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifos.application.master.business.CustomFieldView;
import org.mifos.application.master.persistence.MasterPersistence;
import org.mifos.application.office.exceptions.OfficeException;
import org.mifos.application.office.exceptions.OfficeValidationException;
import org.mifos.application.office.persistence.OfficePersistence;
import org.mifos.application.office.util.helpers.OfficeConstants;
import org.mifos.application.office.util.helpers.OfficeLevel;
import org.mifos.application.office.util.helpers.OfficeStatus;
import org.mifos.application.office.util.helpers.OperationMode;
import org.mifos.framework.business.BusinessObject;
import org.mifos.framework.business.util.Address;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.PropertyNotFoundException;
import org.mifos.framework.security.authorization.HierarchyManager;
import org.mifos.framework.security.util.EventManger;
import org.mifos.framework.security.util.OfficeSearch;
import org.mifos.framework.security.util.SecurityConstants;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.util.helpers.Constants;
import org.mifos.framework.util.helpers.FilePaths;

public class OfficeBO extends BusinessObject {

    private final Short officeId;
    private final Short operationMode;
    @SuppressWarnings("unused")
    // see .hbm.xml file
    private Integer maxChildCount = Integer.valueOf(0);
    private String officeName;
    private String shortName;
    private String globalOfficeNum;
    private String searchId;
    private OfficeLevelEntity level;
    private OfficeBO parentOffice;
    private OfficeStatusEntity status;
    private OfficeAddressEntity address;
    private Set<OfficeCustomFieldEntity> customFields;
    private Set<OfficeBO> children;

    /**
     * default constructor for hibernate usuage
     */
    public OfficeBO() {
        this(null, null, null, null);
        status = new OfficeStatusEntity(OfficeStatus.ACTIVE);
        address = new OfficeAddressEntity();
    }
    
    /**
     * TODO - keithw - work in progress
     * 
     * minimal legal constructor
     */
    public OfficeBO(final String name, final String globalOfficeNum) {
        this.officeId = null;
        this.officeName = name;
        this.globalOfficeNum = globalOfficeNum;
        this.operationMode = OperationMode.REMOTE_SERVER.getValue();
        this.status = new OfficeStatusEntity(OfficeStatus.ACTIVE);
        this.address = new OfficeAddressEntity();
    }

    /**
     * For tests. Does not require that the names in question actually exist in
     * the database.
     */
    public static OfficeBO makeForTest(final UserContext userContext, final OfficeLevel level, final OfficeBO parentOffice,
            final String searchId, final List<CustomFieldView> customFields, final String officeName, final String shortName, final Address address,
            final OperationMode operationMode, final OfficeStatus status) throws OfficeException {
        return new OfficeBO(userContext, null, level, parentOffice, searchId, customFields, officeName, shortName,
                address, operationMode, status);
    }

    public static OfficeBO makeForTest(final UserContext userContext, final Short officeId, final String officeName, final String shortName)
            throws OfficeException {
        return new OfficeBO(userContext, officeId, OfficeLevel.AREAOFFICE, null, null, null, officeName, shortName,
                null, OperationMode.LOCAL_SERVER, OfficeStatus.ACTIVE);
    }

    /**
     * Construct an object without validating it against the database.
     */
    private OfficeBO(final UserContext userContext, final Short officeId, final OfficeLevel level, final OfficeBO parentOffice,
            final String searchId, final List<CustomFieldView> customFields, final String officeName, final String shortName, final Address address,
            final OperationMode operationMode, final OfficeStatus status) throws OfficeValidationException {
        super(userContext);
        verifyFieldsNoDatabase(level, operationMode);

        setCreateDetails();

        this.globalOfficeNum = null;
        this.operationMode = operationMode.getValue();
        this.searchId = searchId;
        this.officeId = officeId;
        this.level = new OfficeLevelEntity(level);
        this.status = new OfficeStatusEntity(status);
        this.parentOffice = parentOffice;

        this.officeName = officeName;
        this.shortName = shortName;
        if (address != null) {
            this.address = new OfficeAddressEntity(this, address);
        }
        this.customFields = new HashSet<OfficeCustomFieldEntity>();
        if (customFields != null) {
            for (CustomFieldView view : customFields) {
                this.customFields.add(new OfficeCustomFieldEntity(view.getFieldValue(), view.getFieldId(), this));
            }
        }
    }

    /**
     * @throws OfficeValidationException
     *             Thrown when there's a validation error
     * @throws PersistenceException
     *             Thrown when there's a problem with the persistence layer
     */
    public OfficeBO(final UserContext userContext, final OfficeLevel level, final OfficeBO parentOffice,
            final List<CustomFieldView> customFields, final String officeName, final String shortName, final Address address,
            final OperationMode operationMode) throws OfficeValidationException, PersistenceException {
        this(userContext, null, level, parentOffice, null, customFields, officeName, shortName, address, operationMode,
                OfficeStatus.ACTIVE);
        verifyFields(officeName, shortName, parentOffice);
    }

    public OfficeBO(final Short officeId, final String officeName, final Integer maxChildCount, final Short operationMode) {
        super();
        this.officeId = officeId;
        this.officeName = officeName;
        this.maxChildCount = maxChildCount;
        this.operationMode = operationMode;
    }

    @Override
    public void setCreatedDate(final Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getGlobalOfficeNum() {
        return globalOfficeNum;
    }

    public OfficeLevel getOfficeLevel() {
        return OfficeLevel.getOfficeLevel(level.getId());
    }

    public OfficeStatus getOfficeStatus() throws OfficeException {
        try {
            return OfficeStatus.getOfficeStatus(status.getId());
        } catch (PropertyNotFoundException e) {
            throw new OfficeException(e);
        }
    }

    public OfficeStatusEntity getStatus() {
        return this.status;
    }

    public OfficeLevelEntity getLevel() {
        return this.level;
    }

    public String getOfficeName() {
        return officeName;
    }

    void setOfficeName(final String officeName) {
        this.officeName = officeName;
    }

    public Short getOfficeId() {
        return officeId;
    }

    public OperationMode getMode() {
        return OperationMode.fromInt(operationMode);
    }

    public OfficeBO getParentOffice() {
        return parentOffice;
    }

    void setParentOffice(final OfficeBO parentOffice) {
        this.parentOffice = parentOffice;
    }

    public String getSearchId() {
        return searchId;
    }

    public String getShortName() {
        return shortName;
    }

    void setShortName(final String shortName) {
        this.shortName = shortName;
    }

    @Override
    public void setUpdatedDate(final Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public OfficeAddressEntity getAddress() {
        return address;
    }

    public Set<OfficeCustomFieldEntity> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(final Set<OfficeCustomFieldEntity> customFields) {
        if (customFields != null) {
            this.customFields = customFields;
        }
    }

    public void setAddress(final OfficeAddressEntity address) {
        this.address = address;
    }

    public void setChildren(final Set<OfficeBO> children) {
        this.children = children;
    }

    void setGlobalOfficeNum(final String globalOfficeNum) {
        this.globalOfficeNum = globalOfficeNum;
    }

    void setLevel(final OfficeLevelEntity level) {
        this.level = level;
    }

    void setSearchId(final String searchId) {
        this.searchId = searchId;
    }

    void setStatus(final OfficeStatusEntity status) {
        this.status = status;
    }

    public Set<OfficeBO> getChildren() {
        return children;
    }

    private void removeChild(final OfficeBO office) {
        Set<OfficeBO> childerns = null;
        if (getChildren() != null && getChildren().size() > 0) {
            childerns = getChildren();
            childerns.remove(office);
        }
        setChildren(childerns);
    }

    private void addChild(final OfficeBO office) {
        Set<OfficeBO> childerns = null;
        if (getChildren() == null) {
            childerns = new HashSet<OfficeBO>();
        } else {
            childerns = getChildren();
        }
        office.setParentOffice(this);
        childerns.add(office);
        setChildren(childerns);
    }

    // Changed this method signature so that it throws the PersistenceExcpetion
    // instead of wrapping in an OfficeExcpetion. The idea being that we are
    // seperating validation exceptions from PersistenceExceptions, which should
    // ultimately be runtime errors because they occurr, most likely, from
    // configuration/binding problems which are exceptions that we shouldn't
    // be catching.
    private void verifyFields(final String officeName, final String shortName, final OfficeBO parentOffice)
            throws OfficeValidationException, PersistenceException {
        OfficePersistence officePersistence = new OfficePersistence();
        if (StringUtils.isBlank(officeName)) {
            throw new OfficeValidationException(OfficeConstants.ERRORMANDATORYFIELD,
                    new Object[] { getLocaleString(OfficeConstants.OFFICE_NAME) });
        }
        if (officePersistence.isOfficeNameExist(officeName)) {
            throw new OfficeValidationException(OfficeConstants.OFFICENAMEEXIST);
        }

        if (StringUtils.isBlank(shortName)) {
            throw new OfficeValidationException(OfficeConstants.ERRORMANDATORYFIELD,
                    new Object[] { getLocaleString(OfficeConstants.OFFICESHORTNAME) });
        }
        if (officePersistence.isOfficeShortNameExist(shortName)) {
            throw new OfficeValidationException(OfficeConstants.OFFICESHORTNAMEEXIST);
        }

        if (parentOffice == null) {
            throw new OfficeValidationException(OfficeConstants.ERRORMANDATORYFIELD,
                    new Object[] { getLocaleString(OfficeConstants.PARENTOFFICE) });
        }
    }

    private void verifyFieldsNoDatabase(final OfficeLevel level, final OperationMode operationMode)
            throws OfficeValidationException {
        if (level == null) {
            throw new OfficeValidationException(OfficeConstants.ERRORMANDATORYFIELD,
                    new Object[] { getLocaleString(OfficeConstants.OFFICELEVEL) });
        }
        if (operationMode == null) {
            throw new OfficeValidationException(OfficeConstants.ERRORMANDATORYFIELD,
                    new Object[] { getLocaleString(OfficeConstants.OFFICEOPERATIONMODE) });
        }
    }

    private String getLocaleString(final String key) {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(FilePaths.OFFICERESOURCEPATH, userContext
                .getPreferredLocale());
        return resourceBundle.getString(key);
    }

    private String generateOfficeGlobalNo() throws OfficeException {

        try {
            /*
             * TODO: Why not auto-increment? Fetching the max and adding one
             * would seem to have a race condition.
             */
            String officeGlobelNo = String.valueOf(new OfficePersistence().getMaxOfficeId().intValue() + 1);
            if (officeGlobelNo.length() > 4) {
                throw new OfficeException(OfficeConstants.MAXOFFICELIMITREACHED);
            }
            StringBuilder temp = new StringBuilder("");
            for (int i = officeGlobelNo.length(); i < 4; i++) {
                temp.append("0");
            }

            return officeGlobelNo = temp.append(officeGlobelNo).toString();
        } catch (PersistenceException e) {
            throw new OfficeException(e);
        }

    }

    private String generateSearchId() throws OfficeException {
        Integer noOfChildern;
        try {
            noOfChildern = new OfficePersistence().getChildCount(parentOffice.getOfficeId());
        } catch (PersistenceException e) {
            throw new OfficeException(e);
        }
        String parentSearchId = HierarchyManager.getInstance().getSearchId(parentOffice.getOfficeId());
        parentSearchId += ".";
        parentSearchId += ++noOfChildern;
        return parentSearchId;
    }

    public void save() throws OfficeException {

        this.globalOfficeNum = generateOfficeGlobalNo();
        this.searchId = generateSearchId();
        try {
            new OfficePersistence().createOrUpdate(this);
        } catch (PersistenceException e) {
            throw new OfficeException(e);
        }
        // if we are here it means office created sucessfully
        // we need to update hierarchy manager cache
        OfficeSearch os = new OfficeSearch(getOfficeId(), getSearchId(), getParentOffice().getOfficeId());
        List<OfficeSearch> osList = new ArrayList<OfficeSearch>();
        osList.add(os);
        EventManger.postEvent(Constants.CREATE, osList, SecurityConstants.OFFICECHANGEEVENT);

    }

    private void changeStatus(final OfficeStatus status) throws OfficeException {
        if (!this.status.getId().equals(status.getValue())) {

            if (status == OfficeStatus.INACTIVE) {
                canInactivateOffice();
            } else {
                canActivateOffice();
            }
            try {
                this.status = (OfficeStatusEntity) new MasterPersistence().getPersistentObject(
                        OfficeStatusEntity.class, status.getValue());
            } catch (PersistenceException e) {
                throw new OfficeException(e);
            }
        }
    }

    private void canInactivateOffice() throws OfficeException {
        OfficePersistence officePersistence = new OfficePersistence();
        try {
            if (officePersistence.hasActiveChildern(this.officeId)) {
                throw new OfficeException(OfficeConstants.KEYHASACTIVECHILDREN);
            }

            if (officePersistence.hasActivePeronnel(this.officeId)) {
                throw new OfficeException(OfficeConstants.KEYHASACTIVEPERSONNEL);

            }
        } catch (PersistenceException e) {
            throw new OfficeException(e);
        }
    }

    private void canActivateOffice() throws OfficeException {

        if (parentOffice.getOfficeStatus().equals(OfficeStatus.INACTIVE)) {
            throw new OfficeException(OfficeConstants.KEYPARENTNOTACTIVE);
        }
    }

    public boolean isActive() {

        return getStatus().getId().equals(OfficeStatus.ACTIVE.getValue());

    }

    public void update(final String newName, final String newShortName, final OfficeStatus newStatus, final OfficeLevel newLevel,
            final OfficeBO newParent, final Address address, final List<CustomFieldView> customFileds) throws OfficeException {
        changeOfficeName(newName);
        changeOfficeShortName(newShortName);

        updateLevel(newLevel);
        if (!this.getOfficeLevel().equals(OfficeLevel.HEADOFFICE)) {
            updateParent(newParent);
        }

        changeStatus(newStatus);
        updateAddress(address);
        updateCustomFields(customFileds);
        setUpdateDetails();
        try {
            new OfficePersistence().createOrUpdate(this);
        } catch (PersistenceException e) {
            throw new OfficeException(e);
        }
    }

    private void changeOfficeName(final String newName) throws OfficeException {

        if (!this.officeName.equalsIgnoreCase(newName)) {
            try {
                if (new OfficePersistence().isOfficeNameExist(newName)) {
                    throw new OfficeException(OfficeConstants.OFFICENAMEEXIST);
                }
            } catch (PersistenceException e) {
                throw new OfficeException(e);
            }
            this.officeName = newName;
        }
    }

    private void changeOfficeShortName(final String newShortName) throws OfficeException {

        if (!this.shortName.equalsIgnoreCase(newShortName)) {
            try {
                if (new OfficePersistence().isOfficeShortNameExist(newShortName)) {
                    throw new OfficeException(OfficeConstants.OFFICESHORTNAMEEXIST);
                }
            } catch (PersistenceException e) {
                throw new OfficeException(e);
            }
            this.shortName = newShortName;
        }
    }

    private void updateParent(final OfficeBO newParent) throws OfficeException {
        if (newParent != null) {
            if (this.getParentOffice() != null) {
                if (!this.getParentOffice().getOfficeId().equals(newParent.getOfficeId())) {
                    OfficeBO oldParent = this.getParentOffice();

                    if (this.getOfficeLevel().getValue().shortValue() < newParent.getOfficeLevel().getValue()
                            .shortValue()) {
                        throw new OfficeException(OfficeConstants.ERROR_INVLID_PARENT);
                    }
                    OfficeBO oldParent1 = getIfChildPresent(newParent, oldParent);
                    if (oldParent1 == null) {
                        oldParent1 = oldParent;
                    }
                    oldParent1.removeChild(this);
                    OfficeBO newParent1 = getIfChildPresent(oldParent, newParent);
                    if (newParent1 == null) {
                        newParent1 = newParent;
                    }
                    newParent1.addChild(this);
                    newParent1.updateSearchId(newParent1.getSearchId());
                    oldParent1.updateSearchId(oldParent1.getSearchId());

                }
            }
        }
    }

    private void updateSearchId(final String searchId) {
        this.setSearchId(searchId);
        int i = 1;
        if (this.getChildren() != null) {
            Iterator<OfficeBO> iter = this.getChildren().iterator();
            while (iter.hasNext()) {
                OfficeBO element = iter.next();
                element.updateSearchId(this.getSearchId() + "." + i);
                i++;

            }

        }
    }

    private void updateLevel(final OfficeLevel level) throws OfficeException {

        if (this.getOfficeLevel() != level) {

            if (!canUpdateLevel(level)) {
                throw new OfficeException(OfficeConstants.ERROR_INVALID_LEVEL);
            }
            try {
                this.level = (OfficeLevelEntity) new MasterPersistence().getPersistentObject(OfficeLevelEntity.class,
                        level.getValue());
            } catch (PersistenceException e) {
                throw new OfficeException(e);
            }
        }
    }

    private boolean canUpdateLevel(final OfficeLevel level) {
        if (this.getOfficeLevel().getValue() > level.getValue()) {
            return true;
        }
        for (OfficeBO office : this.children) {
            if (office.getLevel().getId() <= level.getValue()) {
                return false;
            }
        }
        return true;

    }

    private void updateAddress(final Address address) {
        if (this.address != null && address != null) {
            this.address.setAddress(address);
        } else if (this.address == null && address != null) {
            this.address = new OfficeAddressEntity(this, address);
        }
    }

    private void updateCustomFields(final List<CustomFieldView> customfields) {
        if (this.customFields != null && customfields != null) {
            for (CustomFieldView fieldView : customfields) {
                for (OfficeCustomFieldEntity fieldEntity : this.customFields) {
                    if (fieldView.getFieldId().equals(fieldEntity.getFieldId())) {
                        fieldEntity.setFieldValue(fieldView.getFieldValue());
                    }
                }
            }
        } else if (this.customFields == null && customfields != null) {
            this.customFields = new HashSet<OfficeCustomFieldEntity>();
            for (CustomFieldView view : customfields) {
                this.customFields.add(new OfficeCustomFieldEntity(view.getFieldValue(), view.getFieldId(), this));
            }
        }
    }

    public Set<OfficeBO> getBranchOnlyChildren() {
        Set<OfficeBO> offices = new HashSet<OfficeBO>();
        if (getChildren() != null) {
            for (OfficeBO office : getChildren()) {
                if (office.getOfficeLevel().equals(OfficeLevel.BRANCHOFFICE)) {
                    offices.add(office);
                }
            }
        }
        return offices;
    }

    @Override
    public boolean equals(final Object obj) {
       
        OfficeBO otherOffice = (OfficeBO) obj;
        if (this.officeId.equals(otherOffice.getOfficeId())) {
            return true;
        }

        return false;
    }

    public OfficeBO getIfChildPresent(final OfficeBO parent, final OfficeBO child) {
        if (parent.getChildren() != null) {
            for (OfficeBO childl : parent.getChildren()) {
                if (childl.getOfficeId().equals(child.getOfficeId())) {
                    return childl;
                }
                return getIfChildPresent(childl, child);
            }

        }
        return null;
    }

    public boolean isParent(final OfficeBO child) {
        boolean isParent = false;
        if (getChildren() != null) {
            for (OfficeBO children : getChildren()) {
                if (children.equals(child)) {
                    return true;
                }
                isParent = children.isParent(child);
                if (isParent) {
                    return true;
                }

            }
        }
        return isParent;
    }
}
