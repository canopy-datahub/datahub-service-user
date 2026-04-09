package ex.org.project.userservice.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class SupportAssigneeDTO {
	private Integer id;
	private String firstName;
	private String middleInitial;
	private String lastName;
	private String email;
}
