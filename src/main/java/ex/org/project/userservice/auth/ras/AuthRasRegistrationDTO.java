package ex.org.project.userservice.auth.ras;

import lombok.Data;

@Data
public class AuthRasRegistrationDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String institutionName;
}
