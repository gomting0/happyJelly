package com.ex.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ex.data.BranchesDTO;
import com.ex.data.BranchesListResponseDTO;
import com.ex.entity.BranchEntity;
import com.ex.repository.BranchesRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
// @RequiredArgsConstructor는 final 필드에 대한 생성자를 자동으로 생성
@RequiredArgsConstructor
public class BranchesService {
   // 로깅을 위한 Logger 객체 생성
   private static final Logger logger = LoggerFactory.getLogger(BranchesService.class);
   
   // 브랜치 데이터를 데이터베이스에서 조작하기 위한 리포지토리
   private final BranchesRepository branchesRepository;
   // 카카오 API를 사용하기 위한 서비스
    private final KakaoApiService kakaoApiService; 
 
    // 새로운 브랜치를 등록하는 메서드
    @Transactional // 데이터베이스 트랜잭션을 보장합니다.
    public BranchesDTO registerBranch(BranchesDTO branchesDTO) {
        // 카카오 API를 사용하여 주소를 좌표로 변환
        Map<String, Double> coordinates = kakaoApiService.getCoordinatesFromAddress(
            branchesDTO.getPostCode(), 
            branchesDTO.getAddress(), 
            branchesDTO.getAddress2()
        );
        
        // 변환된 좌표를 DTO에 설정
        branchesDTO.setLatitude(coordinates.get("latitude"));
        branchesDTO.setLongitude(coordinates.get("longitude"));
        branchesDTO.setActive(true);
         
        // DTO를 엔티티로 변환
        BranchEntity branch = convertToEntity(branchesDTO);
        // 데이터베이스에 저장
        BranchEntity savedBranch = branchesRepository.save(branch);
        
        // 저장된 엔티티를 다시 DTO로 변환하여 반환
        return convertToDTO(savedBranch);
    }

    // 활성 상태와 이름으로 정렬된 브랜치 목록을 반환하는 메서드
    public BranchesListResponseDTO listBranchesSortedByActiveAndName(int page, int size) {
        // 정렬 기준 설정: 활성 상태 내림차순, 이름 오름차순
        Sort sort = Sort.by(Sort.Order.desc("active"), Sort.Order.asc("name"));
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        // 페이지네이션과 정렬을 적용하여 브랜치 조회
        Page<BranchEntity> branchPage = branchesRepository.findAll(pageRequest);

        // 엔티티를 DTO로 변환
        List<BranchesDTO> branchesDTOs = branchPage.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        // 응답 DTO 생성 및 반환
        return BranchesListResponseDTO.builder()
                .branches(branchesDTOs)
                .totalPages(branchPage.getTotalPages())
                .totalElements(branchPage.getTotalElements())
                .currentPage(branchPage.getNumber())
                .build();
    }
    
    // 모든 활성 브랜치를 조회하는 메서드
    public List<BranchesDTO> getAllActiveBranches() {
        return branchesRepository.findByActiveTrue().stream()
                                  .map(this::convertToDTO)
                                  .collect(Collectors.toList());
    }

    // 브랜치를 검색하는 메서드
    public BranchesListResponseDTO searchBranches(String term, int page, int size, String sortBy, String sortDir, Boolean activeOnly) {
        // 정렬 설정
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        // 검색 조건에 따라 브랜치 조회
        Page<BranchEntity> branchPage = branchesRepository.searchBranches(term, activeOnly, pageRequest);

        // 엔티티를 DTO로 변환
        List<BranchesDTO> branchesDTOs = branchPage.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        // 응답 DTO 생성 및 반환
        return BranchesListResponseDTO.builder()
            .branches(branchesDTOs)
            .totalPages(branchPage.getTotalPages())
            .totalElements(branchPage.getTotalElements())
            .currentPage(branchPage.getNumber())
            .build();
    }

    // 브랜치 정보를 업데이트하는 메서드
    @Transactional
    public BranchEntity updateBranch(Integer id, BranchesDTO branchesDTO) {
        // ID로 브랜치 조회
        BranchEntity branch = branchesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found"));

        // DTO의 정보로 브랜치 엔티티 업데이트
        branch.setName(branchesDTO.getBranchesName());
        branch.setPostCode(branchesDTO.getPostCode());
        branch.setAddress(branchesDTO.getAddress());
        branch.setAddress2(branchesDTO.getAddress2());
        branch.setPhone(branchesDTO.getPhone());
        branch.setActive(branchesDTO.getActive());
        
        // 위도와 경도 설정 (BigDecimal로 변환 필요)
        if (branchesDTO.getLatitude() != null) {
            branch.setLatitude(BigDecimal.valueOf(branchesDTO.getLatitude()));
        }
        if (branchesDTO.getLongitude() != null) {
            branch.setLongitude(BigDecimal.valueOf(branchesDTO.getLongitude()));
        }

        // 업데이트된 브랜치 저장 및 반환
        return branchesRepository.save(branch);
    }

