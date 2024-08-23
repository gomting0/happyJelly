package com.ex.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
// @RequiredArgsConstructor는 final 필드에 대한 생성자를 자동으로 생성
@RequiredArgsConstructor
public class KakaoApiService {
    // 로깅을 위한 Logger 객체를 생성합니다.
    private static final Logger logger = LoggerFactory.getLogger(KakaoApiService.class);
    
    // RestTemplate은 HTTP 요청을 보내는 데 사용됩니다.
    private final RestTemplate restTemplate;
   
    // @Value application.properties 파일에서 값을 주입받음
    @Value("${kakao.api.javascript.key}")
    private String kakaoJavascriptKey;

    @Value("${kakao.api.rest.key}")
    private String kakaoRestApiKey;
    
    // 주소를 좌표로 변환하는 메서드
    public Map<String, Double> getCoordinatesFromAddress(String postCode, String address, String address2) {
        // Kakao API 엔드포인트 URL을 생성
        URI uri = UriComponentsBuilder.fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
            .queryParam("query", address)
            .build()
            .encode()
            .toUri();
        
        // HTTP 요청 헤더를 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        try {
            // API 요청을 로깅
            logger.info("Sending request to Kakao API: URI={}, Address={}", uri, address);
            
            // API 요청을 보내고 응답을 받음
            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);
            
            // API 응답을 로깅
            logger.info("Received response from Kakao API: StatusCode={}", response.getStatusCode());
            
            Map<String, Object> body = response.getBody();
            
            // API 응답을 파싱하여 좌표를 추출
            if (body != null && body.containsKey("documents")) {
                List<Map<String, Object>> documents = (List<Map<String, Object>>) body.get("documents");
                if (!documents.isEmpty()) {
                    Map<String, Object> firstResult = documents.get(0);
                    Map<String, Object> address1 = (Map<String, Object>) firstResult.get("address");
                    Double longitude = Double.parseDouble((String) firstResult.get("x"));
                    Double latitude = Double.parseDouble((String) firstResult.get("y"));
                    
                    // 좌표를 8자리 소수점으로 반올림
                    BigDecimal latitudeBD = BigDecimal.valueOf(latitude).setScale(8, RoundingMode.HALF_UP);
                    BigDecimal longitudeBD = BigDecimal.valueOf(longitude).setScale(8, RoundingMode.HALF_UP);
                    
                    logger.info("Successfully converted address to coordinates: lat={}, lng={}", latitude, longitude);
                    return Map.of("latitude", latitudeBD.doubleValue(), "longitude", longitudeBD.doubleValue());
                } else {
                    logger.warn("No documents found in the API response for address: {}", address);
                }
            } else {
                logger.warn("Unexpected API response structure: {}", body);
            }
            throw new RuntimeException("주소에 해당하는 좌표를 찾을 수 없습니다.");
        } catch (Exception e) {
            // 오류 발생 시 로깅하고 예외를 던짐
            logger.error("Error occurred while converting address to coordinates: {}", e.getMessage(), e);
            throw new RuntimeException("주소를 좌표로 변환하는 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
    
    // 아래 주석 처리된 코드는 모든 지점의 정보를 가져오고 좌표를 확인하는 메서드
    // 현재는 사용되지 않지만, 필요시 주석을 해제하여 사용할 수 있음
    /*
    @Transactional(readOnly = true)
    public List<BranchesDTO> getAllBranchesForMap() {
        return branchesRepository.findByActiveTrue().stream()
                .map(this::convertToDTO)
                .map(this::ensureCoordinates)
                .collect(Collectors.toList());
    }

    private BranchesDTO convertToDTO(BranchEntity branch) {
        BranchesDTO dto = new BranchesDTO();
        dto.setBranchId(branch.getBranchId());
        dto.setBranchesName(branch.getName());
        dto.setPostCode(branch.getPostCode());
        dto.setAddress(branch.getAddress());
        dto.setAddress2(branch.getAddress2());
        dto.setPhone(branch.getPhone());
        dto.setActive(branch.getActive());
        dto.setLatitude(branch.getLatitude() != null ? branch.getLatitude().doubleValue() : null);
        dto.setLongitude(branch.getLongitude() != null ? branch.getLongitude().doubleValue() : null);
        return dto;
    }

    private BranchesDTO ensureCoordinates(BranchesDTO branch) {
        if (branch.getLatitude() == null || branch.getLongitude() == null) {
            try {
                Map<String, Double> coordinates = getCoordinatesFromAddress(
                        branch.getPostCode(), branch.getAddress(), branch.getAddress2());
                branch.setLatitude(coordinates.get("latitude"));
                branch.setLongitude(coordinates.get("longitude"));

                // 데이터베이스 엔티티 업데이트
                branchesRepository.updateCoordinates(
                    branch.getBranchId(), 
                    Double.valueOf(branch.getLatitude()), 
                    Double.valueOf(branch.getLongitude())
                );
            } catch (Exception e) {
                logger.error("Failed to get coordinates for branch: {}", branch.getBranchId(), e);
            }
        }
        return branch;
    }
    */
}