package com.ex.controller;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ex.data.AttendanceDTO;
import com.ex.data.BranchesDTO;
import com.ex.data.DogsDTO;
import com.ex.data.MonthcareGroupsDTO;
import com.ex.entity.BranchEntity;
import com.ex.entity.MembersEntity;
import com.ex.service.AttendanceService;
import com.ex.service.BranchesService;
import com.ex.service.DogService;
import com.ex.service.MembersService;
import com.ex.service.MonthcareGroupsService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
@RequestMapping("/attendance*")
@RequiredArgsConstructor
public class AttendanceController {

	@Autowired
	private AttendanceService attendanceService;
	@Autowired
	private MembersService membersService;
	@Autowired
	private MonthcareGroupsService monthcareGroupsService;
	@Autowired
	private BranchesService branchesService;
	
	@GetMapping("test")
	@ResponseBody
	public int test() {
		return 0;
	}

	
	// 출석부 목록
	@GetMapping("")
    public String getAttendanceList(@RequestParam(value = "date", required = false) String date,
    							@RequestParam(value = "selectMonthGroup", required = false) Integer selectMonthGroup,
                                Model model, Principal principal) {
		
		// 선택한 일자가 없을시 오늘 날짜로 세팅
		LocalDate currentDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
		
		// 사용자정보(지점id) 조회
		MembersEntity me = membersService.findByUsername(principal.getName());
		Integer branchId = me.getBranchId();
		
		// 소속지점정보 조회 (화면 지점이름을 조회하기 위함)
		BranchesDTO brancheDTO = branchesService.getBranchById(branchId);
		
		// 소속지점정규반 목록조회 (반선택 목록 조회하기 위함)
		List<MonthcareGroupsDTO> monthGroupList = monthcareGroupsService.getMonthcareGroupByBranch(branchId);
		
        // 모든반 출석부 목록조회	: 미선택시 	> 선생님이 근무하고 있는 지점의 해당일자 모든반 출석부
        // 특정반 출석부 목록조회 	: 반선택시 	> 선생님이 근무하고 있는 지점의 해당일자 해당반 출석부
        List<AttendanceDTO> attendances = attendanceService.getAttendanceByDateAndBranchOrMonthGroup(principal.getName(), currentDate, branchId, selectMonthGroup);

        model.addAttribute("currentDate", currentDate);
        model.addAttribute("brancheDTO", brancheDTO);
        model.addAttribute("monthGroupList", monthGroupList);
        model.addAttribute("attendances", attendances);
        
        return "attendance/attendanceList";
    }
	
	
	// 출석부 등록
	@GetMapping("/createAttendance")
    public String createAttendanceForm(@RequestParam(value = "date", required = false) String date,
							            Model model, Principal principal) {
		
		// 지점명을 가져오기 위해 사용자의 근무지 지점정보를 가져와 AttendanceDTO에 대입
		MembersEntity me = membersService.findByUsername(principal.getName());
		Integer branchId = me.getBranchId();
		
		// 지점별 정규반 목록과 강아지 목록조회
		List<DogsDTO> dogList = attendanceService.findDogByBranch(branchId);
		
		BranchEntity be = new BranchEntity();
		be.setBranchId(branchId);
		AttendanceDTO attendanceDTO = new AttendanceDTO();
		attendanceDTO.setBranch(be);
		
		List<MonthcareGroupsDTO> monthGroupList = monthcareGroupsService.getMonthcareGroupByBranch(branchId);
        
        model.addAttribute("dogList", dogList);
        model.addAttribute("brancheEntity", be);
        model.addAttribute("monthGroupList", monthGroupList);
        return "attendance/createAttendance";
    }

	
    @PostMapping("/create")
    public String createAttendance(@RequestParam("branch") Integer branchId, AttendanceDTO attendanceDTO) {
    	attendanceService.createAttendance(branchId, attendanceDTO);
        return "redirect:/attendance";
    }
    
    
    // 출석부 상세조회
    @GetMapping("/details/{id}")
    public String viewOrEditAttendance(@PathVariable("id") Integer id,
    									@RequestParam(value = "mode", defaultValue = "view") String mode,
                                       Model model, Principal principal) {
        
        // 해당 출석부 항목 조회
        AttendanceDTO attendance = attendanceService.getAttendanceById(id);
        
        // 사용자 정보 (지점 정보) 조회
        MembersEntity me = membersService.findByUsername(principal.getName());
        Integer branchId = me.getBranchId();
        
        // 소속 지점 정규반 목록 및 강아지 목록 조회
        List<MonthcareGroupsDTO> monthGroupList = monthcareGroupsService.getMonthcareGroupByBranch(branchId);
        List<DogsDTO> dogList = attendanceService.findDogByBranch(branchId);

        model.addAttribute("attendance", attendance);
        model.addAttribute("monthGroupList", monthGroupList);
        model.addAttribute("dogList", dogList);
        model.addAttribute("mode", mode);
        
        return "attendance/attendanceDetails";
    }

    
    // 출석부 수정폼
    @PostMapping("/updateForm")
    public String updateAttendanceForm(AttendanceDTO attendanceDTO, Model model, Principal principal) {
    	
    	// 사용자 정보 (지점 정보) 조회
        MembersEntity me = membersService.findByUsername(principal.getName());
        Integer branchId = me.getBranchId();
        
        // 소속 지점 정규반 목록 및 강아지 목록 조회
        List<MonthcareGroupsDTO> monthGroupList = monthcareGroupsService.getMonthcareGroupByBranch(branchId);
        List<DogsDTO> dogList = attendanceService.findDogByBranch(branchId);

        model.addAttribute("attendance", attendanceDTO);
        model.addAttribute("monthGroupList", monthGroupList);
        model.addAttribute("dogList", dogList);
        
        return "attendance/updateAttendance";
    }
    
    
    // 출석부 수정
    @PostMapping("/update")
    public String updateAttendance(AttendanceDTO attendanceDTO) {
    	
    	attendanceService.updateAttendance(attendanceDTO);
    	
    	return "redirect:/attendance";
    }
	
}
