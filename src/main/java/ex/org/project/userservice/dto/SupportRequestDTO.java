package ex.org.project.userservice.dto;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupportRequestDTO {
	private Integer id;
    private Integer requestorUserId;
	private String requestTitle;
    private String fullName;
    private String email;
    private String requestType;
    private String status;
    private Integer severity;
    private String assigneeEmail;
    private Integer assigneeUserId;    
    private ZonedDateTime assignedAt;
    private ZonedDateTime createdAt;
    private ZonedDateTime updateAt;
    private ZonedDateTime resolvedAt;
    private String resolutionType;    
    private String requestDetail;
    private String notes;
    private Boolean canEdit;
    private String institution;

    @JsonIgnore
    public boolean isInvalidForInitialSave(){
        return isInvalidField(this.email) || isInvalidField(this.fullName) ||
                isInvalidField(this.institution) || isInvalidField(this.requestType) ||
                isInvalidField(this.requestTitle) || isInvalidField(this.requestDetail);
    }

    private boolean isInvalidField(String field) {
        return field == null || field.isBlank();
    }
}
