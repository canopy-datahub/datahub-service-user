package ex.org.project.userservice.auth.ras;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
interface AuthUserRasMapper {

    @Mapping(source = "phs_id", target = "phs")
    @Mapping(source = "expiration_dt", target = "expirationDate")
    AuthUserRas toUserRasEntity(AuthRasDbGapPermissionDTO rasUserDbGapPermissionDTO);

    List<AuthUserRas> toDTOs(List<AuthRasDbGapPermissionDTO> rasUserDbGapPermissionDTOList);

}
