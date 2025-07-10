package ex.org.project.userservice.util;

/**
 * Enum containing the different types of emails that can be sent by User Service.
 * type: the type of email request
 * subject: the subject line of the email to be sent
 */
public enum EmailRequestType {
    SUPPORT_REQUEST_SUBMISSION("Support Request Submission", "Support Request Submitted"),
    SUPPORT_REQUEST_ASSIGNMENT("Support Request Assignment", "Support Request Update"),
    SUPPORT_REQUEST_RESOLVED("Support Request Resolved", "Support Request Resolved"),
    SUPPORT_REQUEST_CLOSED("Support Request Closed", "Support Request Closed"),
    WELCOME_EMAIL("Welcome Email", "Welcome to the Data Hub!");

    public final String type;
    public final String subject;

    EmailRequestType(String type, String subject){
        this.type = type;
        this.subject = subject;
    }

}
