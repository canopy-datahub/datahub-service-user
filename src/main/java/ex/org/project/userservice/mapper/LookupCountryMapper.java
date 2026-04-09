package ex.org.project.userservice.mapper;

import ex.org.project.userservice.entity.LookupCountry;
import ex.org.project.userservice.dto.LookupCountryDTO;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LookupCountryMapper {
    LookupCountryDTO toCountryDto(LookupCountry lookupCountry);
}
