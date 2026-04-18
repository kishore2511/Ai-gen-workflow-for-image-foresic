package com.defense.forensic.service;

import com.defense.forensic.dto.PredictionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MlClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String predictUrl;

    public MlClient(@Value("${app.ml.predict-url:http://localhost:8000/predict}") String predictUrl) {
        this.predictUrl = predictUrl;
    }

    public PredictionResponse predict(MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<PredictionResponse> response = restTemplate.exchange(
                predictUrl,
                HttpMethod.POST,
                request,
                PredictionResponse.class
        );
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("ML service unavailable");
        }
        return response.getBody();
    }
}
