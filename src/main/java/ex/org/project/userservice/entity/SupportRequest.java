package ex.org.project.userservice.entity;

import ex.org.project.userservice.dto.SupportRequestDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "support_request")
public class SupportRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "requestor_user_id")
    private Integer requestorUserId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "request_title")
    private String requestTitle;

    @Column(name = "request_detail")
    private String requestDetail;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private LookupSupportRequestType requestType;
    
    @Column(name = "piority")
    private Integer severity;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private LookupStatus status;

    @Column(name = "assignee_user_id")
    private Integer assigneeUserId;
    
    @Column(name = "assignee_email")
    private String assigneeEmail;
    
    @Column(name = "assigned_at")
    private ZonedDateTime assignedAt;
    
    @Column(name = "resolved_at")
    private ZonedDateTime resolvedAt;

    @ManyToOne
    @JoinColumn(name = "resolution_type_id")
    private ResolutionType resolutionType;

    @Column(name = "update_at")
    private ZonedDateTime updateAt;

    @Column(name = "updated_by")
    private Integer updatedBy;

    @Column(name = "tech_note")
    private String notes;

    @Column(name = "institution")
    private String institution;

    @Transient
    private String assigneeName;

    @PreUpdate
    public void setUpdatedAt() {
        this.updateAt = ZonedDateTime.now();
    }

    @PrePersist
    public void setCreatedAt() {
        this.createdAt = ZonedDateTime.now();
    }

	public SupportRequest(String fullName, String email, String requestTitle, String requestDetail, ZonedDateTime createdAt, LookupStatus status,
			LookupSupportRequestType requestType) {
		super();
		this.fullName = fullName;
		this.email = email;
		this.requestTitle = requestTitle;
		this.requestDetail = requestDetail;
		this.requestType = requestType;
        this.status = status;
        this.createdAt = createdAt;
	}

    public SupportRequest(SupportRequestDTO supportRequestDTO, LookupStatus status, LookupSupportRequestType requestType){
        this.fullName = supportRequestDTO.getFullName();
        this.email = supportRequestDTO.getEmail();
        this.requestTitle = supportRequestDTO.getRequestTitle();
        this.requestDetail = supportRequestDTO.getRequestDetail();
        this.requestType = requestType;
        this.status = status;
        this.createdAt = supportRequestDTO.getCreatedAt();
        this.requestorUserId = supportRequestDTO.getRequestorUserId();
        this.institution = supportRequestDTO.getInstitution();
    }

}
