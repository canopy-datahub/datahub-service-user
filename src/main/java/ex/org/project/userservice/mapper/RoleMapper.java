package ex.org.project.userservice.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import ex.org.project.userservice.dto.RoleDTO;
import ex.org.project.userservice.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleDTO toRoleDto(Role role);
    
    List<RoleDTO> toDTOs(List<Role> roles);
    
}

