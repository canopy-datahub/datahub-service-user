package ex.org.project.userservice.auth.ras;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthRasRegistrationMapper {

    AuthRasRegistrationDTO toRasRegistrationDto(AuthRasTracking rasTracking);

}
