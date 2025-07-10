package ex.org.project.userservice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserDTO {
	private Integer id;
	private String firstName;
	private String middleInitial;
	private String lastName;
	private String email;
	private String institution;
	private List<String> roles;
	private String sessionID;
	private String jobTitle;
	private String orcidId;
	private String status;
	private Boolean dhpUser;
	private String researcherLevel;
	private String dcc;
	private String redirectUrl;
	private ZonedDateTime createdAt;
	private ZonedDateTime updateAt;

	public UserDTO(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
}
