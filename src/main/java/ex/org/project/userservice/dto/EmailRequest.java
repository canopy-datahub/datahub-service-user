package ex.org.project.userservice.dto;

import ex.org.project.userservice.util.EmailRequestType;
import java.util.*;

public record EmailRequest(
        String type,
        List<String> to,
        List<String> cc,
        String from,
        String subject,
        Map<String, String> props
) {
    public EmailRequest(EmailRequestType emailRequestType, List<String> to, List<String> cc, String from, Map<String, String> props){
        this(
                emailRequestType.type,
                to,
                cc,
                from,
                emailRequestType.subject,
                props
        );
    }

}
