package com.codearena.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class Judge0Service {

    private static final int PYTHON3_LANGUAGE_ID = 71;

    @Value("${judge0.api.url:https://ce.judge0.com}")
    private String judge0Url;

    private final RestTemplate restTemplate = new RestTemplate();

    public String submitCode(String code, String stdin) {
        String url = judge0Url + "/submissions?base64_encoded=true&wait=false";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("language_id", PYTHON3_LANGUAGE_ID);
        body.put("source_code", Base64.getEncoder().encodeToString(code.getBytes()));
        if (stdin != null && !stdin.isEmpty()) {
            body.put("stdin", Base64.getEncoder().encodeToString(stdin.getBytes()));
        }
        body.put("cpu_time_limit", 5.0);
        body.put("memory_limit", 128000);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null)
            throw new RuntimeException("Empty response from Judge0");
        return (String) responseBody.get("token");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getResult(String token) {
        String url = judge0Url + "/submissions/" + token + "?base64_encoded=true";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        return response.getBody();
    }

    public String decodeBase64(String encoded) {
        if (encoded == null)
            return "";
        try {
            return new String(Base64.getDecoder().decode(encoded));
        } catch (Exception e) {
            return encoded;
        }
    }

    public String mapVerdict(int statusId) {
        return switch (statusId) {
            case 3 -> "Accepted";
            case 4 -> "Wrong Answer";
            case 5 -> "Time Limit Exceeded";
            case 6 -> "Compilation Error";
            case 7, 8, 9, 10, 11, 12 -> "Runtime Error";
            case 13 -> "Internal Error";
            case 14 -> "Execution Format Error";
            default -> "Unknown";
        };
    }
}