package com.ex.service;

import com.ex.data.BranchesDTO;
import com.ex.data.StaffMgDTO;
import com.ex.entity.BranchEntity;
import com.ex.entity.MembersEntity;
import com.ex.repository.BranchesRepository;
import com.ex.repository.MembersMgRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffMgService {

    private final MembersMgRepository membersMgRepository;
    private final BranchesRepository branchRepository;

    // 모든 직원 정보를 페이지 네이션을 사용하여 가져옴
    public Page<StaffMgDTO> getAllStaff(int page, int size) {
        // 페이지 요청을 생성 (page: 페이지 번호, size: 페이지 당 항목 수)
        Pageable pageable = PageRequest.of(page, size);
        // "REGULAR" 타입이 아닌 직원들만 가져옴
        Page<MembersEntity> staffEntities = membersMgRepository.findByUserTypeNotIn(List.of("REGULAR"), pageable);
        // 가져온 직원 데이터를 DTO 형태로 변환
        return staffEntities.map(this::convertToDTOWithBranch);
    }

    // 모든 직원 정보를 리스트 형태로 가져옴
    public List<StaffMgDTO> getAllStaffList() {
        // "REGULAR" 타입이 아닌 직원들만 가져옴
        List<MembersEntity> staffEntities = membersMgRepository.findByUserTypeNotIn(List.of("REGULAR"));
        // 가져온 직원 데이터를 DTO 형태로 변환하여 리스트로 반환함
        return staffEntities.stream()
                            .map(this::convertToDTOWithBranch)
                            .collect(Collectors.toList());
    }
    
    // ID를 기준으로 특정 직원 정보를 가져옵니다.
    public StaffMgDTO getStaffById(Integer id) {
        // 주어진 ID로 직원을 찾고, DTO 형태로 변환합니다. 없으면 예외를 발생
        return membersMgRepository.findById(id)
            .map(this::convertToDTOWithBranch)
            .orElseThrow(() -> new RuntimeException("Staff not found"));
    }

    // 키워드로 직원 검색을 수행하고, 페이지 네이션과 정렬을 적용하여 결과를 반환함
    public Page<StaffMgDTO> searchStaff(String keyword, int page, int size, String sortBy, String sortDir) {
        // 정렬 방향을 설정합니다.
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        // 페이지 요청을 생성합니다.
        Pageable pageable = PageRequest.of(page, size, sort);

        // 검색 결과를 페이지 형태로 가져옴
        Page<MembersEntity> staffPage = membersMgRepository.searchStaffByKeyword(keyword, pageable);

        // 가져온 직원 데이터를 DTO 형태로 변환
        return staffPage.map(this::convertToDTOWithBranch);
    }

    // "REGULAR" 타입의 모든 직원 정보를 리스트 형태로 가져
    public List<StaffMgDTO> getAllRegularMembers() {
        // "REGULAR" 타입의 직원들만 가져옵니다.
        return membersMgRepository.findByUserType("REGULAR")
            .stream()
            .map(this::convertToDTOWithBranch)
            .collect(Collectors.toList());
    }

    @Transactional
    // 직원 등록 또는 정보를 업데이트함
    public StaffMgDTO registerStaff(StaffMgDTO staffDTO) {
        // 주어진 회원 ID로 기존 직원을 찾음
        MembersEntity member = membersMgRepository.findByMemberId(staffDTO.getMemberId())
                .orElseThrow(() -> new RuntimeException("Member not found"));

        // 유효한 사용자 타입인지 확인함
        if (!isValidUserType(staffDTO.getUsertype())) {
            throw new IllegalArgumentException("Invalid user type");
        }

        // 직원 정보를 업데이트
        member.setUserType(staffDTO.getUsertype());
        member.setBranchId(staffDTO.getBranchId());

        // 업데이트된 직원을 저장
        MembersEntity savedMember = membersMgRepository.save(member);

        // DTO로 변환 후, 기본적으로 활성 상태로 설정
        StaffMgDTO savedStaffDTO = convertToDTOWithBranch(savedMember);
        savedStaffDTO.setActive(true);

        return savedStaffDTO;
    }
    
    // 기존 직원 정보를 업데이트
    public StaffMgDTO updateStaff(Integer id, StaffMgDTO staffDTO) {
        // 주어진 ID로 직원을 찾음.
        MembersEntity member = membersMgRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // 직원의 활성 상태에 따라 처리
        if (!staffDTO.isActive()) {
            // 퇴사 처리: 사용자 타입을 "REGULAR"로 변경하고 지점을 null로 설정
            member.setUserType("REGULAR");
            member.setBranchId(null);
        } else {
            // 재직 중 처리: 유효한 사용자 타입인지 확인 후 업데이트
            if (isValidUserType(staffDTO.getUsertype())) {
                member.setUserType(staffDTO.getUsertype());
            } else {
                throw new IllegalArgumentException("Invalid user type");
            }
            member.setBranchId(staffDTO.getBranchId());
        }

        // 업데이트된 직원을 저장하고 DTO로 변환하여 반환
        MembersEntity updatedMember = membersMgRepository.save(member);
        return convertToDTOWithBranch(updatedMember);
    }
    
    @Transactional
    // 직원의 사용자 타입을 변경
    public StaffMgDTO updateStaffType(Integer id, String newType) {
        // 주어진 ID로 직원을 찾음
        MembersEntity member = membersMgRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        
        // 유효한 사용자 타입인지 확인
        if (!isValidUserType(newType)) {
            throw new IllegalArgumentException("Invalid user type");
        }

        // 사용자 타입을 업데이트하고 저장
        member.setUserType(newType);
        MembersEntity updatedMember = membersMgRepository.save(member);
        return convertToDTOWithBranch(updatedMember);
    }

    @Transactional
    // 직원을 비활성화 상태로 변경
    public Boolean deactivateStaff(Integer id) {
        // 주어진 ID로 직원을 찾음
        MembersEntity member = membersMgRepository.findById(id).orElse(null);
        if (member != null && isStaff(member.getUserType())) {
            // 사용자 타입을 "INACTIVE"로 변경하고 저장
            member.setUserType("INACTIVE");
            membersMgRepository.save(member);
            return true;
        }
        return false;
    }

    @Transactional
    // 직원을 삭제합니다.
    public Boolean deleteStaff(Integer id) {
        // 주어진 ID로 직원이 존재하는지 확인
        if (membersMgRepository.existsById(id)) {
            MembersEntity member = membersMgRepository.findById(id).orElse(null);
            if (member != null && isStaff(member.getUserType())) {
                // 직원을 삭제
                membersMgRepository.deleteById(id);
                return true;
            }
        }
        return false;
    }

    // 모든 지점 정보를 리스트 형태로 가져
    public List<BranchesDTO> getAllBranches() {
        return branchRepository.findAll().stream()
                .map(this::convertToBranchDTO)
                .collect(Collectors.toList());
    }

    // 직원이 'TEACHER', 'DIRECTOR', 'ADMIN' 중 하나의 타입인지 확인
    private Boolean isStaff(MembersEntity member) {
        return isStaff(member.getUserType());
    }

    // 사용자 타입이 'TEACHER', 'DIRECTOR', 'ADMIN' 중 하나인지 확인
    private Boolean isStaff(String userType) {
        return List.of("TEACHER", "DIRECTOR", "ADMIN").contains(userType);
    }

    // 사용자 타입이 유효한지 확인
    private Boolean isValidUserType(String userType) {
        return List.of("REGULAR", "TEACHER", "DIRECTOR", "ADMIN", "INACTIVE").contains(userType);
    }

    // MembersEntity를 StaffMgDTO로 변환하고, 지점 이름을 설정
    private StaffMgDTO convertToDTOWithBranch(MembersEntity member) {
        StaffMgDTO dto = StaffMgDTO.builder()
            .memberId(member.getMemberId())
            .username(member.getUsername())
            .name(member.getName())
            .email(member.getEmail())
            .phone(member.getPhone())
            .usertype(member.getUserType())
            .branchId(member.getBranchId())
            .active(!List.of("REGULAR", "INACTIVE").contains(member.getUserType()))
            .build();

        if (member.getBranchId() != null) {
            // 지점 ID로 BranchEntity를 찾고 DTO에 지점 이름을 설정
            BranchEntity branch = branchRepository.findById(member.getBranchId())
                .orElse(null);
            if (branch != null) {
                dto.setBranchName(branch.getName());
            }
        }
        return dto;
    }

    // BranchEntity를 BranchesDTO로 변환
    private BranchesDTO convertToBranchDTO(BranchEntity branch) {
        return BranchesDTO.builder()
                .branchId(branch.getBranchId())
                .branchesName(branch.getName())
                .address(branch.getAddress())
                .address2(branch.getAddress2())
                .phone(branch.getPhone())
                .active(branch.getActive())
                .build();
    }
}
