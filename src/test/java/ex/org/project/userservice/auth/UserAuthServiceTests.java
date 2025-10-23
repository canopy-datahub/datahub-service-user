package ex.org.project.userservice.auth;

import ex.org.project.datahub.auth.core.FileAuthorizationService;
import ex.org.project.datahub.auth.exception.UserAuthorizationException;
import ex.org.project.datahub.auth.model.AuthUserRas;
import ex.org.project.datahub.auth.repository.AuthUserRasRepository;
import ex.org.project.datahub.auth.repository.AuthUtilRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTests {

    @Mock
    private AuthUserRasRepository authUserRasRepository;

    @Mock
    private AuthUtilRepository authUtilRepository;

    private FileAuthorizationService fileAuthorizationService;

    @BeforeEach
    public void setup() {
        fileAuthorizationService = new FileAuthorizationService(authUserRasRepository, authUtilRepository);
    }

    private List<AuthUserRas> getAuthUserRasList(){
        AuthUserRas userRas1 = new AuthUserRas(10, "phs001111", 1);
        AuthUserRas userRas2 = new AuthUserRas(10, "phs001112", 1);
        AuthUserRas userRas3 = new AuthUserRas(10, "phs001113", 1);
        return List.of(userRas1, userRas2, userRas3);
    }

    @Test
    void testCheckFileAuthorization_happyPath(){
        List<AuthUserRas> userRasMock = getAuthUserRasList();
        List<String> phsNumbersMock = List.of("phs001111", "phs001112", "phs001113");
        Integer userId = 1;
        List<Integer> dataFileIds = List.of(1, 2, 3, 4, 5);

        when(authUtilRepository.findAllApprovedFileIdsIn(dataFileIds))
                .thenReturn(dataFileIds);
        when(authUserRasRepository.findAllByUserId(userId))
                .thenReturn(userRasMock);
        when(authUtilRepository.findPhsNumbersOfFilesIn(anyList()))
                .thenReturn(phsNumbersMock);

        boolean response = fileAuthorizationService.checkFileAuthorization(userId, dataFileIds);

        assertTrue(response);
    }

    @Test
    void testCheckFileAuthorization_partialStudyAuthorization(){
        List<AuthUserRas> userRasMock = List.of(new AuthUserRas(10, "phs001111", 1));
        List<String> phsNumbersMock = List.of("phs001111", "phs001112", "phs001113");
        Integer userId = 1;
        List<Integer> dataFileIds = List.of(1, 2, 3, 4, 5);

        when(authUtilRepository.findAllApprovedFileIdsIn(dataFileIds))
                .thenReturn(dataFileIds);
        when(authUserRasRepository.findAllByUserId(userId))
                .thenReturn(userRasMock);
        when(authUtilRepository.findPhsNumbersOfFilesIn(anyList()))
                .thenReturn(phsNumbersMock);

        assertThrows(UserAuthorizationException.class,
                () -> fileAuthorizationService.checkFileAuthorization(userId, dataFileIds)
        );
    }

    @Test
    void testCheckFileAuthorization_noStudyAuthorization(){
        List<AuthUserRas> userRasMock = new ArrayList<>();
        List<String> phsNumbersMock = List.of("phs001111", "phs001112", "phs001113");
        Integer userId = 1;
        List<Integer> dataFileIds = List.of(1, 2, 3, 4, 5);

        when(authUtilRepository.findAllApprovedFileIdsIn(dataFileIds))
                .thenReturn(dataFileIds);
        when(authUserRasRepository.findAllByUserId(userId))
                .thenReturn(userRasMock);
        when(authUtilRepository.findPhsNumbersOfFilesIn(anyList()))
                .thenReturn(phsNumbersMock);

        assertThrows(UserAuthorizationException.class,
                () -> fileAuthorizationService.checkFileAuthorization(userId, dataFileIds)
        );
    }

    @Test
    void testCheckFileAuthorization_allUnapprovedFiles(){
        Integer userId = 1;
        List<Integer> dataFileIds = List.of(1, 2, 3, 4, 5);

        when(authUtilRepository.findAllApprovedFileIdsIn(dataFileIds))
                .thenReturn(new ArrayList<>());

        assertThrows(UserAuthorizationException.class,
                () -> fileAuthorizationService.checkFileAuthorization(userId, dataFileIds)
        );
    }

    @Test
    void testCheckFileAuthorization_someUnapprovedFiles(){
        Integer userId = 1;
        List<Integer> dataFileIds = List.of(1, 2, 3, 4, 5);

        when(authUtilRepository.findAllApprovedFileIdsIn(dataFileIds))
                .thenReturn(List.of(1, 2, 3, 4));

        assertThrows(UserAuthorizationException.class,
                () -> fileAuthorizationService.checkFileAuthorization(userId, dataFileIds)
        );
    }
}
