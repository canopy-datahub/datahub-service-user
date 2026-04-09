package ex.org.project.userservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InstitutionDTO {
    private Integer id;
    private String name;
    private String status;
    private String type;
    private Boolean isForProfit;
    private String rorId;
    private String country;
    private String state;
    private String province;
}
