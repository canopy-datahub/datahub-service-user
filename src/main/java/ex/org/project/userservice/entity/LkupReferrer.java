package ex.org.project.userservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "lkup_referrer")
public class LkupReferrer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 256)
    @NotNull
    @Column(name = "name")
    private String name;

    @Size(max = 256)
    @Column(name = "specify_prompt")
    private String specifyPrompt;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @Column(name = "display_order")
    private Integer displayOrder;

}