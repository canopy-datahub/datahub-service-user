package ex.org.project.userservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReferrerSelectionDTO {
    @NotNull(message = "Referrer cannot be null")
    @Min(1)
    private Integer referrerId;
    @NotBlank(message = "Please specify the referrer")
    private String referrerSpecify;
}
