package ex.org.project.userservice.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import ex.org.project.userservice.dto.SupportRequestDTO;
import ex.org.project.userservice.entity.SupportRequest;

@Mapper(componentModel = "spring")
public interface SupportRequestMapper {

    @Mapping(source = "resolutionType.name", target = "resolutionType")
    @Mapping(source = "status.name", target = "status")
    @Mapping(source = "requestType.name", target = "requestType")
    @Mapping(source = "notes", target = "notes")
    SupportRequestDTO toSupportRequestDto(SupportRequest supportRequest);
    
    List<SupportRequestDTO> toDTOs(List<SupportRequest> supportRequests);
    
    //SupportRequest fromSupportRequestDto(SupportRequestDTO supportRequestDTO);
}

