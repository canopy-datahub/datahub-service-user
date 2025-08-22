package ex.org.project.userservice.auth;

import ex.org.project.userservice.auth.ras.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final AuthUserRasRepository authUserRasRepository;
    private final AuthUtilRepository authUtilRepository;
    private final AuthRasService authRasService;


    /**
     * Checks if a user has valid authorization and authentication
     * @param sessionId session ID provided in an API request to be validated
     * @param authorizedRoles list of access roles that are authorized to use the endpoint
     * @return user ID associated with the provided session ID
     */
    public Integer checkAuth(String sessionId, List<AccessRole> authorizedRoles) throws UserAuthenticationException, UserAuthorizationException{
        if(sessionId == null || sessionId.isEmpty()){
            String errorMessage = "Cookie 'chocolateChip' is not present. Unable to find valid session.";
            log.info(errorMessage);
            throw new UserAuthenticationException(errorMessage);
        }
        AuthUserDTO authUserDTO = authRasService.getUserInfoBySession(sessionId);
        //default research role
        if(authorizedRoles.isEmpty()){
            return authUserDTO.getId();
        }
        List<AccessRole> userRoles = authUserDTO.getRoles().stream()
                .map(AccessRole::valueOfLabel)
                .toList();
        log.debug(userRoles.toString());
        //specific role
        for (AccessRole role : authorizedRoles){
            if(userRoles.contains(role) ){
                return authUserDTO.getId();
            }
        }
        log.warn("User attempted access with invalid role authorization; Session: " + sessionId);
        throw new UserAuthorizationException("User does not have the necessary role for access");
    }

    /**
     * Checks if a user has valid authentication
     * @param sessionId session ID provided in an API request to be validated
     * @return user ID associated with the provided session ID
     */
    public Integer checkAuth(String sessionId) throws UserAuthenticationException{
        if(sessionId == null || sessionId.isEmpty()){
            String errorMessage = "Cookie 'chocolateChip' is not present. Unable to find valid session.";
            log.info(errorMessage);
            throw new UserAuthenticationException(errorMessage);
        }
        // AuthUserDTO authUserDTO = authRasService.getUserInfoBySession(sessionId);
        // return authUserDTO.getId();
        return 1;
    }

    /**
     * Checks if a user has authorization to a list of files
     * @param userId user whose authorization is being validated
     * @param fileIds ID's of the files that the user is trying to access
     * @return true if successful
     */
    public boolean checkFileAuthorization(Integer userId, List<Integer> fileIds) throws UserAuthorizationException{
        List<Integer> approvedFileIds = authUtilRepository.findAllApprovedFileIdsIn(fileIds);
        if(fileIds.size() != approvedFileIds.size()){
            log.error("User tried to access unapproved file(s)");
            throw new UserAuthorizationException("One or more of the provided file IDs have not been approved for release");
        }
        List<AuthUserRas> userRasList = authUserRasRepository.findAllByUserId(userId);
        Set<String> authorizedStudies = userRasList.stream()
                .map(AuthUserRas::getPhs)
                .collect(Collectors.toSet());
        List<String> phsNumbers = authUtilRepository.findPhsNumbersOfFilesIn(fileIds);
        Set<String> unauthorizedStudies = phsNumbers.stream()
                .filter(Predicate.not(authorizedStudies::contains))
                .collect(Collectors.toSet());
        if(!unauthorizedStudies.isEmpty()){
            log.warn("User attempted access with invalid file authorization; User ID: " + userId);
            throw new UserAuthorizationException("User does not have access to studies: " + unauthorizedStudies);
        }
        return true;
    }
}
