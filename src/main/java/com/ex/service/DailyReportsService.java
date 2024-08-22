package com.ex.service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ex.data.AttendanceDTO;
import com.ex.data.DailyReportsDTO;
import com.ex.entity.AttendanceEntity;
import com.ex.entity.DailyReportsEntity;
import com.ex.entity.MembersEntity;
import com.ex.repository.AttendanceRepository;
import com.ex.repository.DailyReportsRepository;
import com.ex.repository.MembersRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailyReportsService {

	private final DailyReportsRepository dailyReportsRepository;
	private final MembersRepository membersRepository;
	
	@Autowired
	AttendanceService attendanceService;
	@Autowired
	private AttendanceRepository attendanceRepository;
	
	// 캘린더 알림장 모두 조회
	public List<DailyReportsDTO> getDailyReportsList(String username){
		List<DailyReportsDTO> list = null;
		DailyReportsDTO di = null;
		
		// 사용자 로그인 id로 사용자정보 조회
		Optional<MembersEntity> op = membersRepository.findByUsername(username);
		if(op.isPresent()) {
			// 사용자정보로 알림장 조회
			List<DailyReportsEntity> delist = dailyReportsRepository.findByMembers(op.get());
			list = new ArrayList<>(delist.size());
			for(DailyReportsEntity d: delist) {
				di = new DailyReportsDTO().builder()
						.id(d.getId())
						.dogs(d.getDogs())
						.attendance(d.getAttendance())
						.report_date(d.getReport_date())
						.behavior(d.getBehavior())
						.activities(d.getActivities())
						.meals(d.getMeals())
						.health(d.getHealth())
						.bowel(d.getBowel())
						.contents(d.getContents())
						.title(d.getTitle())
						.build();
						// .members(d.getMembers())
				list.add(di);
			}
		}
		return list;
	}
	
	
	// 알림장 상세조회
	public DailyReportsDTO getDailyReports(Integer id){
		DailyReportsEntity de = dailyReportsRepository.findById(id).get();
		DailyReportsDTO dailyReportsDTO = DailyReportsDTO.builder()
											.id(de.getId())
											.dogs(de.getDogs())
											.attendance(de.getAttendance())
											.report_date(de.getReport_date())
											.behavior(de.getBehavior())
											.activities(de.getActivities())
											.meals(de.getMeals())
											.health(de.getHealth())
											.bowel(de.getBowel())
											.title(de.getTitle())
											.contents(de.getContents())
											.members(de.getMembers())
											.build();
		return dailyReportsDTO;
	}
	
	
	// 알림장등록
	public void create(DailyReportsDTO dailyReportsDTO, Integer attendanceId, String username, String selectDate) {
//		LocalDate diarydate = LocalDate.parse(selectDate);
		
		// 출석부 상세조회
		AttendanceEntity ae = attendanceRepository.findById(attendanceId).get();
		
		DailyReportsEntity de = DailyReportsEntity.builder()
								.dogs(dailyReportsDTO.getDogs())
								.attendance(ae)
								.report_date(dailyReportsDTO.getReport_date())
								.behavior(dailyReportsDTO.getBehavior())
								.activities(dailyReportsDTO.getActivities())
								.meals(dailyReportsDTO.getMeals())
								.health(dailyReportsDTO.getHealth())
								.bowel(dailyReportsDTO.getBowel())
								.contents(dailyReportsDTO.getContents())
								.title(dailyReportsDTO.getTitle())
								.members(dailyReportsDTO.getMembers())
								.build();
		
		de = dailyReportsRepository.save(de);
		
		// 알림장 id 가져오기
		System.out.println("알림장 등록 완료, 알림장 id ::: " + de.getId());
//		de.setId(de.getId());
		ae.setDailyreport(de);
		
		attendanceRepository.save(ae);
		// 알림장 등록시 출석부의 알림장id도 update되어야함
		// 알림장 테이블에서 attendance id를 이미 참조하고 있기 때문에 해당 id를 가지고 알림장id를 넣어주면 됨
		// 출석부테이블에 알림장 id 넣어주기
		
	}
	
}
