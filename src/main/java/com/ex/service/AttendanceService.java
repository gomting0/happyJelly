package com.ex.service;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ex.data.AdmissionsDTO;
import com.ex.data.AttendanceDTO;
import com.ex.data.DogsDTO;
import com.ex.entity.AttendanceEntity;
import com.ex.entity.BranchEntity;
import com.ex.entity.DogsEntity;
import com.ex.entity.MembersEntity;
import com.ex.entity.MonthcareGroupsEntity;
import com.ex.entity.SubscriptionsEntity;
import com.ex.repository.AttendanceRepository;
import com.ex.repository.BranchesRepository;
import com.ex.repository.DogsRepository;
import com.ex.repository.MembersRepository;
import com.ex.repository.MonthcareGroupsRepository;
import com.ex.repository.TestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {
	
	@Autowired
	private AttendanceRepository attendanceRepository;
	@Autowired
	MonthcareGroupsRepository monthcareGroupsRepository;
	@Autowired
	DogsRepository dogsRepository;
	@Autowired
	BranchesRepository branchesRepository;
	private final AdmissionsService admissionsService;
	
	private final TestMapper testMapper;
	
	public List<AttendanceDTO> getAttendanceAll(){
		List<AttendanceDTO> list = null;
		return list;
	}
	
	
	// 출석부 상세조회
	public AttendanceDTO getAttendanceById(Integer attendanceId) {
		AttendanceEntity ae = attendanceRepository.findById(attendanceId).get();
		AttendanceDTO dto = AttendanceDTO.builder()
								.id(ae.getId())
								.dog(ae.getDog())
								.daygroup(ae.getDaygroup())
								.monthgroup(ae.getMonthgroup())
								.attendancedate(ae.getAttendancedate())
								.status(ae.getStatus())
								.dailyreport(ae.getDailyreport())
								.notes(ae.getNotes())
								.branch(ae.getBranch())
								.build();
		return dto;
	}
	
	
	// 해당일자출석부 목록조회
	public List<AttendanceDTO> getAttendanceByDate(LocalDate currentDate){
		return attendanceRepository.findByAttendancedate(currentDate).stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}
	
	
	public List<AttendanceDTO> getAttendenceByDateAndBranch(LocalDate currentDate, Integer branchId){
		return testMapper.dateAndBranchAttendence(currentDate, branchId);
	}

	
	// 일자,지점1,반1 출석부 조회
	public List<AttendanceDTO> getAttendanceByDateAndBranchOrMonthGroup( String username
												, LocalDate attendancedate
												, Integer branch, Integer monthgroup) {
		
		// 선택된 반이 있다면 반id로 해당일자 출석부 조회
		if(monthgroup != null) {
			MonthcareGroupsEntity mge = new MonthcareGroupsEntity();
			mge.setId(monthgroup);
			return attendanceRepository.findByAttendancedateAndMonthgroup(attendancedate, mge)
					.stream()
					.map(this::convertToDTO)
					.collect(Collectors.toList());
			
		// 선택된 반이 없다면 지점id로 해당일자 출석부 조회
		}else {
			BranchEntity be = new BranchEntity();
			be.setBranchId(branch);
			
			return attendanceRepository.findByAttendancedateAndBranch(attendancedate, be)
					.stream()
					.map(this::convertToDTO)
					.collect(Collectors.toList());
		}
    }
	
	
	// 지점별 강아지 출력
	public List<DogsDTO> findDogByBranch(Integer branchId) {
        List<DogsEntity> dogs = dogsRepository.findByBranch(branchId);

        List<DogsDTO> dogsDTOList = new ArrayList<>();

        for (DogsEntity dog : dogs) {
            DogsDTO dto = new DogsDTO();
            dto.setDogId(dog.getDogId());
            dto.setDogname(dog.getDogname());
            dogsDTOList.add(dto);
        }
        return dogsDTOList;
    }
	
	
	// 출석부 출석상태 수정
	public void updateAttendance(AttendanceDTO attendanceDTO) {
		
		AttendanceEntity oldae = attendanceRepository.findById(attendanceDTO.getId()).get();
		
		AttendanceEntity ae = AttendanceEntity.builder()
				.id(attendanceDTO.getId())
				.dog(oldae.getDog())
				.daygroup(attendanceDTO.getDaygroup())
				.monthgroup(attendanceDTO.getMonthgroup())
				.attendancedate(attendanceDTO.getAttendancedate())
				.status(attendanceDTO.getStatus())
				.dailyreport(oldae.getDailyreport())
				.notes(attendanceDTO.getNotes())
				.branch(oldae.getBranch())
				.build();
				
		attendanceRepository.save(ae);
		
		// USER_TYPE != REGULAR >>> 일반 수정
		// USER_TYPE == REGULAR >>> 특이사항만 수정
		
//		if(me.get().getUser_type().equals("REGULAR")) {
//			
//		} else {
//			
//		}
	}
	
	
	// 출석부 등록
	public void createAttendance(Integer branchId, AttendanceDTO attendanceDTO) {
		
		BranchEntity branchEntity = branchesRepository.findById(branchId).get();
		
        // DTO를 엔티티로 변환
        AttendanceEntity attendanceEntity = AttendanceEntity.builder()
        		.attendancedate(attendanceDTO.getAttendancedate())
        		.status(attendanceDTO.getStatus())
        		.notes(attendanceDTO.getNotes())
        		.branch(branchEntity)
        		.dog(attendanceDTO.getDog())
        		.monthgroup(attendanceDTO.getMonthgroup())
        		.build();

        // 데이터베이스에 저장
        attendanceRepository.save(attendanceEntity);
        
    }
	

    public void setMonthAttendance(SubscriptionsEntity subs, int admissionId) {
    	
    	String dayOfWeekString = subs.getTicket().getDayofweek();
        String[] dayOfWeekArray = dayOfWeekString.split(",");

        LocalDate today = LocalDate.now();
        YearMonth nextMonth = YearMonth.of(today.getYear(), today.plusMonths(1).getMonth());
        LocalDate firstDayOfNextMonth = nextMonth.atDay(1);
        LocalDate lastDayOfNextMonth = nextMonth.atEndOfMonth();

        List<LocalDate> attendanceDates = new ArrayList<>();

        for (LocalDate date = firstDayOfNextMonth; !date.isAfter(lastDayOfNextMonth); date = date.plusDays(1)) {
            int dayOfWeekValue = date.getDayOfWeek().getValue();

            if (Arrays.asList(dayOfWeekArray).contains(String.valueOf(dayOfWeekValue))) {
                attendanceDates.add(date);
            }
        }
        
        AdmissionsDTO admissionDTO = admissionsService.getAdmissionById(admissionId);
        MonthcareGroupsEntity me = admissionDTO.getMonthcaregroups();
        
        for (LocalDate attendanceDate : attendanceDates) {
            AttendanceDTO attendanceDTO = new AttendanceDTO();
            attendanceDTO.setAttendancedate(attendanceDate);
            attendanceDTO.setStatus("PRESENT");
            attendanceDTO.setNotes("");
            attendanceDTO.setDog(subs.getDogs());
            attendanceDTO.setBranch(admissionDTO.getBranch());
            attendanceDTO.setMonthgroup(me);
            
            createAttendance(admissionDTO.getBranch().getBranchId(), attendanceDTO);
        }
    }
	
	
	private AttendanceDTO convertToDTO(AttendanceEntity entity) {
		AttendanceDTO dto = new AttendanceDTO();
		dto.setId(entity.getId());
		dto.setDog(entity.getDog());
		dto.setDaygroup(entity.getDaygroup());
		dto.setMonthgroup(entity.getMonthgroup());
		dto.setAttendancedate(entity.getAttendancedate());
		dto.setStatus(entity.getStatus());
		dto.setDailyreport(entity.getDailyreport());
		dto.setNotes(entity.getNotes());
		dto.setBranch(entity.getBranch());
		return dto;
	}
	
}
