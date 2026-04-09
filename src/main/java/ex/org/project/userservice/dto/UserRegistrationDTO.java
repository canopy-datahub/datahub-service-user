package ex.org.project.userservice.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserRegistrationDTO {
    @NotBlank(message = "First Name must not be blank")
    private String firstName;
    @NotBlank(message = "Last Name must not be blank")
    private String lastName;
    private String middleInitial;
    @NotBlank(message = "Email Address must not be blank")
    private String email;
    private String orcidId;
    @NotBlank(message = "Job Title must not be blank")
    private String jobTitle;
    @NotBlank(message = "Institution must not be blank")
    private String institution;
    @NotBlank(message = "Researcher Level must not be blank")
    private String researcherLevel;
    @NotNull(message = "User must agree to terms")
    @AssertTrue(message = "User must agree to terms")
    private Boolean acceptTerms;
    @NotNull(message = "Referrer cannot be null")
    @Valid
    private List<ReferrerSelectionDTO> referrers;
}