    // 브랜치 목록을 조회하는 메서드
    public BranchesListResponseDTO listBranches(int page, int size, String sortBy, String sortDir, boolean activeOnly) {
        logger.info("Listing branches: page={}, size={}, sortBy={}, sortDir={}, activeOnly={}", page, size, sortBy, sortDir, activeOnly);
        
        // 정렬 설정
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<BranchEntity> branchPage;
        // activeOnly 파라미터에 따라 다른 쿼리 실행
        if (activeOnly) {
            branchPage = branchesRepository.findByActiveTrue(pageRequest);
        } else {
            branchPage = branchesRepository.findAllBranchesWithMonthcareGroups(pageRequest);
        }

        // 엔티티를 DTO로 변환
        List<BranchesDTO> branchesDTOs = branchPage.getContent().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());

        logger.info("Found {} branches", branchesDTOs.size());

        // 응답 DTO 생성 및 반환
        return BranchesListResponseDTO.builder()
                .branches(branchesDTOs)
                .totalPages(branchPage.getTotalPages())
                .totalElements(branchPage.getTotalElements())
                .currentPage(branchPage.getNumber())
                .build();
    }

    // 브랜치의 활성 상태를 토글하는 메서드
    @Transactional
    public void toggleBranchStatus(Integer id) {
        BranchEntity branch = branchesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found with id: " + id));
        branch.setActive(!branch.getActive());
        branchesRepository.save(branch);
    }

    // 브랜치를 삭제하는 메서드
    @Transactional
    public void deleteBranch(Integer id) {
        if (!branchesRepository.existsById(id)) {
            throw new EntityNotFoundException("Branch not found with id: " + id);
        }
        branchesRepository.deleteById(id);
    }

    // ID로 브랜치를 조회하는 메서드
    @Transactional(readOnly = true)
    public BranchesDTO getBranchById(Integer id) {
        return branchesRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Branch not found with id: " + id));
    }

    // DTO를 엔티티로 변환하는 private 메서드
    private BranchEntity convertToEntity(BranchesDTO dto) {
        return BranchEntity.builder()
                .branchId(dto.getBranchId())
                .name(dto.getBranchesName())
                .postCode(dto.getPostCode())
                .address(dto.getAddress())
                .address2(dto.getAddress2())
                .phone(dto.getPhone())
                .active(dto.getActive())
                .latitude(dto.getLatitude() != null ? 
                        BigDecimal.valueOf(dto.getLatitude()).setScale(8, RoundingMode.HALF_UP) : null)
                .longitude(dto.getLongitude() != null ? 
                        BigDecimal.valueOf(dto.getLongitude()).setScale(8, RoundingMode.HALF_UP) : null)
                .build();
    }

    // 엔티티를 DTO로 변환하는 private 메서드
    private BranchesDTO convertToDTO(BranchEntity entity) {
        return BranchesDTO.builder()
                .branchId(entity.getBranchId())
                .branchesName(entity.getName())
                .postCode(entity.getPostCode())
                .address(entity.getAddress())
                .address2(entity.getAddress2())
                .phone(entity.getPhone())
                .active(entity.getActive())
                .latitude(entity.getLatitude() != null ? entity.getLatitude().doubleValue() : null)
                .longitude(entity.getLongitude() != null ? entity.getLongitude().doubleValue() : null)
                .build();
    }

    // 브랜치 엔티티의 필드를 업데이트하는 private 메서드
    private void updateBranchFields(BranchEntity branch, BranchesDTO dto, Map<String, Double> coordinates) {
        branch.setName(dto.getBranchesName());
        branch.setPostCode(dto.getPostCode());
        branch.setAddress(dto.getAddress());
        branch.setAddress2(dto.getAddress2());
        branch.setPhone(dto.getPhone());
        branch.setLatitude(BigDecimal.valueOf(coordinates.get("latitude")));
        branch.setLongitude(BigDecimal.valueOf(coordinates.get("longitude")));
    }
}