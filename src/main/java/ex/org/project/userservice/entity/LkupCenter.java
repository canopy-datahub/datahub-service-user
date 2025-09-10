package ex.org.project.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "lkup_dcc")
@Data
public class LkupDCC {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

}