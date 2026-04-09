package ex.org.project.userservice.mapper;

import ex.org.project.userservice.dto.InstitutionDTO;
import ex.org.project.userservice.entity.Institution;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InstitutionMapper {

    @Mapping(target = "status", source = "status.name")
    @Mapping(target = "type", source = "type.name")
    @Mapping(target = "country", source = "country.name")
    @Mapping(target = "state", source = "state.name")
    @Mapping(target = "province", source = "province")
    InstitutionDTO toInstitutionDto(Institution institution);

    List<InstitutionDTO> toInstitutionDTOs(List<Institution> institutions);

}
