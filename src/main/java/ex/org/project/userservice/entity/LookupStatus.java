package ex.org.project.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lkup_status")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LookupStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "usage")
    private String usage;

    @Column(name = "description")
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;
}
