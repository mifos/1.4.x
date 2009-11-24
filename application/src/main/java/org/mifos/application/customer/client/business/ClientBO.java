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

package org.mifos.application.customer.client.business;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifos.application.accounts.business.AccountBO;
import org.mifos.application.accounts.exceptions.AccountException;
import org.mifos.application.accounts.loan.business.LoanBO;
import org.mifos.application.accounts.savings.business.SavingsBO;
import org.mifos.application.accounts.savings.persistence.SavingsPersistence;
import org.mifos.application.accounts.util.helpers.AccountState;
import org.mifos.application.configuration.business.MifosConfiguration;
import org.mifos.application.configuration.util.helpers.ConfigurationConstants;
import org.mifos.application.customer.business.CustomerBO;
import org.mifos.application.customer.business.CustomerHierarchyEntity;
import org.mifos.application.customer.business.CustomerMeetingEntity;
import org.mifos.application.customer.business.CustomerStatusEntity;
import org.mifos.application.customer.client.persistence.ClientPersistence;
import org.mifos.application.customer.client.util.helpers.ClientConstants;
import org.mifos.application.customer.exceptions.CustomerException;
import org.mifos.application.customer.group.business.GroupBO;
import org.mifos.application.customer.group.util.helpers.GroupConstants;
import org.mifos.application.customer.persistence.CustomerPersistence;
import org.mifos.application.customer.util.helpers.CustomerConstants;
import org.mifos.application.customer.util.helpers.CustomerLevel;
import org.mifos.application.customer.util.helpers.CustomerStatus;
import org.mifos.application.fees.business.FeeView;
import org.mifos.application.master.MessageLookup;
import org.mifos.application.master.business.CustomFieldDefinitionEntity;
import org.mifos.application.master.business.CustomFieldView;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.office.business.OfficeBO;
import org.mifos.application.office.persistence.OfficePersistence;
import org.mifos.application.personnel.business.PersonnelBO;
import org.mifos.application.productdefinition.business.SavingsOfferingBO;
import org.mifos.application.productdefinition.persistence.SavingsPrdPersistence;
import org.mifos.application.surveys.business.SurveyInstance;
import org.mifos.application.util.helpers.EntityType;
import org.mifos.application.util.helpers.YesNoFlag;
import org.mifos.framework.business.util.Address;
import org.mifos.framework.components.logger.LoggerConstants;
import org.mifos.framework.components.logger.MifosLogManager;
import org.mifos.framework.components.logger.MifosLogger;
import org.mifos.framework.exceptions.PersistenceException;
import org.mifos.framework.exceptions.ServiceException;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.util.DateTimeService;
import org.mifos.framework.util.helpers.DateUtils;
import org.mifos.framework.util.helpers.Money;

public class ClientBO extends CustomerBO {

    private CustomerPictureEntity customerPicture;

    private final Set<ClientNameDetailEntity> nameDetailSet;
    
    private Set<ClientFamilyDetailEntity> familyDetailSet;

    private final Set<ClientAttendanceBO> clientAttendances;

    private Date dateOfBirth;

    private String governmentId;

    private ClientPerformanceHistoryEntity clientPerformanceHistory;

    private Short groupFlag;

    private String firstName;

    private String lastName;

    private String secondLastName;

    private ClientDetailEntity customerDetail;

    private final Set<ClientInitialSavingsOfferingEntity> offeringsAssociatedInCreate;

    private final MifosLogger logger = MifosLogManager.getLogger(LoggerConstants.CLIENTLOGGER);

    /*
     * Injected Persistence classes
     * 
     * DO NOT ACCESS THESE MEMBERS DIRECTLY! ALWAYS USE THE GETTER!
     * 
     * The Persistence classes below are used by this class and can be injected
     * via a setter for testing purposes. In order for this mechanism to work
     * correctly, the getter must be used to access them because the getter will
     * initialize the Persistence class if it has not been injected.
     * 
     * Long term these references to Persistence classes should probably be
     * eliminated.
     */
    private ClientPersistence clientPersistence = null;
    private SavingsPersistence savingsPersistence = null;
    private SavingsPrdPersistence savingsPrdPersistence = null;
    private OfficePersistence officePersistence = null;

    public OfficePersistence getOfficePersistence() {
        if (null == officePersistence) {
            officePersistence = new OfficePersistence();
        }
        return officePersistence;
    }

    public void setOfficePersistence(final OfficePersistence officePersistence) {
        this.officePersistence = officePersistence;
    }
    
    /**
     * default cosntructor for hibernate
     */
    protected ClientBO() {
        super();
        this.nameDetailSet = new HashSet<ClientNameDetailEntity>();
        this.clientAttendances = new HashSet<ClientAttendanceBO>();
        this.clientPerformanceHistory = null;
        this.offeringsAssociatedInCreate = null;
        this.familyDetailSet=null;
    }

    /**
     * TODO - keithw - work in progress
     * 
     * minimal legal constructor
     */
    public ClientBO(final CustomerLevel customerLevel, final CustomerStatus customerStatus, final String name,
            final OfficeBO office, final PersonnelBO loanOfficer, final CustomerMeetingEntity customerMeeting,
            final String searchId, final CustomerBO parentCustomer) {
        super(customerLevel, customerStatus, name, office, loanOfficer, customerMeeting, parentCustomer);
        this.setSearchId(searchId);
        this.nameDetailSet = new HashSet<ClientNameDetailEntity>();
        this.clientAttendances = new HashSet<ClientAttendanceBO>();
        this.clientPerformanceHistory = null;
        this.offeringsAssociatedInCreate = null;
        this.familyDetailSet=null;
    }

