package ex.org.project.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import ex.org.project.userservice.dto.EmailRequest;
import ex.org.project.userservice.entity.SupportRequest;
import ex.org.project.userservice.entity.User;
import ex.org.project.userservice.util.EmailRequestType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Service
public class SqsMessageService implements MessageService {

    private final SqsAsyncClient sqsAsyncClient;
    private final ObjectWriter objectWriter;
    private final String supportEmailAddress;
    private final String emailQueue;

    public SqsMessageService(SqsAsyncClient sqsAsyncClient,
                             @Value("${hub.emailQueue}") String emailQueue,
                             @Value("${hub.supportEmail}") String supportEmailAddress){
        this.sqsAsyncClient = sqsAsyncClient;
        this.emailQueue = emailQueue;
        this.supportEmailAddress = supportEmailAddress;
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    /** Sends EmailRequest object to SQS so that a corresponding email will be sent
     * @param emailRequest object containing details needed for an email
     * @return boolean mapping to the success of the sqs upload
     * @throws CompletionException if the message failed to be received by SQS
     */
    public boolean sendMessage(EmailRequest emailRequest){
        SendMessageRequest request = SendMessageRequest
                .builder()
                .queueUrl(emailQueue)
                .messageBody(emailRequestToJsonString(emailRequest))
                .build();
        CompletableFuture<SendMessageResponse> response = sqsAsyncClient.sendMessage(request);
        try {
            response.join();
            log.info("Email request sent to SQS{" + emailQueue + "}: " + emailRequest.toString());
            return true;
        } catch (CompletionException e){
            log.error("Error sending message to sqs", e);
            return false;
        }
    }

    public boolean sendWelcomeEmail(User user) {
        EmailRequest request = new EmailRequest(
                EmailRequestType.WELCOME_EMAIL,
                List.of(user.getEmail()),
                List.of(supportEmailAddress),
                supportEmailAddress,
                Map.of("name", String.format("%s %s", user.getFirstName(), user.getLastName()))
        );
        return sendMessage(request);
    }

    /**
     * Converts a SupportRequest object to a form that can be sent to SQS.
     * For support emails that will be received by a user
     * @param supportRequest object containing details of a support ticket
     * @param requestType EmailRequestType containing information on the type of email needing to be sent
     * @return EmailRequest object containing all data needed to send the corresponding email
     */
    public EmailRequest createUserSupportEmailRequest(SupportRequest supportRequest, EmailRequestType requestType){
        List<String> cc = new ArrayList<>();
        cc.add(supportEmailAddress);
        Map<String, String> props = new HashMap<>();
        props.put("name", supportRequest.getFullName());
        props.put("supportRequestId", supportRequest.getId().toString());

        return new EmailRequest(
                requestType,
                List.of(supportRequest.getEmail()),
                cc,
                supportEmailAddress,
                props
        );
    }

    /**
     * Converts a SupportRequest object to a form that can be sent to SQS.
     * For support emails that will be received by a support staff member when a ticket has been assigned to them
     * @param supportRequest object containing details of a support ticket
     * @param supportStaffName The name of the staff member the ticket is assigned to
     * @return EmailRequest object containing all data needed to send the corresponding email
     */
    public EmailRequest  createSupportAssignmentEmailRequest(SupportRequest supportRequest, String supportStaffName){
        List<String> cc = new ArrayList<>();
        Map<String, String> props = new HashMap<>();
        cc.add(supportEmailAddress);
        props.put("supportRequestId", supportRequest.getId().toString());
        props.put("assignedUser", supportStaffName);

        return new EmailRequest(
                EmailRequestType.SUPPORT_REQUEST_ASSIGNMENT,
                List.of(supportRequest.getAssigneeEmail()),
                cc,
                supportEmailAddress,
                props
        );
    }

    /**
     * Converts an EmailRequest object to json
     * @param emailRequest object to transform into json
     * @return json form of the provided EmailRequest as a String
     * @throws RuntimeException if the object cannot be processed
     */
    private String emailRequestToJsonString(EmailRequest emailRequest){
        try {
            return objectWriter.writeValueAsString(emailRequest);
        } catch (JsonProcessingException e){
            String errorMessage = "Object to json to string failure";
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
    }

}
