package ex.org.project.userservice.dto;

import lombok.Data;

@Data
public class RasRegistrationDTO {
	private String firstName;
	private String lastName;
	private String email;
	private String institutionName;
}
