package org.upgrad.upstac.testrequests.consultation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.testrequests.TestRequestUpdateService;
import org.upgrad.upstac.testrequests.flow.TestRequestFlowService;
import org.upgrad.upstac.users.User;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.upgrad.upstac.exception.UpgradResponseStatusException.asBadRequest;
import static org.upgrad.upstac.exception.UpgradResponseStatusException.asConstraintViolation;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    Logger log = LoggerFactory.getLogger(ConsultationController.class);

    @Autowired
    private TestRequestUpdateService testRequestUpdateService;

    @Autowired
    private TestRequestQueryService testRequestQueryService;

    @Autowired
    TestRequestFlowService  testRequestFlowService;

    @Autowired
    private UserLoggedInService userLoggedInService;

    /**
     * this is for Consultations Requested functionality in Doctor user
     *
     * @return list of all Test Requests with lab test completed status
     */
    @GetMapping("/in-queue")
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForConsultations()  {
        try {
            return testRequestQueryService.findBy(RequestStatus.LAB_TEST_COMPLETED);
        } catch(AppException e) {
            log.error(e.getStackTrace().toString());
            throw asBadRequest(e.getMessage());
        }
    }

    /**
     * this is for Request History functionality in Doctor user
     *
     * @return list of Test Requests for the logged in Doctor
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR')")
    public List<TestRequest> getForDoctor()  {
        try {
            User doctor = userLoggedInService.getLoggedInUser();
            return testRequestQueryService.findByDoctor(doctor);
        } catch(AppException e) {
            log.error(e.getStackTrace().toString());
            throw asBadRequest(e.getMessage());
        }
    }

    /**
     * this is for Assign to Me functionality under Consultations Requested tab in Doctor flow
     *
     * @param id - test request id
     * @return test request details
     */
    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/assign/{id}")
    public TestRequest assignForConsultation(@PathVariable Long id) {
        try {
            User doctor = userLoggedInService.getLoggedInUser();
            return testRequestUpdateService.assignForConsultation(id, doctor);
        }catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }

    /**
     * this is for Update Consultation functionality for Doctor
     *
     * @param id - test request id
     * @param consultationRequest - consultation request details
     * @return test request details
     */
    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PutMapping("/update/{id}")
    public TestRequest updateConsultation(@PathVariable Long id,@RequestBody CreateConsultationRequest consultationRequest) {
        try {
            User doctor = userLoggedInService.getLoggedInUser();
            return testRequestUpdateService.updateConsultation(id, consultationRequest, doctor);
        } catch (ConstraintViolationException e) {
            throw asConstraintViolation(e);
        }catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }

}
