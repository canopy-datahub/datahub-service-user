package ex.org.project.userservice.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AuthUserDTO {

    private Integer id;
    private String firstName;
    private String middleInitial;
    private String lastName;
    private String email;
    private List<String> roles;
    private String sessionId;
    private String status;
    private String redirectUrl;

    public AuthUserDTO(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