    public ClientBO(final UserContext userContext, final String displayName, final CustomerStatus customerStatus, final String externalId,
            final Date mfiJoiningDate, final Address address, final List<CustomFieldView> customFields, final List<FeeView> fees,
            final List<SavingsOfferingBO> offeringsSelected, final PersonnelBO formedBy, final OfficeBO office,
            final CustomerBO parentCustomer, final Date dateOfBirth, final String governmentId, final Short trained, final Date trainedDate,
            final Short groupFlag, final ClientNameDetailView clientNameDetailView, final ClientNameDetailView spouseNameDetailView,
            final ClientDetailView clientDetailView, final InputStream picture) throws CustomerException {
        this(userContext, displayName, customerStatus, externalId, mfiJoiningDate, address, customFields, fees,
                offeringsSelected, formedBy, office, parentCustomer, null, null, dateOfBirth, governmentId, trained,
                trainedDate, groupFlag, clientNameDetailView, spouseNameDetailView, clientDetailView, picture);
    }

    public ClientBO(final UserContext userContext, final String displayName, final CustomerStatus customerStatus, final String externalId,
            final Date mfiJoiningDate, final Address address, final List<CustomFieldView> customFields, final List<FeeView> fees,
            final List<SavingsOfferingBO> offeringsSelected, final PersonnelBO formedBy, final OfficeBO office, final MeetingBO meeting,
            final PersonnelBO loanOfficer, final Date dateOfBirth, final String governmentId, final Short trained, final Date trainedDate,
            final Short groupFlag, final ClientNameDetailView clientNameDetailView, final ClientNameDetailView spouseNameDetailView,
            final ClientDetailView clientDetailView, final InputStream picture) throws CustomerException {
        this(userContext, displayName, customerStatus, externalId, mfiJoiningDate, address, customFields, fees,
                offeringsSelected, formedBy, office, null, meeting, loanOfficer, dateOfBirth, governmentId, trained,
                trainedDate, groupFlag, clientNameDetailView, spouseNameDetailView, clientDetailView, picture);
    }

    private ClientBO(final UserContext userContext, final String displayName, final CustomerStatus customerStatus, final String externalId,
            final Date mfiJoiningDate, final Address address, final List<CustomFieldView> customFields, final List<FeeView> fees,
            final List<SavingsOfferingBO> offeringsSelected, final PersonnelBO formedBy, final OfficeBO office,
            final CustomerBO parentCustomer, final MeetingBO meeting, final PersonnelBO loanOfficer, final Date dateOfBirth,
            final String governmentId, final Short trained, final Date trainedDate, final Short groupFlag,
            final ClientNameDetailView clientNameDetailView, final ClientNameDetailView spouseNameDetailView,
            final ClientDetailView clientDetailView, final InputStream picture) throws CustomerException {
        super(userContext, displayName, CustomerLevel.CLIENT, customerStatus, externalId, mfiJoiningDate, address,
                customFields, fees, formedBy, office, parentCustomer, meeting, loanOfficer);
        validateOffice(office);
        validateOfferings(offeringsSelected);
        nameDetailSet = new HashSet<ClientNameDetailEntity>();
        clientAttendances = new HashSet<ClientAttendanceBO>();
        this.clientPerformanceHistory = new ClientPerformanceHistoryEntity(this);
        this.familyDetailSet=null;
        this.dateOfBirth = dateOfBirth;
        this.governmentId = governmentId;
        if (trained != null) {
            setTrained(trained);
        } else {
            setTrained(YesNoFlag.NO.getValue());
        }
        setTrainedDate(trainedDate);
        this.groupFlag = groupFlag;
        this.firstName = clientNameDetailView.getFirstName();
        this.lastName = clientNameDetailView.getLastName();
        this.secondLastName = clientNameDetailView.getSecondLastName();
        this.addNameDetailSet(new ClientNameDetailEntity(this, null, clientNameDetailView));
        if(spouseNameDetailView!=null)
            this.addNameDetailSet(new ClientNameDetailEntity(this, null, spouseNameDetailView));
        this.customerDetail = new ClientDetailEntity(this, clientDetailView);
        createPicture(picture);
        offeringsAssociatedInCreate = new HashSet<ClientInitialSavingsOfferingEntity>();
        createAssociatedOfferings(offeringsSelected);
        validateForDuplicateNameOrGovtId(displayName, dateOfBirth, governmentId);

        if (parentCustomer != null) {
            checkIfClientStatusIsLower(getStatus().getValue(), parentCustomer.getStatus().getValue());
        }

        if (isActive()) {
            validateFieldsForActiveClient(loanOfficer, meeting);
            this.setCustomerActivationDate(this.getCreatedDate());
            createAccountsForClient();
            createDepositSchedule();
        }
        generateSearchId();
    }
    
    public void setFamilyAndNameDetailSets(final List<ClientNameDetailView> familyNameDetailView, final List<ClientFamilyDetailView> familyDetailView) {
        Iterator iterator2=familyDetailView.iterator();
        familyDetailSet=new HashSet<ClientFamilyDetailEntity>();
        for (Iterator iterator = familyNameDetailView.iterator(); iterator.hasNext();) { 
            ClientNameDetailView clientNameDetailView2 = (ClientNameDetailView) iterator.next();
            ClientFamilyDetailView clientFamilyDetailView2= (ClientFamilyDetailView) iterator2.next();
            ClientNameDetailEntity nameEntity=new ClientNameDetailEntity(this, null,clientNameDetailView2);
            this.addNameDetailSet(nameEntity);
            this.addFamilyDetailSet(new ClientFamilyDetailEntity(this,nameEntity, clientFamilyDetailView2));   
        } 
        
    }
    public Set<ClientNameDetailEntity> getNameDetailSet() {
        return nameDetailSet;
    }

