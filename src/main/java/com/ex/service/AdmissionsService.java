package com.ex.service;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.ex.data.AdmissionsDTO;
import com.ex.data.MonthcareGroupsDTO;
import com.ex.entity.AdmissionsEntity;
import com.ex.entity.BranchEntity;
import com.ex.entity.DogsEntity;
import com.ex.entity.MembersEntity;
import com.ex.entity.SubscriptionsEntity;
import com.ex.repository.AdmissionsRepository;
import com.ex.repository.BranchesRepository;
import com.ex.repository.DogsRepository;
import com.ex.repository.MembersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdmissionsService {
    private final AdmissionsRepository admissionRepository;
    private final DogsRepository dogRepository;
    private final BranchesRepository branchRepository;
    private final MembersRepository membersRepository;
    private final MonthcareGroupsService monthcareGroupsService;
   
    @Transactional
    public void createAdmission(AdmissionsDTO admissionDTO) {
        log.info("Starting createAdmission with DTO: {}", admissionDTO);
        
        try {
            DogsEntity dog = dogRepository.findById(admissionDTO.getDogs().getDogId())
                .orElseThrow(() -> new RuntimeException("Dog not found with id: " + admissionDTO.getDogs().getDogId()));
            log.info("Found dog: {}", dog);

            BranchEntity branch = branchRepository.findById(admissionDTO.getBranch().getBranchId())
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + admissionDTO.getBranch().getBranchId()));
            log.info("Found branch: {}", branch);

            AdmissionsEntity ae = AdmissionsEntity.builder()
                .dogs(dog)
                .applicationDate(new Date())
                .status("PENDING")
                .desiredDate(admissionDTO.getDesiredDate())
                .pottytraining(admissionDTO.getPottytraining())
                .marking(admissionDTO.getMarking())
                .ration(admissionDTO.getRation())
                .appetite(admissionDTO.getAppetite())
                .walk(admissionDTO.getWalk())
                .numberofweeks(admissionDTO.getNumberofweeks())
                .significant(admissionDTO.getSignificant())
                .branch(branch)
                .monthcaregroups(admissionDTO.getMonthcaregroups())
                .build();
            log.info("Created AdmissionsEntity: {}", ae);

            AdmissionsEntity savedEntity = admissionRepository.save(ae);
            log.info("Successfully saved admission: {}", savedEntity);
        } catch (Exception e) {
            log.error("Error in createAdmission", e);
            throw new RuntimeException("Failed to create admission: " + e.getMessage(), e);
        }
    }
     
    public AdmissionsDTO getAdmissionById(Integer id) {
        return admissionRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    private AdmissionsDTO convertToDTO(AdmissionsEntity entity) {
        AdmissionsDTO dto = new AdmissionsDTO();
        dto.setAdmissionId(entity.getAdmissionId());
        dto.setDogs(entity.getDogs());
        dto.setApplicationDate(entity.getApplicationDate());
        dto.setStatus(entity.getStatus());
        dto.setApprovalDate(entity.getApprovalDate());
        dto.setDesiredDate(entity.getDesiredDate());
        dto.setPottytraining(entity.getPottytraining());
        dto.setMarking(entity.getMarking());
        dto.setRation(entity.getRation());
        dto.setAppetite(entity.getAppetite());
        dto.setWalk(entity.getWalk());
        dto.setNumberofweeks(entity.getNumberofweeks());
        dto.setSignificant(entity.getSignificant());
        dto.setReason(entity.getReason());
        dto.setBranch(entity.getBranch());
        dto.setMonthcaregroups(entity.getMonthcaregroups());
        return dto;
    }

    public void updateAdmissionStatus(Integer admissionId, String newStatus, String reason) {
        AdmissionsEntity admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new RuntimeException("Admission not found"));

        admission.setStatus(newStatus);

        if ("APPROVED".equals(newStatus)) {
            admission.setApprovalDate(new Date());
        }else{
            admission.setReason(reason);
        }
        admissionRepository.save(admission);
    }
    
    public void cancelAdmission(Integer id) {
        AdmissionsEntity admission = admissionRepository.findById(id).get();
        admission.setStatus("CANCELED");
        admissionRepository.save(admission);
    }

    
    public int checkPending(String status, Integer dog_id) {
        return admissionRepository.countByStatusAndDogs_DogId(status, dog_id);
    }

    public List<BranchEntity> getAllBranches() {
        return branchRepository.findAll();
    }

    public List<MonthcareGroupsDTO> getGroupsByBranch(Integer branchId) {
        return monthcareGroupsService.getMonthcareGroupByBranch(branchId);
    }
    
    public Page<AdmissionsDTO> getAdmissionsByRole(String username, int page, String status) {
        // 페이지네이션과 정렬을 위한 Pageable 객체 생성. 페이지 크기는 10, admissionId 기준 내림차순 정렬
        Pageable pageable = PageRequest.of(page, 10, Sort.by("admissionId").descending());

        // 주어진 username으로 MembersEntity를 찾음. 없으면 RuntimeException 발생
        MembersEntity member = membersRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Member not found"));

        // 멤버의 사용자 유형(ADMIN, DIRECTOR, 일반 사용자 등) 가져오기
        String userType = member.getUserType();
        Page<AdmissionsEntity> entityPage;

        // 사용자 유형에 따라 다른 쿼리 실행
        if ("ADMIN".equals(userType)) {
            // 관리자는 모든 입학 신청서를 볼 수 있음
            entityPage = admissionRepository.findAll(pageable);
        } else if ("DIRECTOR".equals(userType)) {
            // 디렉터는 자신의 지점의 입학 신청서만 볼 수 있음
            entityPage = admissionRepository.findByBranch_BranchId(member.getBranchId(), pageable);
        } else {
            // 일반 사용자는 자신의 입학 신청서만 볼 수 있음
            entityPage = admissionRepository.findByDogs_Member_Username(username, pageable);
        }

        // 상태 필터링 (모든 사용자 유형에 적용)
        if (status != null && !status.isEmpty()) {
            // 상태가 지정된 경우, 해당 상태와 일치하는 입학 신청서만 필터링
            List<AdmissionsEntity> filteredList = entityPage.getContent().stream()
                .filter(admission -> admission.getStatus().equals(status))
                .collect(Collectors.toList());

            // 필터링된 리스트로 새로운 Page 객체를 생성하고 DTO로 변환하여 반환
            return new PageImpl<>(filteredList, pageable, filteredList.size())
                .map(this::convertToDTO);
        }

        // 상태 필터링이 없는 경우, 원래의 Page 객체를 DTO로 변환하여 반환
        return entityPage.map(this::convertToDTO);
    }
    
    public void setSubscription (SubscriptionsEntity subs, Integer admissonId) {
		AdmissionsEntity ae = admissionRepository.findById(admissonId).get();
		ae.setSubscription(subs);
		System.out.println(subs);
		admissionRepository.save(ae);
	}
    
    
  
}