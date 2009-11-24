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

package org.mifos.application.ppi.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import junitx.framework.ObjectAssert;

import org.joda.time.DateMidnight;
import org.mifos.application.personnel.business.PersonnelBO;
import org.mifos.application.personnel.util.helpers.PersonnelConstants;
import org.mifos.application.personnel.util.helpers.PersonnelLevel;
import org.mifos.application.ppi.helpers.Country;
import org.mifos.application.ppi.helpers.XmlPPISurveyParser;
import org.mifos.application.ppi.persistence.PPIPersistence;
import org.mifos.application.surveys.business.Question;
import org.mifos.application.surveys.business.QuestionChoice;
import org.mifos.application.surveys.business.Survey;
import org.mifos.application.surveys.business.SurveyInstance;
import org.mifos.application.surveys.business.SurveyQuestion;
import org.mifos.application.surveys.helpers.AnswerType;
import org.mifos.application.surveys.helpers.SurveyState;
import org.mifos.application.surveys.helpers.SurveyType;
import org.mifos.config.GeneralConfig;
import org.mifos.framework.MifosInMemoryIntegrationTestCase;
import org.mifos.framework.business.util.Name;
import org.mifos.framework.exceptions.ValidationException;
import org.mifos.framework.hibernate.helper.StaticHibernateUtil;
import org.mifos.framework.persistence.TestDatabase;
import org.mifos.framework.util.helpers.TestObjectFactory;
import org.springframework.core.io.ClassPathResource;

public class PPISurveyTest extends MifosInMemoryIntegrationTestCase {
    private static final double DELTA = 0.00000001;
    private PPIPersistence persistence;
    private TestDatabase database;

    public PPISurveyTest() {
        super();
        StaticHibernateUtil.initialize();
    }

    @Override
    public void setUp() {
        super.setUp();
        persistence = new PPIPersistence();
        database = TestDatabase.makeStandard();
        database.installInThreadLocal();
    }

    @Override
    public void tearDown() {
        StaticHibernateUtil.resetDatabase();
        super.tearDown();
    }

    public void testCreateSurveyInstance() throws Exception {
        Survey survey = new PPISurvey();
        SurveyInstance instance = survey.createSurveyInstance();
       Assert.assertTrue("Instance should be instance of PpiSurveyInstance", PPISurvey.class.isInstance(survey));
    }

    public void testCreateSurvey() throws Exception {
        PPISurvey ppiSurvey = makePPISurvey("PPI Test Survey");

        Survey regularSurvey = new Survey("NON-PPI Test Survey", SurveyState.ACTIVE, SurveyType.CLIENT);

        persistence.createOrUpdate(ppiSurvey);
        persistence.createOrUpdate(regularSurvey);

       Assert.assertEquals(1, persistence.retrieveAllPPISurveys().size());
       Assert.assertEquals(2, persistence.retrieveAllSurveys().size());

        PPISurvey dbPPISurvey = persistence.retrieveActivePPISurvey();
        Assert.assertNotNull(dbPPISurvey);
       Assert.assertEquals(ppiSurvey.getQuestions().size(), dbPPISurvey.getQuestions().size());
       Assert.assertEquals(ppiSurvey.getNonPoorMin(), dbPPISurvey.getNonPoorMin());
       Assert.assertEquals(ppiSurvey.getName(), dbPPISurvey.getName());
       Assert.assertEquals(ppiSurvey.getCountry(), dbPPISurvey.getCountry());
    }

