package com.ex.service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ex.data.AttendanceDTO;
import com.ex.data.MonthcareGroupsDTO;
import com.ex.entity.AttendanceEntity;
import com.ex.entity.BranchEntity;
import com.ex.entity.MembersEntity;
import com.ex.entity.MonthcareGroupsEntity;
import com.ex.repository.AttendanceRepository;
import com.ex.repository.MembersRepository;
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
	private MembersRepository membersRepository;
	
	private final TestMapper testMapper;
	
	public List<AttendanceDTO> getAttendanceAll(){
		List<AttendanceDTO> list = null;
		return list;
	}
	
	
	// 해당일자출석부 목록조회
	public List<AttendanceDTO> getAttendanceDate(LocalDate currentDate){
		return attendanceRepository.findByAttendancedate(currentDate).stream()
				.map(this::convertToDTO)
				.collect(Collectors.toList());
	}
	
	
	public List<AttendanceDTO> getAttendenceDateAndBranch(LocalDate currentDate, Integer branchId){
		return testMapper.dateAndBranchAttendence(currentDate, branchId);
	}

	
	// 일자,지점1,반1 출석부 조회
	public List<AttendanceDTO> getAttendanceByDateAndBranchOrMonthGroup(LocalDate attendancedate
												, Integer branch, Integer monthgroup) {
		System.out.println("===============서비스================");
		System.out.println("attendancedate ::: " + attendancedate);
		System.out.println("monthgroup ::: " + monthgroup);
		System.out.println("branch ::: " + branch);
		
		// 선택된 반이 있다면 반id로 해당일자 출석부 조회
		if(monthgroup != null) {
			System.out.println("(monthgroup != null");
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
			
			AttendanceEntity ae = attendanceRepository.findById(17).get();
			System.out.println("asfdsaf=================>>>>"+ae.getDog());
			System.out.println("asfdsaf=================>>>>"+ae.getMonthgroup());  
			System.out.println("asfdsaf=================>>>>"+ae.getDog().getDogname());
			
			return attendanceRepository.findByAttendancedateAndBranch(attendancedate, be)
					.stream()
					.map(this::convertToDTO)
					.collect(Collectors.toList());
		}
    }
	
	
	// USER_TYPE != REGULAR >>> 일반 수정
	// USER_TYPE == REGULAR >>> 특이사항만 수정
	public void updateAttendance(Integer username, AttendanceDTO attendanceDTO) {
		Optional<MembersEntity> me = membersRepository.findById(username);
		AttendanceEntity ae = AttendanceEntity.builder()
				.dog(attendanceDTO.getDog())
				.daygroup(attendanceDTO.getDaygroup())
				.monthgroup(attendanceDTO.getMonthgroup())
				.attendancedate(attendanceDTO.getAttendancedate())
				.status(attendanceDTO.getStatus())
				.dailyreport(attendanceDTO.getDailyreport())
				.notes(attendanceDTO.getNotes())
				.build();
				
		attendanceRepository.save(ae);
//		if(me.get().getUser_type().equals("REGULAR")) {
//			System.out.println("regular");
//		} else {
//			
//		}
	}
	
	
	// 출석부 등록
	public void createAttendance(AttendanceDTO attendanceDTO) {
		
		System.out.println("===============서비스createAttendance================");
		System.out.println(attendanceDTO.toString());
		
        // DTO를 엔티티로 변환
        AttendanceEntity attendanceEntity = new AttendanceEntity();
        attendanceEntity.setAttendancedate(attendanceDTO.getAttendancedate());
        attendanceEntity.setDog(attendanceDTO.getDog());
        attendanceEntity.setMonthgroup(attendanceDTO.getMonthgroup());
        attendanceEntity.setStatus(attendanceDTO.getStatus());
        attendanceEntity.setNotes(attendanceDTO.getNotes());

        // 데이터베이스에 저장
        attendanceRepository.save(attendanceEntity);
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
	
	
	// 지점id와 선생님id로 
//	public List<MonthcareGroupsDTO> getMonthGroupByTeacherId(Integer branchId, Integer teacherId){
//		List<MonthcareGroupsDTO> monthcareDtoList = new ArrayList<>();
//		List<MonthcareGroupsEntity> monthcareEntityList = monthcareGroupsRepository.findByBranchesAndTeachers(branchId, teacherId);
//		for(MonthcareGroupsEntity me : monthcareEntityList) {
//			MonthcareGroupsDTO dto = new MonthcareGroupsDTO();
//			dto.setId(me.getId());
//			dto.setName(me.getName());
//			dto.setDescription(me.getDescription());
//			dto.setCapacity(me.getCapacity());
//			dto.setBranches(me.getBranches());
//			dto.setTeacher(me.getTeachers());
//			monthcareDtoList.add(dto);
//		}
//		return monthcareDtoList;
//		
//	}
	
}
