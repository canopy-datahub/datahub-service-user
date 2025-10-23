package ex.org.project.userservice.mapper;

import ex.org.project.userservice.dto.LookupCountryDTO;
import ex.org.project.userservice.entity.LookupCountry;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LookupCountryMapper {
    LookupCountryDTO toCountryDto(LookupCountry lookupCountry);
}
