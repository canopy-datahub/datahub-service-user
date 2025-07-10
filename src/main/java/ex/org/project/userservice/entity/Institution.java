package ex.org.project.userservice.entity;

import ex.org.project.userservice.dto.InstitutionDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "institution")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Institution {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;

	@Column(name = "name", nullable = false)
	private String name;

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "status_id")
	private LookupStatus status;

	@ManyToOne
	@JoinColumn(name = "institution_type_id", nullable = false)
	private LookupInstitutionType type;

	@Column(name = "is_for_profit")
	private Boolean isForProfit;

	@Column(name = "ror_id")
	private String rorId;

	@ManyToOne
	@JoinColumn(name = "country_id")
	private LookupCountry country;

	@ManyToOne
	@JoinColumn(name = "state_id")
	private LookupState state;

	@Column(name = "province_region")
	private String province;

	@Column(name = "created_by")
	private Integer createdBy;

	public Institution(InstitutionDTO dto, LookupStatus status, LookupInstitutionType type,
					   LookupCountry country, LookupState state) {
		this.name = dto.getName();
		this.status = status;
		this.type = type;
		this.isForProfit = dto.getIsForProfit();
		this.rorId = dto.getRorId();
		this.country = country;
		this.state = state;
		this.province = dto.getProvince();
	}

}
