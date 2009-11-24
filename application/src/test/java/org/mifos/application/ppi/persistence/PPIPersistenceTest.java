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

package org.mifos.application.ppi.persistence;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.mifos.application.master.business.CustomFieldType;
import org.mifos.application.master.business.CustomFieldView;
import org.mifos.application.office.business.OfficeBO;
import org.mifos.application.personnel.business.PersonnelBO;
import org.mifos.application.personnel.util.helpers.PersonnelConstants;
import org.mifos.application.personnel.util.helpers.PersonnelLevel;
import org.mifos.application.ppi.business.PPIChoice;
import org.mifos.application.ppi.business.PPILikelihood;
import org.mifos.application.ppi.business.PPISurvey;
import org.mifos.application.ppi.business.PPISurveyInstance;
import org.mifos.application.ppi.helpers.Country;
import org.mifos.application.surveys.business.Question;
import org.mifos.application.surveys.business.SurveyInstance;
import org.mifos.application.surveys.business.SurveyQuestion;
import org.mifos.application.surveys.business.SurveyResponse;
import org.mifos.application.surveys.helpers.AnswerType;
import org.mifos.application.surveys.helpers.SurveyState;
import org.mifos.application.surveys.helpers.SurveyType;
import org.mifos.framework.MifosInMemoryIntegrationTestCase;
import org.mifos.framework.business.util.Address;
import org.mifos.framework.business.util.Name;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class PPIPersistenceTest extends MifosInMemoryIntegrationTestCase {
    private static final double DELTA = 0.00000001;
    private PPIPersistence persistence;

    public PPIPersistenceTest() {
        super();
        StaticHibernateUtil.initialize();
    }

    public void setUp() {
        super.setUp();
        persistence = new PPIPersistence();
    }

    public void testLikelihoods() throws Exception {
        int surveyId = createSurveyWithLikelihoods("surveyName");
        PPISurvey retreivedSurvey = (PPISurvey) persistence.getSurvey(surveyId);
        Assert.assertNotNull(retreivedSurvey);
        PPILikelihood lh = retreivedSurvey.getLikelihood(17);
       Assert.assertEquals(0, lh.getScoreFrom());
       Assert.assertEquals(20, lh.getScoreTo());
       Assert.assertEquals(80.0, lh.getBottomHalfBelowPovertyLinePercent(), DELTA);
       Assert.assertEquals(20.0, lh.getTopHalfBelowPovertyLinePercent(), DELTA);

        lh = retreivedSurvey.getLikelihood(46);
       Assert.assertEquals(21, lh.getScoreFrom());
       Assert.assertEquals(100, lh.getScoreTo());
       Assert.assertEquals(30.0, lh.getBottomHalfBelowPovertyLinePercent(), DELTA);
       Assert.assertEquals(70.0, lh.getTopHalfBelowPovertyLinePercent(), DELTA);
    }

    public void testRetrieveActivePPISurvey() throws Exception {
        createSurveyWithLikelihoods("surveyName");
       Assert.assertEquals("surveyName", persistence.retrieveActivePPISurvey().getName());
    }

    public void testRetrieveAllPPISurveys() throws Exception {
        createSurveyWithLikelihoods("survey1");
       Assert.assertEquals(1, persistence.retrieveAllPPISurveys().size());
    }

    public void testRetrievePPISurveyByCountry() throws Exception {
        createSurveyWithLikelihoods("surveyForIndia");
       Assert.assertEquals(Country.INDIA, persistence.retrievePPISurveyByCountry(Country.INDIA).getCountryAsEnum());
    }

    public void testGetPPISurvey() throws Exception {
        createSurveyWithLikelihoods("surveyName");
       Assert.assertEquals("surveyName", persistence.getPPISurvey(1).getName());
    }

    public void testPersistPPISurveyInstance() throws Exception {
        int surveyId = createSurveyWithLikelihoods("surveyName");
        PPISurvey survey = persistence.getPPISurvey(surveyId);
        int instanceId = createSurveyInstance(survey);

        PPISurveyInstance retrievedInstance = (PPISurveyInstance) persistence.getInstance(instanceId);
       Assert.assertEquals("surveyName", retrievedInstance.getSurvey().getName());
       Assert.assertEquals(5, retrievedInstance.getScore());
       Assert.assertEquals(80.0, retrievedInstance.getBottomHalfBelowPovertyLinePercent(), DELTA);
       Assert.assertEquals(20.0, retrievedInstance.getTopHalfBelowPovertyLinePercent(), DELTA);
    }

    private int createSurveyInstance(PPISurvey survey) throws Exception {
        PPISurveyInstance instance = new PPISurveyInstance();
        instance.setSurvey(survey);
        instance.setCreator(createOfficer());
        instance.setDateConducted(new Date());
        Set<SurveyResponse> responses = new HashSet<SurveyResponse>();
        responses.add(createSurveyResponse(instance));
        instance.setSurveyResponses(responses);
        instance.initialize();
        persistence.createOrUpdate(instance);
        return instance.getInstanceId();
    }

    private PersonnelBO createOfficer() throws Exception {
        TestObjectFactory factory = new TestObjectFactory();
        OfficeBO office = factory.getOffice(TestObjectFactory.HEAD_OFFICE);
        List<CustomFieldView> customFieldView = new ArrayList<CustomFieldView>();
        customFieldView.add(new CustomFieldView((short) 9, "123456", CustomFieldType.NUMERIC));
        Name name = new Name("XYZ", null, null, null);
        Address address = new Address("abcd" + "ppiSurvey", "abcd", "abcd", "abcd", "abcd", "abcd", "abcd", "abcd");
        Date date = new Date();
        return new PersonnelBO(PersonnelLevel.LOAN_OFFICER, office, Integer.valueOf("1"),
                TestObjectFactory.TEST_LOCALE, "PASSWORD", "officer" + System.currentTimeMillis(), "officer@mifos.org",
                null, customFieldView, name, "govId" + "ppiSurvey", date, Integer.valueOf("1"), Integer.valueOf("1"),
                date, date, address, PersonnelConstants.SYSTEM_USER);
    }

    private SurveyResponse createSurveyResponse(SurveyInstance instance) throws Exception {
        Question question = new Question("shortName", "questionText", AnswerType.CHOICE);
        PPIChoice choice1 = new PPIChoice("choice1");
        choice1.setPoints(5);
        PPIChoice choice2 = new PPIChoice("choice2");
        choice2.setPoints(10);
        question.addChoice(choice1);
        question.addChoice(choice2);
        SurveyQuestion surveyQuestion = new SurveyQuestion();
        surveyQuestion.setSurvey(instance.getSurvey());
        surveyQuestion.setQuestion(question);
        surveyQuestion.setOrder(0);
        SurveyResponse response = new SurveyResponse(instance, surveyQuestion);
        response.setChoiceValue(choice1);
        persistence.createOrUpdate(response);
        return response;
    }

    private int createSurveyWithLikelihoods(String name) throws Exception {
        PPISurvey survey = new PPISurvey(name, SurveyState.ACTIVE, SurveyType.CLIENT, Country.INDIA);
        List<PPILikelihood> list = new ArrayList<PPILikelihood>();
        PPILikelihood likelihood1 = new PPILikelihood(0, 20, 80, 20);
        likelihood1.setSurvey(survey);
        list.add(likelihood1);
        PPILikelihood likelihood2 = new PPILikelihood(21, 100, 30, 70);
        likelihood2.setSurvey(survey);
        list.add(likelihood2);
        survey.setLikelihoods(list);
        persistence.createOrUpdate(survey);
        return survey.getSurveyId();
    }
}