    public static PPISurvey makePPISurvey(String name) throws ValidationException {
        PPISurvey survey = new PPISurvey(name, SurveyState.ACTIVE, SurveyType.CLIENT, Country.INDIA);

        survey.setVeryPoorMin(0);
        survey.setVeryPoorMax(25);
        survey.setPoorMin(26);
        survey.setPoorMax(50);
        survey.setAtRiskMin(51);
        survey.setAtRiskMax(75);
        survey.setNonPoorMin(76);
        survey.setNonPoorMax(100);

        List<SurveyQuestion> questions = new ArrayList<SurveyQuestion>();

        Question question = new Question("Test Question 1", "What is this question?", AnswerType.CHOICE);
        question.setChoices(new ArrayList<QuestionChoice>());
        PPIChoice choice = new PPIChoice("First choice");
        choice.setChoiceId(1);
        choice.setPoints(20);
        question.getChoices().add(choice);
        survey.addQuestion(question, true);
        survey.setQuestions(questions);

        addLikelihoods(survey);
        return survey;
    }

    private static void addLikelihoods(PPISurvey survey) throws ValidationException {
        List<PPILikelihood> likelihoods = new ArrayList<PPILikelihood>();
        PPILikelihood likelihood1 = new PPILikelihood(0, 49, 10, 50);
        likelihood1.setSurvey(survey);
        likelihoods.add(likelihood1);
        PPILikelihood likelihood2 = new PPILikelihood(50, 100, 20, 30);
        likelihood2.setSurvey(survey);
        likelihoods.add(likelihood2);
        survey.setLikelihoods(likelihoods);
    }

    public void testDefaultPovertyBandLimits() throws Exception {
        PPISurvey ppiSurvey = new PPISurvey("PPI Test Survey", SurveyState.ACTIVE, SurveyType.CLIENT, Country.INDIA);
        ppiSurvey.populateDefaultValues();
        int nonPoorMax = GeneralConfig.getMaxPointsPerPPISurvey();

       Assert.assertTrue(0 == ppiSurvey.getVeryPoorMin());
       Assert.assertTrue(ppiSurvey.getVeryPoorMin() <= ppiSurvey.getVeryPoorMax());
       Assert.assertTrue(ppiSurvey.getVeryPoorMax() == ppiSurvey.getPoorMin() - 1);
       Assert.assertTrue(ppiSurvey.getPoorMin() <= ppiSurvey.getPoorMax());
       Assert.assertTrue(ppiSurvey.getPoorMax() == ppiSurvey.getAtRiskMin() - 1);
       Assert.assertTrue(ppiSurvey.getAtRiskMin() <= ppiSurvey.getAtRiskMax());
       Assert.assertTrue(ppiSurvey.getAtRiskMax() == ppiSurvey.getNonPoorMin() - 1);
       Assert.assertTrue(ppiSurvey.getNonPoorMin() <= ppiSurvey.getNonPoorMax());
       Assert.assertTrue(ppiSurvey.getNonPoorMax() == nonPoorMax);
    }

    public void testRetrieve() throws Exception {
        PPISurvey ppiSurvey = new PPISurvey("PPI Test Survey", SurveyState.ACTIVE, SurveyType.CLIENT, Country.INDIA);
        addLikelihoods(ppiSurvey);
        persistence.createOrUpdate(ppiSurvey);

        PPISurvey retrievedPPISurvey = persistence.retrieveActivePPISurvey();
       Assert.assertEquals("PPI Test Survey", retrievedPPISurvey.getName());
    }

    public void testRetrieveById() throws Exception {
        PPISurvey ppiSurvey = new PPISurvey("PPI Test Survey", SurveyState.ACTIVE, SurveyType.CLIENT, Country.INDIA);
        addLikelihoods(ppiSurvey);
        persistence.createOrUpdate(ppiSurvey);

        Survey retrievedPPISurvey = persistence.getSurvey(ppiSurvey.getSurveyId());
       Assert.assertEquals("PPI Test Survey", retrievedPPISurvey.getName());
    }

