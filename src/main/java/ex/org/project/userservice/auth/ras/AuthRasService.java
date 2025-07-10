package ex.org.project.userservice.auth.ras;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ex.org.project.userservice.auth.*;
import ex.org.project.userservice.exception.RasException;
import ex.org.project.userservice.exception.UserRegistrationFormException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthRasService {
	@Autowired
	RestTemplate restTemplate;

	@Value("${ras.url}")
	private String rasUrl;

	@Value("${ras.client.id}")
	private String rasClientId;

	@Value("${ras.client.secret}")
	private String rasClientSecret;

	@Value("${redirect.url}")
	private String redirectUrl;

	@Value("${ras.auth}")
	private String rasAuth;
	
	private final AuthRasTrackingRepository rasTrackingRepository;
	private final AuthUserRasMapper userRasMapper;
	private final AuthUserRasRepository userRasRepository;
	private final AuthUserLoginRepository userLoginRepository;
	private final AuthRasRegistrationMapper rasRegistrationMapper;
	private final AuthRasTrackingRepository authRasTrackingRepository;
	private final AuthUserMapper authUserMapper;
	private final AuthUserRepository authUserRepository;

	private static final String GRANT_TYPE = "grant_type";
	private static final String AUTHORIZATION_CODE = "authorization_code";
	private static final String CODE = "code";
	private static final String SCOPE = "scope";
	private static final String OPEN_ID = "openid";
	private static final String CLIENT_ID = "client_id";
	private static final String CLIENT_SECRET = "client_secret";
	private static final String REDIRECT_URI = "redirect_uri";
    @Autowired
    private AuthUserRasRepository authUserRasRepository;

	/**
	 * To get the RAS User info by sending the accessToken to the API
	 * @param accessToken
	 * @return RasUserDTO
	 */
	public AuthRasDTO getRasUserInfo(String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
		HttpEntity<?> request = new HttpEntity<>(payload, headers);
		String url = rasUrl + "/openid/connect/v1.1/userinfo";
		ResponseEntity<AuthRasDTO> responseEntity = restTemplate.exchange(url, HttpMethod.POST, request, AuthRasDTO.class);
		return responseEntity.getBody();
	}

	public void processRefreshToken(String sessionId) {
		Optional<AuthRasTracking> rasUserOpt = rasTrackingRepository.findBySessionId(sessionId);
		if(rasUserOpt.isEmpty()){
			String errorMessage = "Unable to find valid session: " + sessionId;
			log.info(errorMessage);
			throw new UserAuthenticationException(errorMessage);
		}
		AuthRasTracking authRasTracking = rasUserOpt.get();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
		payload.add(GRANT_TYPE, "refresh_token");
		payload.add(SCOPE, "openid profile email department");
		payload.add("refresh_token", authRasTracking.getRefreshToken());
		payload.add(CLIENT_ID, rasClientId);
		payload.add(CLIENT_SECRET, rasClientSecret);
		HttpEntity<?> request = new HttpEntity<>(payload, headers);

		String url = rasUrl + "/auth/oauth/v2/token";
		ResponseEntity<AuthRasToken> responseEntity = restTemplate.exchange(url, HttpMethod.POST, request, AuthRasToken.class);
		AuthRasToken rasToken = responseEntity.getBody();
		rasTrackingRepository.delete(authRasTracking);
		authRasTracking.setId(null);
		authRasTracking.setRefreshToken(rasToken.getRefresh_token());
		authRasTracking.setAccessToken(rasToken.getAccess_token());
		rasTrackingRepository.save(authRasTracking);
	}

	public AuthRasRegistrationDTO getRasRegistrationDetails(String sessionId) throws UserRegistrationFormException {
		AuthRasTracking rasTracking = rasTrackingRepository.findBySessionId(sessionId)
				.orElseThrow(() -> new UserRegistrationFormException("The provided sessionId does not exist"));
		return rasRegistrationMapper.toRasRegistrationDto(rasTracking);
	}

	/**
	 * To process Login info from RAS
	 * @param code from RAS used to get token
	 * @return UserDTO User object
	 */
	public AuthUserDTO processLogin(String code) {
		// ## Step 1: Log attempt and get RAS Token ##
		if(code == null) {
			return new AuthUserDTO(rasAuth);
		}
		String sessionID = UUID.randomUUID().toString();
		log.info("Session Id {} was created for the user.", sessionID);
		//this save means the user has been redirected to us and a session id has been created
		AuthRasTracking rasTrackingEntry = rasTrackingRepository.save(new AuthRasTracking(code, sessionID));
		AuthRasToken rasCredentialDTO = getRasToken(code);
		rasTrackingEntry.setAccessToken(rasCredentialDTO.getAccess_token());
		rasTrackingEntry.setRefreshToken(rasCredentialDTO.getRefresh_token());
		rasTrackingEntry.setIdToken(rasCredentialDTO.getId_token());

		String transactionId = getTransactionId(rasCredentialDTO.getAccess_token());
		rasTrackingEntry.setCorrelationId(transactionId);
		// this save means we were able to retrieve the RAS access/refresh tokens
		rasTrackingEntry = rasTrackingRepository.save(rasTrackingEntry);
		// ## Step 2: Get RAS User info using the RAS Access Token ##
		AuthRasDTO rasUserDTO = getRasUserInfo(rasCredentialDTO.getAccess_token());
		rasTrackingEntry.setFirstName(rasUserDTO.getFirst_name());
		rasTrackingEntry.setLastName(rasUserDTO.getLast_name());
		rasTrackingEntry.setEmail(rasUserDTO.getEmail());
		rasTrackingEntry.setInstitutionName(rasUserDTO.getDepartment());
		rasTrackingEntry.setPassport(rasUserDTO.getPassport_jwt_v11());
		rasTrackingRepository.save(rasTrackingEntry);

		// ## Step 3: Get User info ##
		AuthUserDTO userDTO;
		try {
			userDTO = getUserByEmail(rasUserDTO.getEmail());
			List<AuthRasDbGapPermissionDTO> dbGapPermissions = getRasDbGapPermissions(rasUserDTO.getPassport_jwt_v11());
			List<String> phsList = dbGapPermissions.stream().map(AuthRasDbGapPermissionDTO::getPhs_id).toList();
			if(phsList.isEmpty()){
				log.info("User with sessionId {} and emailId {} had no access", sessionID, rasUserDTO.getEmail());
			}else {
				log.info("User with sessionId {} and emailId {} has access to {}", sessionID, rasUserDTO.getEmail(), phsList);
			}
			List<AuthUserRas> userRasList = createUserRasList(dbGapPermissions, userDTO, transactionId, rasUserDTO.getPassport_jwt_v11());
			userRasRepository.saveAll(userRasList);
			userLoginRepository.save(new AuthUserLogin(userDTO.getId()));
		} catch (UserNotFoundException e) {
			log.error("User with email not found", e);
			userDTO = new AuthUserDTO();
		}
		userDTO.setRedirectUrl(((userDTO.getEmail() != null) ? "/postAuth?" : "/userRegistration?") + "sessionID=" + sessionID);
		return userDTO;
	}

	public String processLogout(String sessionId) {
		AuthRasTracking rasTracking = rasTrackingRepository.findBySessionId(sessionId)
				.orElseThrow(() -> new UserNotFoundException("The provided sessionId does not exist"));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
		payload.add("id_token", rasTracking.getIdToken());
		payload.add(CLIENT_ID, rasClientId);
		payload.add(CLIENT_SECRET, rasClientSecret);
		HttpEntity<?> request = new HttpEntity<>(payload, headers);
		try{
			String url = rasUrl + "/connect/session/logout";
			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
			//To remove entries from tables
			AuthUserDTO userInfo = getUserInfoBySession(sessionId);
			authUserRasRepository.deleteByUserId(userInfo.getId());
			rasTrackingRepository.delete(rasTracking);
			return responseEntity.getBody();
		}catch(Exception e) {
			throw new RasException("Logout failed", e);
		}
	}

	/**
	 * To get the RAS token from the oAuth token API
	 * @param code string
	 * @return AuthRasToken
	 */
	private AuthRasToken getRasToken(String code) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
		payload.add(GRANT_TYPE, AUTHORIZATION_CODE);
		payload.add(CODE, code);
		payload.add(SCOPE, "openid email profile ga4gh_passport_v1 department");
		payload.add(CLIENT_ID, rasClientId);
		payload.add(CLIENT_SECRET, rasClientSecret);
		payload.add(REDIRECT_URI, redirectUrl);

		HttpEntity<?> request = new HttpEntity<>(payload, headers);
		String url = rasUrl + "/auth/oauth/v2/token";
		ResponseEntity<AuthRasToken> responseEntity = restTemplate.exchange(url, HttpMethod.POST, request, AuthRasToken.class);
		return responseEntity.getBody();
	}

	private String getTransactionId(String accessToken) {
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		AuthRasDTO authRasDTO;
		String accessTokenString = decodedValue(accessToken);
		try {
			authRasDTO = mapper.readValue(accessTokenString, AuthRasDTO.class);
		}catch(JsonProcessingException e) {
			log.error("Failed to decode accessToken for txn id", e);
			return "";
		}
		return authRasDTO.getTxn();
	}

	private static String decodedValue(String decode) {
		return new String(Base64.decodeBase64(decode.split("\\.")[1]), StandardCharsets.UTF_8) ;
	}

	private List<AuthRasDbGapPermissionDTO> getRasDbGapPermissions(String passport) {
		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		AuthRasDTO authRasDTO;
		try{
			String jsonString = decodedValue(passport);
			authRasDTO = mapper.readValue(jsonString, AuthRasDTO.class);
			String ga4ghpassport = authRasDTO.getGa4gh_passport_v1()[0];
			String newValue = decodedValue(ga4ghpassport);
			authRasDTO = mapper.readValue(newValue, AuthRasDTO.class);

		}catch(JsonProcessingException e) {
			log.error("Failed to decode RAS dbGap permissions", e);
			return Collections.emptyList();
		}
		return authRasDTO.getRas_dbgap_permissions();
	}

	private List<AuthUserRas> createUserRasList(List<AuthRasDbGapPermissionDTO> dbGapPermissionList, AuthUserDTO userDTO, String transactionId, String passport) {
		dbGapPermissionList.forEach(r -> r.setExpiration_dt(changeFormat(r.getExpiration())));
		Integer userId = userDTO.getId();
		List<AuthUserRas> dtos = userRasMapper.toDTOs(dbGapPermissionList);
		dtos.forEach(k -> {
			k.setUserId(userId);
			k.setTransactionId(transactionId);
			k.setPassport(passport);
		});
		return dtos;
	}

	/**
	 * To change the datetime format
	 * @param date
	 * @return ZonedDateTime
	 */
	private ZonedDateTime changeFormat(long date) {
		return ZonedDateTime.ofInstant(
				Instant.ofEpochSecond(date), ZoneId.systemDefault());
	}


	/**
	 * To validate RAS passport
	 *
	 * @param passport
	 * @return
	 */
	private String validatePassport(String passport){
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		try{
			String url = rasUrl + "/passport/validate?visa=" + passport;
			ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
			return responseEntity.getBody();
		} catch (Exception e){
			throw new RasException("Failed to validate passport", e);
		}
	}

	/**
	 * To ping RAS Passport Service
	 */
	@Transactional
	public void pingRasPassportService() {
		List<AuthUserRas> userRasList = authUserRasRepository.findDistictUsers();
		if(!CollectionUtils.isEmpty(userRasList)){
			List<AuthUserRas> userWithInvalidPassportList = userRasList.stream()
					.filter(p -> !validatePassport(p.getPassport()).equalsIgnoreCase("valid")).toList();

			userWithInvalidPassportList.forEach(u -> authUserRasRepository.deleteByUserId(u.getId()));
		}
	}

	/**
	 * Finds a user by session ID
	 * @param sessionId session ID provided in an API request to be validated
	 * @return object containing user data needed for validation
	 */
	public AuthUserDTO getUserInfoBySession(String sessionId) {
		if(sessionId == null || sessionId.isEmpty()){
			String errorMessage = "Cookie 'chocolateChip' is not present. Unable to find valid session.";
			log.info(errorMessage);
			throw new UserAuthenticationException(errorMessage);
		}
		Optional<AuthRasTracking> rasUserOpt = authRasTrackingRepository.findBySessionId(sessionId);
		if(rasUserOpt.isEmpty()) {
			String errorMessage = "Unable to find valid session: " + sessionId;
			log.info(errorMessage);
			throw new UserAuthenticationException(errorMessage);
		}

		try {
			getRasUserInfo(rasUserOpt.get().getAccessToken());
		}
		catch(HttpClientErrorException ex) {
			try {
				processRefreshToken(sessionId);
			}catch (HttpClientErrorException e) {
				String errorMessage = "Unable to find valid session: " + sessionId;
				log.info(errorMessage);
				throw new UserAuthenticationException(errorMessage);
			}
		}
		AuthRasTracking rasUser = rasUserOpt.get();

		if (rasUser.getEmail() != null) {
			Optional<AuthUser> userOpt = authUserRepository.findByEmail(rasUser.getEmail());
			if(userOpt.isEmpty()){
				String errorMessage = "Unable to find user with email address: " + rasUser.getEmail();
				log.error(errorMessage);
				throw new UserNotFoundException(errorMessage);
			}
			return authUserMapper.toAuthUserDto(userOpt.get(), sessionId);
		} else {
			String errorMessage = "Unable to find user email address for session: " + sessionId;
			log.error(errorMessage);
			throw new UserNotFoundException(errorMessage);
		}
	}

	public AuthUserDTO getUserByEmail(String email) {
		AuthUser user = authUserRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("Unable to find user with provided email"));
		return authUserMapper.toAuthUserDto(user);
	}

}


