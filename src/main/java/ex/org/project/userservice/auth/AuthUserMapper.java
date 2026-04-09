package ex.org.project.userservice.auth;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthUserMapper {

    @Mapping(target = "roles", source = "authUser.roles", qualifiedByName = "extractRoles")
    @Mapping(target = "status", source = "authUser.status", qualifiedByName = "extractStatus")
    AuthUserDTO toAuthUserDto(AuthUser authUser);

    @Mapping(target = "roles", source = "authUser.roles", qualifiedByName = "extractRoles")
    @Mapping(target = "status", source = "authUser.status", qualifiedByName = "extractStatus")
    AuthUserDTO toAuthUserDto(AuthUser authUser, String session);

    @Named("extractRoles")
    static List<String> extractRoles(List<AuthRole> authRoles){
        return authRoles.stream().map(AuthRole::getName).toList();
    }

    @Named("extractStatus")
    static String extractStatus(AuthLookupStatus status){
        return status.getName();
    }

}