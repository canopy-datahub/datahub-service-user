package ex.org.project.userservice.mapper;

import ex.org.project.userservice.dto.LkupReferrerDTO;
import ex.org.project.userservice.dto.UserRegistrationDTO;
import ex.org.project.userservice.entity.Institution;
import ex.org.project.userservice.entity.LkupReferrer;
import ex.org.project.userservice.entity.LookupResearcherLevel;
import ex.org.project.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {InstitutionMapper.class})
public interface UserRegistrationMapper {

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.middleInitial", target = "middleInitial")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.orcidId", target = "orcidId")
    @Mapping(source = "user.jobTitle", target = "jobTitle")
    @Mapping(source = "user.acceptTerms", target = "acceptTerms")
    default String map(Institution institution) {
        return institution != null ? institution.getName() : null;
    }
    default String map(LookupResearcherLevel researcherLevel) {
        return researcherLevel != null ? researcherLevel.getName() : null;
    }
    UserRegistrationDTO toUserRegistrationtDto(User user);

    LkupReferrerDTO mapReferrer(LkupReferrer referrer);

    List<LkupReferrerDTO> mapReferrerTypes(List<LkupReferrer> referrers);

}