    public void testRetrieveRegularSurvey() throws Exception {
        Survey regularSurvey = new Survey("PPI Test Survey", SurveyState.ACTIVE, SurveyType.CLIENT);
        persistence.createOrUpdate(regularSurvey);
        int surveyId = regularSurvey.getSurveyId();

        Survey retrievedSurvey = persistence.getSurvey(surveyId);
       Assert.assertEquals("PPI Test Survey", retrievedSurvey.getName());
        Assert.assertFalse(retrievedSurvey instanceof PPISurvey);

       Assert.assertEquals(null, new PPIPersistence().getPPISurvey(surveyId));
    }

    public void testNotFound() throws Exception {
        Survey retrieved = persistence.getSurvey(23423);
       Assert.assertEquals(null, retrieved);
    }

    public void testViaInstance() throws Exception {
        PPISurvey ppiSurvey = makePPISurvey("survey name");
        persistence.createOrUpdate(ppiSurvey);

        SurveyInstance instance = new SurveyInstance();
        instance.setSurvey(ppiSurvey);
        instance.setDateConducted(new Date(new DateMidnight("2007-06-27").getMillis()));
        PersonnelBO systemUser = makeSystemUser();
        instance.setOfficer(systemUser);
        instance.setCreator(systemUser);
        persistence.createOrUpdate(instance);
        int instanceId = instance.getInstanceId();

        SurveyInstance retrieved = persistence.getInstance(instanceId);
        Survey directSurvey = retrieved.getSurvey();
        // ObjectAssert.assertNotInstanceOf(PPISurvey.class, directSurvey);
        ObjectAssert.assertInstanceOf(Survey.class, directSurvey);

        Survey retrievedSurvey = persistence.getPPISurvey(directSurvey.getSurveyId());
        ObjectAssert.assertInstanceOf(PPISurvey.class, retrievedSurvey);
    }

    public void testLoadFromXmlAndStoreToDatabase() throws Exception {
        XmlPPISurveyParser parser = new XmlPPISurveyParser();
        PPISurvey survey = new PPISurvey();
        ClassPathResource surveyXml = new ClassPathResource("org/mifos/framework/util/resources/ppi/PPISurveyINDIA.xml");
        parser.parseInto(surveyXml.getPath(), survey);

        survey.setAppliesTo(SurveyType.CLIENT);
        persistence.createOrUpdate(survey);

        PPISurvey retrievedSurvey = persistence.retrievePPISurveyByCountry(Country.INDIA);
       Assert.assertEquals("PPI Survey India", retrievedSurvey.getName());
       Assert.assertEquals(Country.INDIA, retrievedSurvey.getCountryAsEnum());

       Assert.assertEquals(10, retrievedSurvey.getQuestions().size());
       Assert.assertEquals("Number of children", retrievedSurvey.getQuestion(0).getShortName());
       Assert.assertEquals("What is the household's primary energy source for cooking?", retrievedSurvey.getQuestion(1)
                .getQuestionText());

       Assert.assertEquals(0, retrievedSurvey.getLikelihood(2).getOrder());
       Assert.assertEquals(1, retrievedSurvey.getLikelihood(5).getOrder());
       Assert.assertEquals(5, retrievedSurvey.getLikelihood(25).getOrder());

       Assert.assertEquals(6.9, retrievedSurvey.getLikelihood(43).getBottomHalfBelowPovertyLinePercent(), DELTA);
       Assert.assertEquals(29.5, retrievedSurvey.getLikelihood(43).getTopHalfBelowPovertyLinePercent(), DELTA);
    }

    private PersonnelBO makeSystemUser() throws Exception {
        Date date = new Date(new DateMidnight("2004-06-27").getMillis());
        Name name = new Name("XYZ", null, null, null);
        return new PersonnelBO(PersonnelLevel.LOAN_OFFICER, null, Integer.valueOf("1"), TestObjectFactory.TEST_LOCALE,
                "PASSWORD", "a test officer", "xyz@yahoo.com", null, null, name, "govId", date, Integer.valueOf("1"),
                Integer.valueOf("1"), date, date, null, PersonnelConstants.SYSTEM_USER);
    }

}
