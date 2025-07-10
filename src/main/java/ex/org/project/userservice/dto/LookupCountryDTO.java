package ex.org.project.userservice.dto;
import lombok.Data;

@Data
public class LookupCountryDTO {
    private Integer id;
    private String name;
    private Integer displayOrder;
}