    public Set<ClientFamilyDetailEntity> getFamilyDetailSet() {
        return this.familyDetailSet;
    }

    public CustomerPictureEntity getCustomerPicture() {
        return customerPicture;
    }

    public void setCustomerPicture(final CustomerPictureEntity customerPicture) {
        this.customerPicture = customerPicture;
    }

    public Set<ClientAttendanceBO> getClientAttendances() {
        return clientAttendances;
    }

    public Date getDateOfBirth() {
        return this.dateOfBirth;
    }

    void setDateOfBirth(final Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGovernmentId() {
        return governmentId;
    }

    void setGovernmentId(final String governmentId) {
        this.governmentId = governmentId;
    }

    public ClientDetailEntity getCustomerDetail() {
        return customerDetail;
    }

    public void setCustomerDetail(final ClientDetailEntity customerDetail) {
        this.customerDetail = customerDetail;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getSecondLastName() {
        return secondLastName;
    }

    public void setSecondLastName(final String secondLastName) {
        this.secondLastName = secondLastName;
    }

    public Double getPovertyLikelihoodPercent() {
        return this.customerDetail.getPovertyLikelihoodPercent();
    }

    /**
     * TODO: This method is deprecated and should be removed once method
     * attachPpiSurvey is implemented. Poverty likelihood should be set based on
     * the results of a PPI survey and should not be over-writable in order to
     * maintain the integrity of this data.
     * 
     * The method is included here in order to test hibernate mappings through
     * customerDetail.
     */
    public void setPovertyLikelihoodPercent(final Double pct) {
        this.customerDetail.setPovertyLikelihoodPercent(pct);
    }

    public Set<ClientInitialSavingsOfferingEntity> getOfferingsAssociatedInCreate() {
        return offeringsAssociatedInCreate;
    }

    public ClientPerformanceHistoryEntity getClientPerformanceHistory() {
        return clientPerformanceHistory;
    }

    public void setClientPerformanceHistory(final ClientPerformanceHistoryEntity clientPerformanceHistory) {
        if (clientPerformanceHistory != null) {
            clientPerformanceHistory.setClient(this);
        }
        this.clientPerformanceHistory = clientPerformanceHistory;
    }

    public void addClientAttendance(final ClientAttendanceBO clientAttendance) {
        clientAttendance.setCustomer(this);
        clientAttendances.add(clientAttendance);
    }

    public void addNameDetailSet(final ClientNameDetailEntity customerNameDetail) {
        this.nameDetailSet.add(customerNameDetail);
    }

    public void addFamilyDetailSet(final ClientFamilyDetailEntity clientFamilyDetail){
        this.familyDetailSet.add(clientFamilyDetail);
     }
    @Override
    public boolean isActive() {
        return getStatus() == CustomerStatus.CLIENT_ACTIVE;
    }

    public boolean isOnHold() {
        return getStatus() == CustomerStatus.CLIENT_HOLD;
    }

    public boolean isClientUnderGroup() {
        return groupFlag.equals(YesNoFlag.YES.getValue());
    }

    public ClientAttendanceBO getClientAttendanceForMeeting(final Date meetingDate) {
        for (ClientAttendanceBO clientAttendance : getClientAttendances()) {
            if (DateUtils.getDateWithoutTimeStamp(clientAttendance.getMeetingDate().getTime()).compareTo(
                    DateUtils.getDateWithoutTimeStamp(meetingDate.getTime())) == 0) {
                return clientAttendance;
            }
        }
        return null;
    }

    // when this method is called from Bulk Entry preview persist will be false
    public void handleAttendance(final Date meetingDate, final Short attendance, final boolean persist) throws ServiceException,
            CustomerException {
        ClientAttendanceBO clientAttendance = getClientAttendanceForMeeting(meetingDate);
        if (clientAttendance == null) {
            clientAttendance = new ClientAttendanceBO();
            clientAttendance.setMeetingDate(meetingDate);
            addClientAttendance(clientAttendance);
        }
        clientAttendance.setAttendance(attendance);
        if (persist) {
            try {
                getCustomerPersistence().createOrUpdate(this);
            } catch (PersistenceException e) {
                throw new CustomerException(e);
            }
        }
    }

    public void handleAttendance(final Date meetingDate, final AttendanceType attendance) throws ServiceException,
            CustomerException {
        boolean persist = true;
        handleAttendance(meetingDate, attendance.getValue(), persist);
    }

    @Override
    public void changeStatus(final Short newStatusId, final Short flagId, final String comment) throws CustomerException {
        super.changeStatus(newStatusId, flagId, comment);
        if (isClientUnderGroup()
                && (newStatusId.equals(CustomerStatus.CLIENT_CLOSED.getValue()) || newStatusId
                        .equals(CustomerStatus.CLIENT_CANCELLED.getValue()))) {
            resetPositions(getParentCustomer());
            getParentCustomer().setUserContext(getUserContext());
            getParentCustomer().update();
            CustomerBO center = getParentCustomer().getParentCustomer();
            if (center != null) {
                resetPositions(center);
                center.setUserContext(getUserContext());
                center.update();
                center = null;
            }
        }
    }

    @Override
    public void updateMeeting(final MeetingBO meeting) throws CustomerException {
        if (getCustomerMeeting() == null) {
            this.setCustomerMeeting(createCustomerMeeting(meeting));
        } else {
            saveUpdatedMeeting(meeting);
        }
        this.update();
    }

    @Override
    protected void saveUpdatedMeeting(final MeetingBO meeting) throws CustomerException {
        MeetingBO newMeeting = getCustomerMeeting().getUpdatedMeeting();
        super.saveUpdatedMeeting(meeting);
        if (getParentCustomer() == null) {
            deleteMeeting(newMeeting);
        }
    }

    @Override
    protected void validateStatusChange(final Short newStatusId) throws CustomerException {
        if (getParentCustomer() != null) {
            checkIfClientStatusIsLower(newStatusId, getParentCustomer().getCustomerStatus().getId());
        }

        if (newStatusId.equals(CustomerStatus.CLIENT_CLOSED.getValue())) {
            checkIfClientCanBeClosed();
        }

        if (newStatusId.equals(CustomerStatus.CLIENT_ACTIVE.getValue())) {
            checkIfClientCanBeActive(newStatusId);
        }
    }

    @Override
    protected boolean isActiveForFirstTime(final Short oldStatus, final Short newStatusId) {
        return (oldStatus.equals(CustomerStatus.CLIENT_PARTIAL.getValue()) || oldStatus
                .equals(CustomerStatus.CLIENT_PENDING.getValue())) && newStatusId.equals(CustomerStatus.CLIENT_ACTIVE
                .getValue());
    }

    @Override
    protected void handleActiveForFirstTime(final Short oldStatusId, final Short newStatusId) throws CustomerException {
        super.handleActiveForFirstTime(oldStatusId, newStatusId);
        if (isActiveForFirstTime(oldStatusId, newStatusId)) {
            this.setCustomerActivationDate(new DateTimeService().getCurrentJavaDateTime());
            createAccountsForClient();
            getSavingsPersistence().persistSavingAccounts(this);
            createDepositSchedule();
        }
    }

    public void updatePersonalInfo(final String displayname, final String governmentId, final Date dateOfBirth) throws CustomerException {
        validateForDuplicateNameOrGovtId(displayname, dateOfBirth, governmentId);
        setDisplayName(displayname);
        setGovernmentId(governmentId);
        setDateOfBirth(dateOfBirth);

        super.update();
    }   
  
    /**
     * This method is called for client family and name details update
     */
     public void updateFamilyInfo(List<Integer> primaryKeys,List<ClientNameDetailView> clientNameDetailView, List<ClientFamilyDetailView> clientFamilyDetailView) throws PersistenceException {                 
         
         updateFamilyAndNameDetails(primaryKeys,clientNameDetailView,clientFamilyDetailView);
         deleteFamilyAndNameDetails(primaryKeys);
         insertFamilyAndNameDetails(primaryKeys,clientNameDetailView,clientFamilyDetailView);
         
         new CustomerPersistence().createOrUpdate(this);  
     }
     
     /**
      * This method return boolean value
      * 
      * @param clientNameId
      * @param primaryKeys
      * @return
      */
     private boolean isKeyExists(int clientNameId, List<Integer> primaryKeys){         
         boolean keyFound = false;
         for(int i=0;i<primaryKeys.size();i++){
             if(primaryKeys.get(i)!=null && primaryKeys.get(i)==clientNameId){
                 keyFound = true;
             }
         }      
         return keyFound;
     }
     
     /**
      * This method is used to update the Client Family and Name Details
      *  
      * @param primaryKeys
      * @param clientNameDetailView
      * @param clientFamilyDetailView
      */
     public void updateFamilyAndNameDetails (List<Integer> primaryKeys,List<ClientNameDetailView> clientNameDetailView, List<ClientFamilyDetailView> clientFamilyDetailView) {
         for(int key=0;key<primaryKeys.size();key++) {
             // check for the primary key if that is not null update the data
            if(primaryKeys.get(key)!=null){
              //update name details
                for (ClientNameDetailEntity clientNameDetailEntity : nameDetailSet){
                    if(clientNameDetailEntity.getCustomerNameId().intValue()==primaryKeys.get(key).intValue()){                        
                        clientNameDetailEntity.updateNameDetails(clientNameDetailView.get(key));
                    }// if clientNameDetailEntity.getCustomerNameId()                 
                }//inner for clientNameDetailEntity
                
                //update family details 
                for (ClientFamilyDetailEntity clientFamilyDetailEntity  : familyDetailSet){                
                    if(clientFamilyDetailEntity.getClientName().getCustomerNameId().intValue()==primaryKeys.get(key).intValue()){
                        clientFamilyDetailEntity.updateClientFamilyDetails(clientFamilyDetailView.get(key));
                    }// if clientFamilyDetailEntity   
                }//end of for clientFamilyDetailEntity                 
            }// end of if
        }//End of for 
     }
    
     /**
     *  This method is used to delete the Client Family and Name Details
     *   
     * @param primaryKeys
     * @throws PersistenceException 
     */
     public void deleteFamilyAndNameDetails(List<Integer> primaryKeys) throws PersistenceException{
         // check for the primary  if that is null crate the data
         //get all the family entities to delete
         List<ClientFamilyDetailEntity> deleteFamilyDetailEntity = new ArrayList<ClientFamilyDetailEntity>();
         for (ClientFamilyDetailEntity clientFamilyDetailEntity : familyDetailSet){
             if(!isKeyExists(clientFamilyDetailEntity.getClientName().getCustomerNameId(),primaryKeys)){
                 deleteFamilyDetailEntity.add(clientFamilyDetailEntity);
             }
         }
        //get all the name entities to delete
         List<ClientNameDetailEntity> deleteNameDetailEntity = new ArrayList<ClientNameDetailEntity>();
         for (ClientNameDetailEntity clientNameDetailEntity : nameDetailSet){
             //Ignoring name entities of client type
             if(!clientNameDetailEntity.getNameType().equals(ClientConstants.CLIENT_NAME_TYPE)) {
                 if(!isKeyExists(clientNameDetailEntity.getCustomerNameId(),primaryKeys)){
                     deleteNameDetailEntity.add(clientNameDetailEntity);
                 }
             }         
         }         
         //Delete ClientFamilyDetailEntity
         for(int i=0;i<deleteFamilyDetailEntity.size();i++){
             familyDetailSet.remove(deleteFamilyDetailEntity.get(i));
            getCustomerPersistence().delete(deleteFamilyDetailEntity.get(i));            
         }
       //Delete ClientNameDetailEntity
         for(int i=0;i<deleteNameDetailEntity.size();i++){
             nameDetailSet.remove(deleteNameDetailEntity.get(i));
             getCustomerPersistence().delete(deleteNameDetailEntity.get(i));           
         }
     }
     
     /**
      * This method is used to insert the Client Family and Name Details
      * 
      * @param primaryKeys
      * @param clientNameDetailView
      * @param clientFamilyDetailView
      */
     public void insertFamilyAndNameDetails(List<Integer> primaryKeys,List<ClientNameDetailView> clientNameDetailView, List<ClientFamilyDetailView> clientFamilyDetailView) {
       //INSERT data
         for(int i=0;i<primaryKeys.size();i++){
             if(primaryKeys.get(i)==null){                 
                 ClientNameDetailEntity nameDetail=new ClientNameDetailEntity(this,null,clientNameDetailView.get(i));
                 nameDetailSet.add(nameDetail);
                 familyDetailSet.add(new ClientFamilyDetailEntity(this,nameDetail,clientFamilyDetailView.get(i)));
             }
         }
     }
     
     public void updateMfiInfo(final PersonnelBO personnel) throws CustomerException {
        if (isActive() || isOnHold()) {
            validateLO(personnel);
        }
        setPersonnel(personnel);
        for (AccountBO account : this.getAccounts()) {
            account.setPersonnel(this.getPersonnel());
        }
        super.update();
    }

    public void transferToBranch(final OfficeBO officeToTransfer) throws CustomerException {
        validateBranchTransfer(officeToTransfer);
        logger.debug("In ClientBO::transferToBranch(), transfering customerId: " + getCustomerId() + "to branch : "
                + officeToTransfer.getOfficeId());
        if (isActive()) {
            setCustomerStatus(new CustomerStatusEntity(CustomerStatus.CLIENT_HOLD));
        }

        makeCustomerMovementEntries(officeToTransfer);
        this.setPersonnel(null);
        generateSearchId();
        super.update();
        logger.debug("In ClientBO::transferToBranch(), successfully transfered, customerId :" + getCustomerId());
    }

    public void transferToGroup(final GroupBO newParent) throws CustomerException {
        validateGroupTransfer(newParent);
        logger.debug("In ClientBO::transferToGroup(), transfering customerId: " + getCustomerId() + "to Group Id : "
                + newParent.getCustomerId());

        if (!isSameBranch(newParent.getOffice())) {
            makeCustomerMovementEntries(newParent.getOffice());
        }

        CustomerBO oldParent = getParentCustomer();
        changeParentCustomer(newParent);
        resetPositions(oldParent);

        if (oldParent.getParentCustomer() != null) {
            CustomerBO center = oldParent.getParentCustomer();
            resetPositions(center);
            center.setUserContext(getUserContext());
            center.update();
        }
        update();
        logger.debug("In ClientBO::transferToGroup(), successfully transfered, customerId :" + getCustomerId());
    }

    public void handleGroupTransfer() throws CustomerException {
        if (!isSameBranch(getParentCustomer().getOffice())) {
            makeCustomerMovementEntries(getParentCustomer().getOffice());
            if (isActive()) {
                setCustomerStatus(new CustomerStatusEntity(CustomerStatus.CLIENT_HOLD));
            }
            this.setPersonnel(null);
        }
        setSearchId(getParentCustomer().getSearchId() + getSearchId().substring(getSearchId().lastIndexOf(".")));
        if (getParentCustomer().getParentCustomer() != null) {
            setPersonnel(getParentCustomer().getPersonnel());
            setUpdatedMeeting(getParentCustomer().getParentCustomer().getCustomerMeeting().getMeeting());
        }
        update();
    }

    public ClientNameDetailEntity getClientName() {
        for (ClientNameDetailEntity nameDetail : nameDetailSet) {
            if (nameDetail.getNameType().equals(ClientConstants.CLIENT_NAME_TYPE)) {
                return nameDetail;
            }
        }
        return null;
    }

    public ClientNameDetailEntity getSpouseName() {
        for (ClientNameDetailEntity nameDetail : nameDetailSet) {
            if (!nameDetail.getNameType().equals(ClientConstants.CLIENT_NAME_TYPE)) {
                return nameDetail;
            }
        }
        return null;
    }

    public void updateClientDetails(final ClientDetailView clientDetailView) {
        customerDetail.updateClientDetails(clientDetailView);

    }

    public void updatePicture(final InputStream picture) throws CustomerException {
        if (customerPicture != null) {
            try {
                customerPicture.setPicture(getClientPersistence().createBlob(picture));
            } catch (PersistenceException e) {
                throw new CustomerException(e);
            }
        } else {
            try {
                this.customerPicture = new CustomerPictureEntity(this, getClientPersistence().createBlob(picture));
            } catch (PersistenceException e) {
                throw new CustomerException(e);
            }
        }

    }

    private void createPicture(final InputStream picture) throws CustomerException {
        try {
            if (picture != null && picture.available() > 0) {
                try {
                    this.customerPicture = new CustomerPictureEntity(this, getClientPersistence().createBlob(picture));
                } catch (PersistenceException e) {
                    throw new CustomerException(e);
                }
            }

        } catch (IOException e) {
            throw new CustomerException(e);
        }
    }

    private void createAssociatedOfferings(final List<SavingsOfferingBO> offeringsSelected) {
        if (offeringsSelected != null) {
            for (SavingsOfferingBO offering : offeringsSelected) {
                offeringsAssociatedInCreate.add(new ClientInitialSavingsOfferingEntity(this, offering));
            }
        }
    }

    private void validateForActiveAccounts() throws CustomerException {
        if (isAnyLoanAccountOpen() || isAnySavingsAccountOpen()) {
            throw new CustomerException(ClientConstants.ERRORS_ACTIVE_ACCOUNTS_PRESENT, new Object[] { MessageLookup
                    .getInstance().lookupLabel(ConfigurationConstants.GROUP, userContext) });
        }
    }

    private void validateBranchTransfer(final OfficeBO officeToTransfer) throws CustomerException {
        if (officeToTransfer == null) {
            throw new CustomerException(CustomerConstants.INVALID_OFFICE);
        }

        if (isSameBranch(officeToTransfer)) {
            throw new CustomerException(CustomerConstants.ERRORS_SAME_BRANCH_TRANSFER);
        }

        if (!officeToTransfer.isActive()) {
            throw new CustomerException(CustomerConstants.ERRORS_TRANSFER_IN_INACTIVE_OFFICE);
        }
    }

    private boolean isSameGroup(final GroupBO group) {
        return getParentCustomer().getCustomerId().equals(group.getCustomerId());
    }

    private void validateGroupTransfer(final GroupBO toGroup) throws CustomerException {
        if (toGroup == null) {
            throw new CustomerException(CustomerConstants.INVALID_PARENT);
        }

        if (isSameGroup(toGroup)) {
            throw new CustomerException(CustomerConstants.ERRORS_SAME_PARENT_TRANSFER);
        }

        validateForGroupStatus(toGroup.getStatus());
        validateForActiveAccounts();
        if (getCustomerMeeting() != null && toGroup.getCustomerMeeting() != null) {
            validateMeetingRecurrenceForTransfer(getCustomerMeeting().getMeeting(), toGroup.getCustomerMeeting()
                    .getMeeting());
        }
    }

    private void validateForGroupStatus(final CustomerStatus groupStatus) throws CustomerException {
        if (isGroupStatusLower(getStatus(), groupStatus)) {
            MifosConfiguration labelConfig = MifosConfiguration.getInstance();
            throw new CustomerException(ClientConstants.ERRORS_LOWER_GROUP_STATUS, new Object[] {
                    MessageLookup.getInstance().lookupLabel(ConfigurationConstants.GROUP, userContext),
                    MessageLookup.getInstance().lookupLabel(ConfigurationConstants.CLIENT, userContext) });
        }
        if (groupStatus.equals(CustomerStatus.GROUP_CANCELLED) || groupStatus.equals(CustomerStatus.GROUP_CLOSED)) {
            throw new CustomerException(CustomerConstants.ERRORS_INTRANSFER_PARENT_INACTIVE);
        }

    }

    private void generateSearchId() throws CustomerException {
        int count;
        if (getParentCustomer() != null) {
            childAddedForParent(getParentCustomer());
            this.setSearchId(getParentCustomer().getSearchId() + "." + getParentCustomer().getMaxChildCount());
        } else {
            try {
                count = getCustomerPersistence().getCustomerCountForOffice(CustomerLevel.CLIENT,
                        getOffice().getOfficeId());
            } catch (PersistenceException pe) {
                throw new CustomerException(pe);
            }
            String searchId = GroupConstants.PREFIX_SEARCH_STRING + ++count;
            this.setSearchId(searchId);
        }
    }

    private void validateForDuplicateNameOrGovtId(final String displayName, final Date dateOfBirth, final String governmentId)
            throws CustomerException {
        checkForDuplicates(displayName, dateOfBirth, governmentId, getCustomerId() == null ? Integer.valueOf("0")
                : getCustomerId());
    }

    private void validateFieldsForActiveClient(final PersonnelBO loanOfficer, final MeetingBO meeting) throws CustomerException {
        if (isActive()) {
            if (!isClientUnderGroup()) {
                validateLO(loanOfficer);
                validateMeeting(meeting);
            }
        }
    }

    private void checkForDuplicates(final String name, final Date dob, final String governmentId, final Integer customerId)
            throws CustomerException {
        if (StringUtils.isNotBlank(governmentId)) {
            try {
                if (getClientPersistence().checkForDuplicacyOnGovtIdForNonClosedClients(governmentId, customerId) == true) {
                    String label = MessageLookup.getInstance().lookupLabel(ConfigurationConstants.GOVERNMENT_ID,
                            userContext);
                    throw new CustomerException(CustomerConstants.DUPLICATE_GOVT_ID_EXCEPTION, new Object[] {
                            governmentId, label });
                }
            } catch (PersistenceException e) {
                throw new CustomerException(e);
            }
        } else {
            try {
                if (getClientPersistence().checkForDuplicacyForNonClosedClientsOnNameAndDob(name, dob, customerId) == true) {
                    throw new CustomerException(CustomerConstants.CUSTOMER_DUPLICATE_CUSTOMERNAME_EXCEPTION,
                            new Object[] { name });
                }
            } catch (PersistenceException e) {
                throw new CustomerException(e);
            }
        }

    }

    private boolean isGroupStatusLower(final CustomerStatus clientStatus, final CustomerStatus groupStatus) {
        if (clientStatus.equals(CustomerStatus.CLIENT_PENDING)) {
            if (groupStatus.equals(CustomerStatus.GROUP_PARTIAL)) {
                return true;
            }
        } else if (clientStatus.equals(CustomerStatus.CLIENT_ACTIVE) || clientStatus.equals(CustomerStatus.CLIENT_HOLD)) {
            if (groupStatus.equals(CustomerStatus.GROUP_PARTIAL) || groupStatus.equals(CustomerStatus.GROUP_PENDING)) {
                return true;
            }
        }
        return false;
    }

    private void checkIfClientCanBeClosed() throws CustomerException {
        if (isAnyLoanAccountOpen() || isAnySavingsAccountOpen()) {
            throw new CustomerException(CustomerConstants.CUSTOMER_HAS_ACTIVE_ACCOUNTS_EXCEPTION);
        }
    }

    private void checkIfClientStatusIsLower(final Short clientStatusId, final Short groupStatus) throws CustomerException {
        if ((clientStatusId.equals(CustomerStatus.CLIENT_ACTIVE.getValue()) || clientStatusId
                .equals(CustomerStatus.CLIENT_PENDING.getValue()))
                && this.isClientUnderGroup()) {
            if (groupStatus.equals(CustomerStatus.GROUP_CANCELLED.getValue())) {
                throw new CustomerException(ClientConstants.ERRORS_GROUP_CANCELLED, new Object[] { MessageLookup
                        .getInstance().lookupLabel(ConfigurationConstants.GROUP, this.getUserContext()) });
            }

            if (isGroupStatusLower(clientStatusId, groupStatus)) {

                throw new CustomerException(ClientConstants.INVALID_CLIENT_STATUS_EXCEPTION, new Object[] {
                        MessageLookup.getInstance().lookupLabel(ConfigurationConstants.GROUP, this.getUserContext()),
                        MessageLookup.getInstance().lookupLabel(ConfigurationConstants.CLIENT, this.getUserContext()) });
            }
        }
    }

    private void checkIfClientCanBeActive(final Short newStatus) throws CustomerException {
        boolean loanOfficerActive = false;
        boolean branchInactive = false;
        short officeId = getOffice().getOfficeId();
        if (getPersonnel() == null || getPersonnel().getPersonnelId() == null) {
            throw new CustomerException(ClientConstants.CLIENT_LOANOFFICER_NOT_ASSIGNED);
        }
        if (getCustomerMeeting() == null || getCustomerMeeting().getMeeting() == null) {
            throw new CustomerException(GroupConstants.MEETING_NOT_ASSIGNED);
        }
        if (getPersonnel() != null) {
            try {
                loanOfficerActive = getOfficePersistence().hasActivePeronnel(officeId);
            } catch (PersistenceException e) {
                throw new CustomerException(e);
            }
        }

        try {
            branchInactive = getOfficePersistence().isBranchInactive(officeId);
        } catch (PersistenceException e) {
            throw new CustomerException(e);
        }
        if (loanOfficerActive == false) {
            throw new CustomerException(CustomerConstants.CUSTOMER_LOAN_OFFICER_INACTIVE_EXCEPTION,
                    new Object[] { MessageLookup.getInstance().lookup(ConfigurationConstants.BRANCHOFFICE,
                            getUserContext()) });
        }
        if (branchInactive == true) {
            throw new CustomerException(CustomerConstants.CUSTOMER_BRANCH_INACTIVE_EXCEPTION,
                    new Object[] { MessageLookup.getInstance().lookup(ConfigurationConstants.BRANCHOFFICE,
                            getUserContext()) });
        }
    }

    private void createAccountsForClient() throws CustomerException {
        if (offeringsAssociatedInCreate != null) {
            for (ClientInitialSavingsOfferingEntity clientOffering : offeringsAssociatedInCreate) {
                try {
                    SavingsOfferingBO savingsOffering = getSavingsPrdPersistence().getSavingsProduct(
                            clientOffering.getSavingsOffering().getPrdOfferingId());
                    if (savingsOffering.isActive()) {
                        List<CustomFieldDefinitionEntity> customFieldDefs = getSavingsPersistence()
                                .retrieveCustomFieldsDefinition(EntityType.SAVINGS.getValue());
                        addAccount(new SavingsBO(getUserContext(), savingsOffering, this, AccountState.SAVINGS_ACTIVE,
                                savingsOffering.getRecommendedAmount(),
                                createCustomFieldViewsForClientSavingsAccount(customFieldDefs)));
                    }
                } catch (PersistenceException pe) {
                    throw new CustomerException(pe);
                } catch (AccountException pe) {
                    throw new CustomerException(pe);
                }
            }
        }
    }

    private List<CustomFieldView> createCustomFieldViewsForClientSavingsAccount(
            final List<CustomFieldDefinitionEntity> customFieldDefs) {
        List<CustomFieldView> customFields = new ArrayList<CustomFieldView>();
        for (CustomFieldDefinitionEntity customFieldDef : customFieldDefs) {
            customFields.add(new CustomFieldView(customFieldDef.getFieldId(), customFieldDef.getDefaultValue(),
                    customFieldDef.getFieldType()));
        }
        return customFields;
    }

    private boolean isGroupStatusLower(final Short clientStatusId, final Short parentStatus) {
        return isGroupStatusLower(CustomerStatus.fromInt(clientStatusId), CustomerStatus.fromInt(parentStatus));
    }

    private void validateOfferings(final List<SavingsOfferingBO> offeringsSelected) throws CustomerException {
        if (offeringsSelected != null) {
            for (int i = 0; i < offeringsSelected.size() - 1; i++) {
                for (int j = i + 1; j < offeringsSelected.size(); j++) {
                    if (offeringsSelected.get(i).getPrdOfferingId().equals(offeringsSelected.get(j).getPrdOfferingId())) {
                        throw new CustomerException(ClientConstants.ERRORS_DUPLICATE_OFFERING_SELECTED);
                    }
                }
            }
        }
    }

    private void createDepositSchedule() throws CustomerException {
        try {
            if (getParentCustomer() != null) {
                List<SavingsBO> savingsList = getCustomerPersistence().retrieveSavingsAccountForCustomer(
                        getParentCustomer().getCustomerId());
                if (getParentCustomer().getParentCustomer() != null) {
                    savingsList.addAll(getCustomerPersistence().retrieveSavingsAccountForCustomer(
                            getParentCustomer().getParentCustomer().getCustomerId()));
                }
                for (SavingsBO savings : savingsList) {
                    savings.setUserContext(getUserContext());
                    savings.generateAndUpdateDepositActionsForClient(this);
                }
            }
        } catch (PersistenceException pe) {
            throw new CustomerException(pe);
        } catch (AccountException ae) {
            throw new CustomerException(ae);
        }
    }

    public void updateClientFlag() throws CustomerException, PersistenceException {
        this.groupFlag = YesNoFlag.NO.getValue();
        update();

    }

    public void validateBeforeAddingClientToGroup() throws CustomerException {
        if (isClientCancelledOrClosed()) {
            throw new CustomerException(CustomerConstants.CLIENT_IS_CLOSED_OR_CANCELLED_EXCEPTION);
        }
        if (isAnyLoanAccountOpen()) {
            throw new CustomerException(CustomerConstants.CLIENT_HAVE_OPEN_LOAN_ACCOUNT_EXCEPTION);
        }
    }

    private boolean isClientCancelledOrClosed() throws CustomerException {
        return getStatus() == CustomerStatus.CLIENT_CLOSED || getStatus() == CustomerStatus.CLIENT_CANCELLED ? true
                : false;
    }

    public void addClientToGroup(final GroupBO newParent) throws CustomerException {
        validateAddClientToGroup(newParent);

        if (!isSameBranch(newParent.getOffice())) {
            makeCustomerMovementEntries(newParent.getOffice());
        }

        setParentCustomer(newParent);
        addCustomerHierarchy(new CustomerHierarchyEntity(this, newParent));
        handleAddClientToGroup();
        childAddedForParent(newParent);
        setSearchId(newParent.getSearchId() + "." + String.valueOf(newParent.getMaxChildCount()));
        newParent.setUserContext(getUserContext());
        newParent.update();

        groupFlag = YesNoFlag.YES.getValue();
        update();
    }

    private void validateAddClientToGroup(final GroupBO toGroup) throws CustomerException {

        if (toGroup == null) {
            throw new CustomerException(CustomerConstants.INVALID_PARENT);
        }
        validateForGroupStatus(toGroup.getStatus());

    }

    public void attachPpiSurveyInstance(final SurveyInstance ppiSurvey) {
        /* TODO not implemented yet */
    }

    @Override
    public void updatePerformanceHistoryOnDisbursement(final LoanBO loan, final Money disburseAmount) {
        ClientPerformanceHistoryEntity performanceHistory = (ClientPerformanceHistoryEntity) getPerformanceHistory();
        performanceHistory.updateOnDisbursement(loan.getLoanOffering());
    }

    @Override
    public void updatePerformanceHistoryOnWriteOff(final LoanBO loan) {
        ClientPerformanceHistoryEntity performanceHistory = (ClientPerformanceHistoryEntity) getPerformanceHistory();
        performanceHistory.updateOnWriteOff(loan.getLoanOffering());
    }

    @Override
    public void updatePerformanceHistoryOnReversal(final LoanBO loan, final Money lastLoanAmount) throws CustomerException {
        ClientPerformanceHistoryEntity clientPerfHistory = (ClientPerformanceHistoryEntity) getPerformanceHistory();
        clientPerfHistory.updateOnReversal(loan.getLoanOffering(), lastLoanAmount);
    }

    @Override
    public void updatePerformanceHistoryOnRepayment(final LoanBO loan, final Money totalAmount) {
        ClientPerformanceHistoryEntity clientPerfHistory = (ClientPerformanceHistoryEntity) getPerformanceHistory();
        clientPerfHistory.updateOnRepayment(totalAmount);
    }

    @Override
    public void updatePerformanceHistoryOnLastInstlPayment(final LoanBO loan, final Money totalAmount) throws CustomerException {
        updatePerformanceHistoryOnRepayment(loan, totalAmount);
    }

    public void setSavingsPersistence(final SavingsPersistence savingsPersistence) {
        this.savingsPersistence = savingsPersistence;
    }

    public SavingsPersistence getSavingsPersistence() {
        if (null == savingsPersistence) {
            savingsPersistence = new SavingsPersistence();
        }
        return savingsPersistence;
    }

    public void setSavingsPrdPersistence(final SavingsPrdPersistence savingsPrdPersistence) {
        this.savingsPrdPersistence = savingsPrdPersistence;
    }

    public SavingsPrdPersistence getSavingsPrdPersistence() {
        if (null == savingsPrdPersistence) {
            savingsPrdPersistence = new SavingsPrdPersistence();
        }
        return savingsPrdPersistence;
    }

    public ClientPersistence getClientPersistence() {
        if (null == clientPersistence) {
            clientPersistence = new ClientPersistence();
        }
        return clientPersistence;
    }

    public void setClientPersistence(final ClientPersistence clientPersistence) {
        this.clientPersistence = clientPersistence;
    }
}
