package ex.org.project.userservice.mapper;

import ex.org.project.userservice.dto.LookupStateDTO;
import ex.org.project.userservice.entity.LookupState;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LookupStateMapper {
    LookupStateDTO toStateDto(LookupState lookupState);
}
