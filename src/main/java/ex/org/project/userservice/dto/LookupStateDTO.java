package ex.org.project.userservice.dto;

import lombok.Data;

@Data
public class LookupStateDTO {
    private Integer id;
    private String name;
    private String abbreviation;
    private Integer displayOrder;
}
