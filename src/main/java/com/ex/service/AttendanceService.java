package com.ex.service;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
			System.out.println("(monthgroup != null 선택된 반 있음");
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
	
	
	// 출석부 수정
	public void updateAttendance(AttendanceDTO attendanceDTO) {
		
		AttendanceEntity ae = AttendanceEntity.builder()
				.id(attendanceDTO.getId())
				.dog(attendanceDTO.getDog())
				.daygroup(attendanceDTO.getDaygroup())
				.monthgroup(attendanceDTO.getMonthgroup())
				.attendancedate(attendanceDTO.getAttendancedate())
				.status(attendanceDTO.getStatus())
				.dailyreport(attendanceDTO.getDailyreport())
				.notes(attendanceDTO.getNotes())
				.branch(attendanceDTO.getBranch())
				.build();
				
		attendanceRepository.save(ae);
		
		// USER_TYPE != REGULAR >>> 일반 수정
		// USER_TYPE == REGULAR >>> 특이사항만 수정
		
//		if(me.get().getUser_type().equals("REGULAR")) {
//			System.out.println("regular");
//		} else {
//			
//		}
	}
	
	
	// 출석부 등록
	public void createAttendance(Integer branchId, AttendanceDTO attendanceDTO) {
		BranchEntity be = new BranchEntity();
    	be.setBranchId(branchId);
		
        // DTO를 엔티티로 변환
        AttendanceEntity attendanceEntity = new AttendanceEntity();
        attendanceEntity.setAttendancedate(attendanceDTO.getAttendancedate());
        attendanceEntity.setDog(attendanceDTO.getDog());
        attendanceEntity.setMonthgroup(attendanceDTO.getMonthgroup());
        attendanceEntity.setStatus(attendanceDTO.getStatus());
        attendanceEntity.setNotes(attendanceDTO.getNotes());
        attendanceEntity.setBranch(be);

        // 데이터베이스에 저장
        attendanceRepository.save(attendanceEntity);
    }
	
	
	// 결제 > 입학완료 > 개배정 > 사용자의 티켓 요일정보에 따른 한달치 출석부 세팅 일~토 1~7
    public void setMonthAttendance(SubscriptionsEntity subs, int admissionId) {
    	
    	// 구독티켓의 요일정보가져오기 (ex: "1,2,3" -> 일,월,화)
    	String dayOfWeekString = subs.getTicket().getDayofweek();
    	
    	// 구독요일을 콤마로분리하여 리스트로 변환
        String[] dayOfWeekArray = dayOfWeekString.split(",");

        // 현재날짜기준으로 다음달의 첫째날과 마지막날 계산
        LocalDate today = LocalDate.now();
        YearMonth nextMonth = YearMonth.of(today.getYear(), today.plusMonths(1).getMonth());
        LocalDate firstDayOfNextMonth = nextMonth.atDay(1);
        LocalDate lastDayOfNextMonth = nextMonth.atEndOfMonth();

        // 출석부에 넣을 날짜리스트
        List<LocalDate> attendanceDates = new ArrayList<>();

        // 첫째날부터 마지막날까지 반복
        for (LocalDate date = firstDayOfNextMonth; !date.isAfter(lastDayOfNextMonth); date = date.plusDays(1)) {
            // 현재날짜의 요일 (1 = 일요일, 2 = 월요일, ... 7 = 토요일)
            int dayOfWeekValue = date.getDayOfWeek().getValue();
            if (dayOfWeekValue == 7) dayOfWeekValue = 1; // 일요일을 1로 맞추기

            // 해당요일이 구독된요일리스트에 포함되면 출석부추가
            for (String day : dayOfWeekArray) {
                if (Integer.parseInt(day.trim()) == dayOfWeekValue) {
                    attendanceDates.add(date);
                    break;
                }
            }
        }

        AdmissionsDTO admissionDTO = admissionsService.getAdmissionById(admissionId);
        MonthcareGroupsEntity me = admissionDTO.getMonthcaregroups();
        
        // 출석 날짜들을 출석부에 저장
        for (LocalDate attendanceDate : attendanceDates) {
            // 출석부 등록을 위한 DTO 생성
            AttendanceDTO attendanceDTO = new AttendanceDTO();
            attendanceDTO.setAttendancedate(attendanceDate);
            attendanceDTO.setDog(subs.getDogs());
            attendanceDTO.setMonthgroup(me);
            attendanceDTO.setStatus("PRESENT");  // 출석 상태 초기값 설정
            attendanceDTO.setNotes("");  // 특별한 노트 없음
            
            // 출석부 등록 메서드 호출
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
		dto.setNotes(entity.getNotes());
		return dto;
	}
	
}
