package ex.org.project.userservice.controller;

import java.net.URI;
import java.net.URISyntaxException;

import ex.org.project.userservice.auth.ras.AuthRasRegistrationDTO;
import ex.org.project.userservice.auth.ras.AuthRasService;
import ex.org.project.userservice.auth.AuthUserDTO;
import ex.org.project.userservice.util.RequestValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RasController {

    private final AuthRasService rasService;

    @GetMapping("/ras")
    public ResponseEntity<Void> rasLogin(@RequestParam(required = false) String code) throws URISyntaxException {
        RequestValidator.validateStringRequestParam(code);
        AuthUserDTO userDTO = rasService.processLogin(code);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(new URI(userDTO.getRedirectUrl()));
        return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
    }
    
    @GetMapping("/getRegistrationDetails")
    public ResponseEntity<AuthRasRegistrationDTO> getRegistrationDetailsFromRAS(@RequestParam(required = false) String sessionId){
        RequestValidator.validateStringRequestParam(sessionId);
    	AuthRasRegistrationDTO rasRegistrationDto = rasService.getRasRegistrationDetails(sessionId);
    	return ResponseEntity.ok(rasRegistrationDto); 
    }

    @PostMapping("/logout")
    public ResponseEntity<String> rasLogout(@CookieValue(value="chocolateChip", required = false) String sessionId) {
        String value = rasService.processLogout(sessionId);
        return ResponseEntity.ok(value);
    }

    @PostMapping("/refresh/token")
    public ResponseEntity<Void> rasRefreshToken(@CookieValue(value="chocolateChip", required = false) String sessionId) {
        rasService.processRefreshToken(sessionId);
        return ResponseEntity.ok().build();
    }
}
