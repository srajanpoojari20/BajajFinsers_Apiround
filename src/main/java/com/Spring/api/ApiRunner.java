package com.Spring.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class ApiRunner implements CommandLineRunner {

    @Value("${app.name}")
    private String name;

    @Value("${app.regNo}")
    private String regNo;

    @Value("${app.email}")
    private String email;

    @Value("${app.generateUrl}")
    private String generateUrl;

    @Value("${app.submitUrl}")
    private String submitUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void run(String... args) throws Exception {
        System.out.println(" API Test");

        Map<String, String> body = new HashMap<>();
        body.put("name", name);
        body.put("regNo", regNo);
        body.put("email", email);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(generateUrl, request, String.class);

        System.out.println("Webhook Response: " + response.getBody());

        JsonNode json = mapper.readTree(response.getBody());
        String webhook = json.path("webhook").asText();
        String accessToken = json.path("accessToken").asText();

        System.out.println("Webhook: " + webhook);
        System.out.println("Access Token: " + accessToken);

        String finalQuery = "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, "
                + "COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT "
                + "FROM EMPLOYEE e1 "
                + "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID "
                + "LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT "
                + "AND e2.DOB > e1.DOB "
                + "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME "
                + "ORDER BY e1.EMP_ID DESC;";


        Map<String, String> queryBody = new HashMap<>();
        queryBody.put("finalQuery", finalQuery);

        HttpHeaders headers2 = new HttpHeaders();
        headers2.setContentType(MediaType.APPLICATION_JSON);
        headers2.set("Authorization", accessToken);

        HttpEntity<Map<String, String>> queryRequest = new HttpEntity<>(queryBody, headers2);
        ResponseEntity<String> queryResponse = restTemplate.postForEntity(submitUrl, queryRequest, String.class);

        System.out.println("Query Response: " + queryResponse.getBody());

    }
}
