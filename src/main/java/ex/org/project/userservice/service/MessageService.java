package ex.org.project.userservice.service;

import ex.org.project.userservice.dto.EmailRequest;
import ex.org.project.userservice.entity.SupportRequest;
import ex.org.project.userservice.entity.User;
import ex.org.project.userservice.util.EmailRequestType;

public interface MessageService {

    boolean sendMessage(EmailRequest emailRequest);

    EmailRequest createUserSupportEmailRequest(SupportRequest supportRequest, EmailRequestType requestType);

    EmailRequest  createSupportAssignmentEmailRequest(SupportRequest supportRequest, String supportStaffName);

    boolean sendWelcomeEmail(User user);

}
