package us.mn.state.health.lims.dashboard.daoimpl;

import org.bahmni.feed.openelis.IT;
import org.junit.Before;
import us.mn.state.health.lims.analysis.daoimpl.AnalysisDAOImpl;
import us.mn.state.health.lims.analysis.valueholder.Analysis;
import us.mn.state.health.lims.analyte.daoimpl.AnalyteDAOImpl;
import us.mn.state.health.lims.analyte.valueholder.Analyte;
import us.mn.state.health.lims.common.action.IActionConstants;
import us.mn.state.health.lims.common.util.DateUtil;
import us.mn.state.health.lims.dashboard.valueholder.Order;
import us.mn.state.health.lims.patient.daoimpl.PatientDAOImpl;
import us.mn.state.health.lims.patient.valueholder.Patient;
import us.mn.state.health.lims.patientidentity.daoimpl.PatientIdentityDAOImpl;
import us.mn.state.health.lims.patientidentity.valueholder.PatientIdentity;
import us.mn.state.health.lims.patientidentitytype.util.PatientIdentityTypeMap;
import us.mn.state.health.lims.person.daoimpl.PersonDAOImpl;
import us.mn.state.health.lims.person.valueholder.Person;
import us.mn.state.health.lims.sample.daoimpl.SampleDAOImpl;
import us.mn.state.health.lims.sample.valueholder.Sample;
import us.mn.state.health.lims.samplehuman.daoimpl.SampleHumanDAOImpl;
import us.mn.state.health.lims.samplehuman.valueholder.SampleHuman;
import us.mn.state.health.lims.sampleitem.daoimpl.SampleItemDAOImpl;
import us.mn.state.health.lims.sampleitem.valueholder.SampleItem;
import us.mn.state.health.lims.samplesource.daoimpl.SampleSourceDAOImpl;
import us.mn.state.health.lims.samplesource.valueholder.SampleSource;
import us.mn.state.health.lims.statusofsample.util.StatusOfSampleUtil;
import us.mn.state.health.lims.test.dao.TestSectionDAO;
import us.mn.state.health.lims.test.daoimpl.TestDAOImpl;
import us.mn.state.health.lims.test.daoimpl.TestSectionDAOImpl;
import us.mn.state.health.lims.test.valueholder.Test;
import us.mn.state.health.lims.test.valueholder.TestSection;
import us.mn.state.health.lims.testanalyte.daoimpl.TestAnalyteDAOImpl;
import us.mn.state.health.lims.testanalyte.valueholder.TestAnalyte;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OrderListDAOImplIT extends IT {
    private String accessionNumber1;
    private String accessionNumber2;
    private String accessionNumber3;
    private String patientIdentityData;
    private String firstName;
    private String lastName;
    private Patient patient;
    private List<Test> allTests;
    private Date today;
    private Date dateInThePast;

    @Before
    public void setUp() throws Exception {
        accessionNumber1 = "05082013-001";
        accessionNumber2 = "05082013-002";
        accessionNumber3 = "05082013-003";
        patientIdentityData = "TEST1234567";
        firstName = "Some";
        lastName = "One";
        patient = createPatient(firstName, lastName, patientIdentityData);
        TestDAOImpl testDAO = createTests("SampleTest1", "SampleTest2", "SampleTest3", "SampleTest4");
        allTests = testDAO.getAllTests(true);
        today = new Date();
        dateInThePast = new Date(today.getTime() - 10 * 24 * 60 * 60 * 1000);
    }

    @org.junit.Test
    public void shouldGetAllTestsToday() {
    Sample sample1 = createSample(accessionNumber1, today);
        createSampleHuman(sample1, patient);
        SampleItem sampleItem1 = createSampleItem(sample1);

        Sample sample2 = createSample(accessionNumber2, today);
        createSampleHuman(sample2, patient);
        SampleItem sampleItem2 = createSampleItem(sample2);

        Sample sample3 = createSample(accessionNumber3, dateInThePast);
        createSampleHuman(sample3, patient);
        SampleItem sampleItem3 = createSampleItem(sample3);

        createAnalysis(sampleItem1,StatusOfSampleUtil.AnalysisStatus.NotTested,"Hematology",allTests.get(0));
        createAnalysis(sampleItem1,StatusOfSampleUtil.AnalysisStatus.TechnicalAcceptance,"Hematology",allTests.get(1));
        createAnalysis(sampleItem2,StatusOfSampleUtil.AnalysisStatus.Finalized,"Hematology",allTests.get(2));
        createAnalysis(sampleItem3,StatusOfSampleUtil.AnalysisStatus.Finalized,"Hematology",allTests.get(3));

        List<Order> allToday = new OrderListDAOImpl().getAllToday();

        assertEquals(2, allToday.size());

        assertTrue(hasAccessionNumber(allToday, accessionNumber1));
        assertTrue(hasAccessionNumber(allToday, accessionNumber2));
        assertFalse(hasAccessionNumber(allToday, accessionNumber3));

        assertEquals( 1, getByAccessionNumber(allToday, accessionNumber1).getPendingTestCount());
        assertEquals( 1, getByAccessionNumber(allToday, accessionNumber1).getPendingValidationCount());
        assertEquals( 2, getByAccessionNumber(allToday, accessionNumber1).getTotalTestCount());
        assertEquals( false, getByAccessionNumber(allToday, accessionNumber1).getIsCompleted());

        assertEquals( 0, getByAccessionNumber(allToday, accessionNumber2).getPendingTestCount());
        assertEquals( 0, getByAccessionNumber(allToday, accessionNumber2).getPendingValidationCount());
        assertEquals( 1, getByAccessionNumber(allToday, accessionNumber2).getTotalTestCount());
        assertEquals( true, getByAccessionNumber(allToday, accessionNumber2).getIsCompleted());
    }

    @org.junit.Test
    public void shouldGetAllPendingTestsBeforeToday() {
    Sample sample1 = createSample(accessionNumber1, dateInThePast);
        createSampleHuman(sample1, patient);
        SampleItem sampleItem1 = createSampleItem(sample1);

        Sample sample2 = createSample(accessionNumber2, today);
        createSampleHuman(sample2, patient);
        SampleItem sampleItem2 = createSampleItem(sample2);

        Sample sample3 = createSample(accessionNumber3, dateInThePast);
        createSampleHuman(sample3, patient);
        SampleItem sampleItem3 = createSampleItem(sample3);

        createAnalysis(sampleItem1, StatusOfSampleUtil.AnalysisStatus.NotTested, "Hematology", allTests.get(0));
        createAnalysis(sampleItem1, StatusOfSampleUtil.AnalysisStatus.TechnicalAcceptance, "Hematology", allTests.get(1));
        createAnalysis(sampleItem2, StatusOfSampleUtil.AnalysisStatus.Finalized, "Hematology", allTests.get(2));
        createAnalysis(sampleItem3, StatusOfSampleUtil.AnalysisStatus.Finalized, "Hematology", allTests.get(3));

        List<Order> allPendingBeforeToday = new OrderListDAOImpl().getAllPendingBeforeToday();

        assertEquals(1, allPendingBeforeToday.size());

        assertTrue(hasAccessionNumber(allPendingBeforeToday, accessionNumber1));
        assertFalse(hasAccessionNumber(allPendingBeforeToday, accessionNumber2));
        assertFalse(hasAccessionNumber(allPendingBeforeToday, accessionNumber3));

        assertEquals( 1, getByAccessionNumber(allPendingBeforeToday, accessionNumber1).getPendingTestCount());
        assertEquals( 1, getByAccessionNumber(allPendingBeforeToday, accessionNumber1).getPendingValidationCount());
        assertEquals( 2, getByAccessionNumber(allPendingBeforeToday, accessionNumber1).getTotalTestCount());
        assertEquals(false, getByAccessionNumber(allPendingBeforeToday, accessionNumber1).getIsCompleted());
    }

    private Order getByAccessionNumber(List<Order> orders, String accessionNumber) {
        for (Order order: orders) {
            if(order.getAccessionNumber().equals(accessionNumber)){
                return order;
            }
        }
        return null;
    }

    private boolean hasAccessionNumber(List<Order> orders, String accessionNumber) {
        return getByAccessionNumber(orders, accessionNumber) != null;
    }

    private TestDAOImpl createTests(String... samples) {
        TestDAOImpl testDAO = new TestDAOImpl();
        for (String sample : samples) {
            createATest(testDAO, sample);
        }
        return testDAO;
    }

    private Test createATest(TestDAOImpl testDAO, String testName) {
        Test test = createTest(testName);
        testDAO.insertData(test);
        TestAnalyteDAOImpl testAnalyteDAO = new TestAnalyteDAOImpl();
        testAnalyteDAO.insertData(createTestAnalyte(test, new AnalyteDAOImpl().readAnalyte("1")));
        return test;
    }

    private TestAnalyte createTestAnalyte(Test test, Analyte analyte) {
        TestAnalyte testAnalyte = new TestAnalyte();
        testAnalyte.setTest(test);
        testAnalyte.setAnalyte(analyte);
        testAnalyte.setSysUserId("1");
        testAnalyte.setResultGroup("1");
        return testAnalyte;
    }

    private Test createTest(String testName) {
        Test test = new Test();
        test.setTestName(testName);
        test.setDescription(testName);
        test.setIsActive(IActionConstants.YES);
        test.setSortOrder("1");
        test.setOrderable(Boolean.TRUE);
        test.setSysUserId("1");
        return test;
    }

    private Patient createPatient(String firstName, String lastName, String patientIdentityData) {
        Person person = new Person();
        person.setFirstName(firstName);
        person.setLastName(lastName);
        person.setSysUserId("1");
        new PersonDAOImpl().insertData(person);

        Patient patient = new Patient();
        patient.setPerson(person);
        patient.setGender("M");
        patient.setBirthDate(DateUtil.getNowAsTimestamp());
        patient.setSysUserId("1");
        patient.setLastupdated(DateUtil.getNowAsTimestamp());
        new PatientDAOImpl().insertData(patient);

        PatientIdentity patientIdentity = new PatientIdentity();
        patientIdentity.setPatientId(patient.getId());
        patientIdentity.setIdentityTypeId(PatientIdentityTypeMap.getInstance().getIDForType("ST"));
        patientIdentity.setIdentityData(patientIdentityData);
        patientIdentity.setSysUserId("1");
        new PatientIdentityDAOImpl().insertData(patientIdentity);

        return patient;
    }

    private SampleHuman createSampleHuman(Sample sample, Patient patient) {
        SampleHuman sampleHuman = new SampleHuman();
        sampleHuman.setPatientId(patient.getId());
        sampleHuman.setSampleId(sample.getId());
        sampleHuman.setSysUserId("1");

        new SampleHumanDAOImpl().insertData(sampleHuman);
        return sampleHuman;
    }

    private Analysis createAnalysis(SampleItem sampleItem, StatusOfSampleUtil.AnalysisStatus analysisStatus, String testSectionName, Test test) {
        TestSectionDAO testSectionDAO = new TestSectionDAOImpl();
        TestSection testSection = testSectionDAO.getTestSectionByName(testSectionName);
        Analysis analysis = new Analysis();
        analysis.setSampleItem(sampleItem);
        analysis.setAnalysisType(IActionConstants.ANALYSIS_TYPE_MANUAL);
        analysis.setStatusId(StatusOfSampleUtil.getStatusID(analysisStatus));
        analysis.setTest(test);
        analysis.setSysUserId("1");
        analysis.setLastupdated(sampleItem.getSample().getLastupdated());
        analysis.setTestSection(testSection);
        new AnalysisDAOImpl().insertData(analysis, false);
        return analysis;
    }

    private SampleItem createSampleItem(Sample startedSample) {
        SampleItem enteredSampleItem = new SampleItem();
        enteredSampleItem.setSample(startedSample);
        enteredSampleItem.setStatusId(StatusOfSampleUtil.getStatusID(StatusOfSampleUtil.SampleStatus.Entered));
        enteredSampleItem.setSortOrder("1");
        enteredSampleItem.setSysUserId("1");
        new SampleItemDAOImpl().insertData(enteredSampleItem);
        return enteredSampleItem;
    }

    private Sample createSample(String accessionNumber, Date date) {
        List<SampleSource> sampleSources = new SampleSourceDAOImpl().getAll();
        Sample sample = new Sample();
        sample.setAccessionNumber(accessionNumber);
        sample.setStatusId(StatusOfSampleUtil.getStatusID(StatusOfSampleUtil.OrderStatus.Started));
        sample.setEnteredDate(DateUtil.convertStringDateToSqlDate(new SimpleDateFormat("dd/MM/yyyy").format(new Date())));
        sample.setReceivedTimestamp(DateUtil.convertStringDateToTimestamp("01/01/2001 00:00"));
        sample.setSampleSource(sampleSources.get(0));
        sample.setSysUserId("1");
        sample.setLastupdated(new Timestamp(date.getTime()));
        new SampleDAOImpl().insertDataWithAccessionNumber(sample);
        return sample;
    }
}
