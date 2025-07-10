package ex.org.project.userservice.entity;

import java.time.ZonedDateTime;
import java.util.List;

import ex.org.project.userservice.dto.UserDTO;
import ex.org.project.userservice.dto.UserRegistrationDTO;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "middle_initial")
    private String middleInitial;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Column(name = "email_address")
    private String email;

    @ManyToOne
    @JoinColumn(name = "institution_id")
    private Institution institution;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private LookupStatus status;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "modified_at")
    private ZonedDateTime updateAt;

    @ManyToOne
    @JoinColumn(name = "researcher_level_id")
    private LookupResearcherLevel researcherLevel;

    @Column(name = "accept_terms")
    private Boolean acceptTerms;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "orcid_id")
    private String orcidId;

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    
    @Fetch(FetchMode.JOIN)
    @JoinTable(
            name = "user_role",
            joinColumns = { @JoinColumn(name = "user_id") },
            inverseJoinColumns = { @JoinColumn(name = "role_id") }
    )
    private List<Role> roles;

    @Column(name = "internal_user")
    private Boolean dhpUser;

    @ManyToOne
    @JoinColumn(name = "dcc_id")
    private LkupDCC dcc;

    @PreUpdate
    public void setUpdatedAt() {
        this.updateAt = ZonedDateTime.now();
    }

    @PrePersist
    public void setCreatedAt() {
        this.createdAt = ZonedDateTime.now();
    }

    public User(UserRegistrationDTO regDTO, Institution institution, LookupStatus status, LookupResearcherLevel researcherLevel) {
        this.firstName = regDTO.getFirstName().trim();
        this.middleInitial = regDTO.getMiddleInitial();
        this.lastName = regDTO.getLastName().trim();
        this.email = regDTO.getEmail();
        this.institution = institution;
        this.researcherLevel = researcherLevel;
        this.acceptTerms = regDTO.getAcceptTerms();
        this.jobTitle = regDTO.getJobTitle();
        this.orcidId = regDTO.getOrcidId();
        this.status = status;
        this.dhpUser = false;
    }

    public void updateUser(UserDTO userDTO, Institution institution, List<Role> roles,
                           LookupStatus status, LookupResearcherLevel researcherLevel, LkupDCC dcc) {
        this.firstName = userDTO.getFirstName().trim();
        this.middleInitial = userDTO.getMiddleInitial();
        this.lastName = userDTO.getLastName().trim();
        this.email = userDTO.getEmail();
        this.institution = institution;
        this.jobTitle = userDTO.getJobTitle();
        this.status = status;
        this.roles = roles;
        this.dhpUser = userDTO.getDhpUser();
        this.researcherLevel = researcherLevel;
        this.dcc = dcc;
    }

    public void updateUser(String jobTitle, String orcidId, Institution institution, LookupResearcherLevel researcherLevel) {
        this.jobTitle = jobTitle;
        this.orcidId = orcidId;
        this.institution = institution;
        this.researcherLevel = researcherLevel;
    }
}
