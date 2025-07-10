package ex.org.project.userservice.dto;

import lombok.Data;

@Data
public class LookupSupportRequestTypeDTO {
    private Long id;
    private String name;
    private String description;
    private String displayOrder;
}
