package ex.org.project.userservice.mapper;

import ex.org.project.userservice.dto.RoleDTO;
import ex.org.project.userservice.entity.Role;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleDTO toRoleDto(Role role);

    List<RoleDTO> toDTOs(List<Role> roles);

}

