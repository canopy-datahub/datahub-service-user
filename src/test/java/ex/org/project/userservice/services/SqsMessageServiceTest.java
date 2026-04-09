package ex.org.project.userservice.services;

import ex.org.project.userservice.dto.EmailRequest;
import ex.org.project.userservice.entity.LookupStatus;
import ex.org.project.userservice.entity.LookupSupportRequestType;
import ex.org.project.userservice.entity.SupportRequest;
import ex.org.project.userservice.service.SqsMessageService;
import ex.org.project.userservice.util.EmailRequestType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class SqsMessageServiceTest {

    @Mock
    private SqsAsyncClient sqsAsyncClient;
    private String supportEmailAddress = "test@email.com";
    private String emailQueue = "fakeQueue";
    private final SqsMessageService messageService = new SqsMessageService(sqsAsyncClient, emailQueue, supportEmailAddress);

    @Test
    public void testCreateUserSupportEmailRequestSubmission(){
        //setup
        SupportRequest mockSupportRequest = new SupportRequest(
                "Test McTestington",
                "user@email.com",
                "Testing Message Service",
                "I would like to test the SQS Message Service",
                ZonedDateTime.now(),
                new LookupStatus(),
                new LookupSupportRequestType()
        );
        mockSupportRequest.setId(1);
        EmailRequestType requestType = EmailRequestType.SUPPORT_REQUEST_SUBMISSION;
        //run
        EmailRequest emailRequest = messageService.createUserSupportEmailRequest(mockSupportRequest, requestType);
        //test
        assertEquals(requestType.type, emailRequest.type());
        assertEquals(requestType.subject, emailRequest.subject());
        assertEquals(mockSupportRequest.getId().toString(), emailRequest.props().get("supportRequestId"));
        assertEquals(mockSupportRequest.getFullName(), emailRequest.props().get("name"));
        assertEquals(mockSupportRequest.getEmail(), emailRequest.to().get(0));
    }

    @Test
    public void testCreateUserSupportEmailRequestResolved(){
        //setup
        SupportRequest mockSupportRequest = new SupportRequest(
                "Test McTestington",
                "user@email.com",
                "Testing Message Service",
                "I would like to test the SQS Message Service",
                ZonedDateTime.now(),
                new LookupStatus(),
                new LookupSupportRequestType()
        );
        mockSupportRequest.setId(1);
        EmailRequestType requestType = EmailRequestType.SUPPORT_REQUEST_RESOLVED;
        //run
        EmailRequest emailRequest = messageService.createUserSupportEmailRequest(mockSupportRequest, requestType);
        //test
        assertEquals(requestType.type, emailRequest.type());
        assertEquals(requestType.subject, emailRequest.subject());
        assertEquals(mockSupportRequest.getId().toString(), emailRequest.props().get("supportRequestId"));
        assertEquals(mockSupportRequest.getFullName(), emailRequest.props().get("name"));
        assertEquals(mockSupportRequest.getEmail(), emailRequest.to().get(0));
    }

    @Test
    public void testCreateUserSupportEmailRequestClosed(){
        //setup
        SupportRequest mockSupportRequest = new SupportRequest(
                "Test McTestington",
                "user@email.com",
                "Testing Message Service",
                "I would like to test the SQS Message Service",
                ZonedDateTime.now(),
                new LookupStatus(),
                new LookupSupportRequestType()
        );
        mockSupportRequest.setId(1);
        EmailRequestType requestType = EmailRequestType.SUPPORT_REQUEST_CLOSED;
        //run
        EmailRequest emailRequest = messageService.createUserSupportEmailRequest(mockSupportRequest, requestType);
        //test
        assertEquals(requestType.type, emailRequest.type());
        assertEquals(requestType.subject, emailRequest.subject());
        assertEquals(mockSupportRequest.getId().toString(), emailRequest.props().get("supportRequestId"));
        assertEquals(mockSupportRequest.getFullName(), emailRequest.props().get("name"));
        assertEquals(mockSupportRequest.getEmail(), emailRequest.to().get(0));
    }

    @Test
    public void testCreateSupportAssignmentEmailRequest(){
        //setup
        SupportRequest mockSupportRequest = new SupportRequest(
                "Test McTestington",
                "user@email.com",
                "Testing Message Service",
                "I would like to test the SQS Message Service",
                ZonedDateTime.now(),
                new LookupStatus(),
                new LookupSupportRequestType()
        );
        mockSupportRequest.setId(1);
        mockSupportRequest.setAssigneeEmail("support-staff@email.com");
        String staffName = "Staff McStaffington";
        //run
        EmailRequest emailRequest = messageService.createSupportAssignmentEmailRequest(mockSupportRequest, staffName);
        //test
        assertEquals(EmailRequestType.SUPPORT_REQUEST_ASSIGNMENT.type, emailRequest.type());
        assertEquals(EmailRequestType.SUPPORT_REQUEST_ASSIGNMENT.subject, emailRequest.subject());
        assertEquals(mockSupportRequest.getId().toString(), emailRequest.props().get("supportRequestId"));
        assertEquals(staffName, emailRequest.props().get("assignedUser"));
        assertEquals(mockSupportRequest.getAssigneeEmail(), emailRequest.to().get(0));
    }

}
