package ex.org.project.userservice.auth.ras;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ex.org.project.userservice.service.UserService;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class AuthRasServiceTest {

    @Mock
    AuthRasTrackingRepository rasTrackingRepository;
    @Mock
    UserService userService;
    @Mock
    RestTemplate restTemplate;
    String passport;

    @BeforeEach
    void setUp(){
        //passport = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ik5JSC1BVVRILUdMT0JBTC1TSUdOLVNURy0wMk5PVjIxIn0.ew0KInN1YiI6IjZ3TzVpOWl2QThGSHdXMUdrTWV5cy1DNHFkRUJBVHVpSXV5OHl3cjJxZmciLA0KImp0aSI6IjdhYzQwYWE0LWI0YjQtNDdiOC05YWNhLTc4ZjA5NTNhNmRiNiIsDQoic2NvcGUiOiJvcGVuaWQgZ2E0Z2hfcGFzc3BvcnRfdjEiLA0KInR4biI6ImY2NjY3MDFiOTZhMTFmOWEuOTRmZjQ3N2U4MTZiMTlmYSIsDQoiaXNzIjogImh0dHBzOi8vc3Rzc3RnLm5paC5nb3YiLCAKImlhdCI6IDE2OTkyOTY2NjksCiJleHAiOiAxNjk5MzM5ODY5LAoiZ2E0Z2hfcGFzc3BvcnRfdjEiIDogWyJleUowZVhBaU9pSktWMVFpTENKaGJHY2lPaUpTVXpJMU5pSXNJbXRwWkNJNklrNUpTQzFCVlZSSUxVZE1UMEpCVEMxVFNVZE9MVk5VUnkwd01rNVBWakl4SW4wLmV3MEtJQ0FpYVhOeklqb2dJbWgwZEhCek9pOHZjM1J6YzNSbkxtNXBhQzVuYjNZaUxBMEtJQ0FpYzNWaUlqb2dJalozVHpWcE9XbDJRVGhHU0hkWE1VZHJUV1Y1Y3kxRE5IRmtSVUpCVkhWcFNYVjVPSGwzY2pKeFptY2lMQ0FOQ2lBZ0ltbGhkQ0k2SURFMk9Ua3lPVFkyTmprc0RRb2dJQ0psZUhBaU9pQXhOams1TXpNNU9EWTVMQTBLSUNBaWMyTnZjR1VpT2lBaWIzQmxibWxrSUdkaE5HZG9YM0JoYzNOd2IzSjBYM1l4SWl3TkNpQWdJbXAwYVNJNklDSTBaR0ptWkdWaFpDMWlaRGhtTFRSbFpqY3RPVGc1TXkweFpESTROV1ExTjJaak9UY2lMQTBLSUNBaWRIaHVJam9nSW1ZMk5qWTNNREZpT1RaaE1URm1PV0V1T1RSbVpqUTNOMlU0TVRaaU1UbG1ZU0lzRFFvZ0lDSm5ZVFJuYUY5MmFYTmhYM1l4SWpvZ2V5QU5DaUFnSUNBZ0luUjVjR1VpT2lBaWFIUjBjSE02THk5eVlYTXVibWxvTG1kdmRpOTJhWE5oY3k5Mk1TNHhJaXdnRFFvZ0lDQWdJQ0poYzNObGNuUmxaQ0k2SURFMk9Ua3lPVFkyTmprc0RRb2dJQ0FnSUNKMllXeDFaU0k2SUNKb2RIUndjem92TDNOMGMzTjBaeTV1YVdndVoyOTJMM0JoYzNOd2IzSjBMMlJpWjJGd0wzWXhMakVpTEEwS0lDQWdJQ0FpYzI5MWNtTmxJam9nSW1oMGRIQnpPaTh2Ym1OaWFTNXViRzB1Ym1sb0xtZHZkaTluWVhBaUxBMEtJQ0FnSUNBaVlua2lPaUFpWkdGakluMHNEUW9nSUNBZ0lDSnlZWE5mWkdKbllYQmZjR1Z5YldsemMybHZibk1pT2lCYkRRb2dJQ0FnSUNBZ0lDQU5DaUFnSUNBZ1hTQU5DbjAuUXRBc1I5NE5FM2ZycHpfLWczVkdwRjZLOGNTTTUwYjZhb3RxSVpqTmcxX3BoMUUyZXQ1TFJWYnhJWGNpZkdaQ2V2VXo0M1lSckZ1U3M4ZXVmRFNGNUtQTExEWFRKVHFyR1V5cEF6Y2QzZk9MLVRYUlFmX2tQOWF6SFd3MVloOEt5ZVRwd0twd21RNlU0TkUydzdYVms4UHVuLXlaLUluNzA2SEd1eXVNa2xSRWhJaEFLb3kwekJLYU9GS3ZlYXozZ1hiUTM5b3U0YW1CZDRqYnlBLVhvYmNqY1pTbWFGY2luRTFwNW55SVRLX3FzQ0ZKcGJ6YXdjMWluZ19aU1dVakRTU3RWRDZyZ2RFTW9WazBTYWJtakFkQTV3WVoxWVZDSzdhRjQyRGdZRjlkZS1qMVZ6eVUtSlN5MmxJQ1d1bUJvVk92dFFjMDZ5UllrUGMxOGxLRER3Il0NCn0.JQVUjpoLf56rmVW3Wz4NkpZmLOm5kEcaS016B0ppFuTBmBxyK_BfxZRXjE_4gwPSkENI21M27mVNynSXa6hnTNWs3v857IkbzO7pa3UZsdAbmP4KGrbof-WpR4aakADFqLiS2eklP98YYpDnxXzwoERQ1ECHXBZg7cq2yYnCOBcx-MzJJOWhejbOFItWxPaCoBBfJSB3SDfl3L5Up-Dp8jw8b8GLGMUtYKQtqyVbPh47j2isbWfa6O628IXY7piI8HwHsp1Wk_Nmml1DY2UoZaIWpwlFqv_Nfi5vN4ILyGME99hyjj1ox3heCNADy47YUwqGSIDpGoOVVJ0COi1gpg";
        passport = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6Ik5JSC1BVVRILUdMT0JBTC1TSUdOLVNURy0wMk5PVjIxIn0.ew0KInN1YiI6IjZ3TzVpOWl2QThGSHdXMUdrTWV5cy1DNHFkRUJBVHVpSXV5OHl3cjJxZmciLA0KImp0aSI6ImJhMjA3ZmM0LTQxMzgtNDQ1MC05NTk1LTNlOTE5MjExMzRlYSIsDQoic2NvcGUiOiJvcGVuaWQgZ2E0Z2hfcGFzc3BvcnRfdjEiLA0KInR4biI6IjQ5NGMxZDI0OGMwOGU5YzEuOTRmZjQ3N2U4MThhMzAyMyIsDQoiaXNzIjogImh0dHBzOi8vc3Rzc3RnLm5paC5nb3YiLCAKImlhdCI6IDE2OTk5MDM1NTgsCiJleHAiOiAxNjk5OTQ2NzU4LAoiZ2E0Z2hfcGFzc3BvcnRfdjEiIDogWyJleUowZVhBaU9pSktWMVFpTENKaGJHY2lPaUpTVXpJMU5pSXNJbXRwWkNJNklrNUpTQzFCVlZSSUxVZE1UMEpCVEMxVFNVZE9MVk5VUnkwd01rNVBWakl4SW4wLmV3MEtJQ0FpYVhOeklqb2dJbWgwZEhCek9pOHZjM1J6YzNSbkxtNXBhQzVuYjNZaUxBMEtJQ0FpYzNWaUlqb2dJalozVHpWcE9XbDJRVGhHU0hkWE1VZHJUV1Y1Y3kxRE5IRmtSVUpCVkhWcFNYVjVPSGwzY2pKeFptY2lMQ0FOQ2lBZ0ltbGhkQ0k2SURFMk9UazVNRE0xTlRnc0RRb2dJQ0psZUhBaU9pQXhOams1T1RRMk56VTRMQTBLSUNBaWMyTnZjR1VpT2lBaWIzQmxibWxrSUdkaE5HZG9YM0JoYzNOd2IzSjBYM1l4SWl3TkNpQWdJbXAwYVNJNklDSTNaRFpoTmpkaE9TMDVaVFV3TFRReE9ETXRPR1kyTlMxa1kySTJZMkUwTW1VNE4yRWlMQTBLSUNBaWRIaHVJam9nSWpRNU5HTXhaREkwT0dNd09HVTVZekV1T1RSbVpqUTNOMlU0TVRoaE16QXlNeUlzRFFvZ0lDSm5ZVFJuYUY5MmFYTmhYM1l4SWpvZ2V5QU5DaUFnSUNBZ0luUjVjR1VpT2lBaWFIUjBjSE02THk5eVlYTXVibWxvTG1kdmRpOTJhWE5oY3k5Mk1TNHhJaXdnRFFvZ0lDQWdJQ0poYzNObGNuUmxaQ0k2SURFMk9UazVNRE0xTlRnc0RRb2dJQ0FnSUNKMllXeDFaU0k2SUNKb2RIUndjem92TDNOMGMzTjBaeTV1YVdndVoyOTJMM0JoYzNOd2IzSjBMMlJpWjJGd0wzWXhMakVpTEEwS0lDQWdJQ0FpYzI5MWNtTmxJam9nSW1oMGRIQnpPaTh2Ym1OaWFTNXViRzB1Ym1sb0xtZHZkaTluWVhBaUxBMEtJQ0FnSUNBaVlua2lPaUFpWkdGakluMHNEUW9nSUNBZ0lDSnlZWE5mWkdKbllYQmZjR1Z5YldsemMybHZibk1pT2lCYkRRb2dJQ0FnSUNBZ0lDQU5DaUFnSUNBZ1hTQU5DbjAuZlV5Tkg0R1hYZy1TMDZyVDNfOUlIRHQtNFBYaWtvT3RoMDdXbnBuVkdPZTZCWGI0X1doTFRjazJLVUFiT1JkbGIzbjU4bkVCQlktUHFiOFRLMmV6TlR4cGQ0VmdPeUtOalAwczZ3cFo4ajFNdHlFZFR4a3ZncVV4U0hqQ3MyRDViU2ZWX25GZF8ycVg4dHJUVzl2YlJpaEx0eEFqcmI1VnBSeXhjdUxva1M2dHdUUGFlQmMwU0ZwWWxBSXVpZHRyUUFBck1hMEZrT1NXb2xfRmVVMExESGdZZDV3a1d1UHpLT241dzZ6SXhRWjFkbnFJN1RLOGZYSFo1bjkzcmZnektGZjFhV1pVb0Y5VnJiSHNzRXV3YTZabGVRMVdWR1VGbHdOMkRIRzU0S3RyVzIwemtDTHVLQ0VySFVHMTJ1LTZBZVhmeU53VGxYSjNtTEZhWWZ3R3NnIl0NCn0.kjQfCOOv51zdjMqFBkIt5mACI5r3-wODNBTPI3M4XKvLbZUq046nNPl0g7JEriQh6KUIRFgz_GH51v7bCnKNhzxvxG-Vi7bs8Qe5aAyQnq5lDRjQUYX05jrkfxASpY1JYAcvizrPR20Lj0Za0oIQctNMSFFUoyIvvy5vudFfjpEEEemVmmadyJrG4TbU3cRlBTftrP__vXrfsqhmUBHjI2qaFbtwhbZcXZoLyEjkToKv6F8mnObgvLJEkutOdUXYMxWXCt0G_Z2SBMFmgvkZWt7O_4woCVyXN160GCLpYbYo0xEwxH52Pb6gVTZyesugmZrNIGNhUMHHXANKpEEZ-w";
    }

    @Test
    void decode() throws JSONException{
        String jsonString = decodedValue(passport);
        System.out.println(jsonString);
        JSONArray decode = getJsonArray(jsonString, "ga4gh_passport_v1");
        String jsonString1 = decodedValue(decode.toString());
        JSONArray decode1 = getJsonArray(jsonString1, "ras_dbgap_permissions");
        System.out.println(decode1);

    }
    @Test
    void getDbGapPermissions() {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);;
        List<AuthRasDbGapPermissionDTO> dbGapPermissionList;
        try {
            dbGapPermissionList  = Arrays.asList(mapper.readValue(dbGapJsonString(), AuthRasDbGapPermissionDTO[].class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        assertEquals(2, dbGapPermissionList.size());
    }

    @Test
    void redirectUrl() {
        String email = "sam@nih.com";
        String sessionId = "423loj08545";
        String redirectUrl = ((email != null) ? "/postAuth?" : "/userRegister?")
                            + "sessionID=" + sessionId;

        assertEquals("/postAuth?sessionID=423loj08545", redirectUrl);
    }

    @Test
    void zonedTime() {
        long dateTime = 1701454601;//1701370223
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(
                Instant.ofEpochSecond(dateTime), ZoneId.systemDefault());
        System.out.println(zonedDateTime);

        ZonedDateTime zonedDateTime1 = ZonedDateTime.now();
        Timestamp timestamp = Timestamp.from(zonedDateTime1.toInstant());
        System.out.println(timestamp);
    }

    private String dbGapJsonString() {
        return "[ \n" +
                "    { \n" +
                "      \"consent_name\": \"General Research Use\", \n" +
                "      \"phs_id\": \"phs000021\", \n" +
                "      \"version\": \"v3\", \n" +
                "      \"participant_set\": \"p2\", \n" +
                "      \"consent_group\": \"c1\", \n" +
                "      \"role\": \"pi\", \n" +
                "      \"expiration\": 1617599965\n" +
                "    }, \n" +
                "    { \n" +
                "      \"consent_name\": \"General Research Use\", \n" +
                "      \"phs_id\": \"phs000021\", \n" +
                "      \"version\": \"v3\", \n" +
                "      \"participant_set\": \"p2\", \n" +
                "      \"consent_group\": \"c1\", \n" +
                "      \"role\": \"pi\", \n" +
                "      \"expiration\": 1617599965 \n" +
                "    } \n" +
                "  ] \n";
    }

    private static JSONArray getJsonArray(String jsonString, String passport) throws JSONException {
        JSONObject obj = new JSONObject(jsonString);
        return obj.getJSONArray(passport);
    }

    private static String decodedValue(String decode) {
        return new String(Base64.decodeBase64(decode.split("\\.")[1]), StandardCharsets.UTF_8) ;
    }

}