package ex.org.project.userservice.mapper;

import ex.org.project.userservice.dto.UserDTO;
import ex.org.project.userservice.entity.Role;
import ex.org.project.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

	@Mapping(target = "institution", source = "institution.name")
	@Mapping(target = "roles", source = "user.roles", qualifiedByName = "extractRoles")
	@Mapping(target = "jobTitle", source = "jobTitle")
	@Mapping(target = "status", source = "status.name")
	@Mapping(target = "researcherLevel", source = "user.researcherLevel.name")
	@Mapping(target = "center", source = "user.center.name")
	UserDTO toUserDto(User user);

	@Mapping(target = "institution", source = "user.institution.name")
	@Mapping(target = "roles", source = "user.roles", qualifiedByName = "extractRoles")
	@Mapping(target = "jobTitle", source = "user.jobTitle")
	@Mapping(target = "status", source = "user.status.name")
	@Mapping(target = "sessionID", source = "session")
	@Mapping(target = "researcherLevel", source = "user.researcherLevel.name")
	@Mapping(target = "center", source = "user.center.name")
	UserDTO toUserDto(User user, String session);

	List<UserDTO> toUserDTOs(List<User> users);

    @Named("extractRoles")
	static List<String> extractRoles(List<Role> roles){
		return roles.stream().map(Role::getName).toList();
	}
}
