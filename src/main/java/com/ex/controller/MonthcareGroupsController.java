package com.ex.controller;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ex.data.MembersDTO;
import com.ex.data.MonthcareGroupsDTO;
import com.ex.service.DogAssignmentsService;
import com.ex.service.MembersService;
import com.ex.service.MonthcareGroupsService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/month/*")
@RequiredArgsConstructor
public class MonthcareGroupsController {
	
	private final MonthcareGroupsService monthcareGroupService;
	private final MembersService membersService;
	private final DogAssignmentsService dogAssignmentsService;
	
	// 지점별 반 리스트 출력
	@GetMapping("list")
	@PreAuthorize("isAuthenticated()")
	public String monthcareGroupList(Model model, Principal principal) {
	    Integer branch_id = membersService.readMembersInfo(principal.getName()).getBranchId();
	    
	    List<MonthcareGroupsDTO> monthcareList = monthcareGroupService.getMonthcareGroupByBranch(branch_id);
	    
	    Map<Integer, String> capacityInfoMap = new HashMap<>();
	    for (MonthcareGroupsDTO group : monthcareList) {
	        int currentStudents = dogAssignmentsService.countCurrentAndFutureStudentsInGroup(group.getId());
	        String capacityInfo = currentStudents + " / " + group.getCapacity();
	        capacityInfoMap.put(group.getId(), capacityInfo);
	    }
	    
	    model.addAttribute("monthcare", monthcareList);
	    model.addAttribute("capacityInfoMap", capacityInfoMap);
	    model.addAttribute("branch_id", branch_id);
	    return "monthcaregroups/monthgroup_list";
	}
	
	
	// 지점별 신규 반 생성
	@GetMapping("create")
	@PreAuthorize("isAuthenticated()")
	public String createMonthcareGroup(Model model, MonthcareGroupsDTO monthDTO, Principal principal) {		
		Integer branch_id = membersService.readMembersInfo(principal.getName()).getBranchId();
		model.addAttribute("branch_id", branch_id);
		model.addAttribute("monthDTO",monthDTO);
		
		// 해당 지점에 소속되어져 있는 선생님 리스트를 model 에 담아서 view 로 보냄
		List<MembersDTO> teacher = monthcareGroupService.getTeachers(branch_id);
		model.addAttribute("teachers", teacher);
		return "monthcaregroups/add_monthgroup";
	}
	
	
	@PostMapping("create")
	@PreAuthorize("isAuthenticated()")
	public String createMonthcareGroup(MonthcareGroupsDTO monthDTO, Principal principal) {
		Integer branch_id = membersService.readMembersInfo(principal.getName()).getBranchId();
		monthcareGroupService.createMonthcareGroup(branch_id, monthDTO);
		return "redirect:/month/list";
	}
	
	
	@GetMapping("delete/{id}")
	@PreAuthorize("isAuthenticated()")
	public String deleteMonthcareGroup(@PathVariable("id") Integer month_id) {
		return "";
	}
	
	
	// 지점별 반 정보 수정
	@GetMapping("update/{id}")
	@PreAuthorize("isAuthenticated()")
	public String updateMonthcareGroup(@PathVariable("id") Integer month_id, Model model, Principal principal) {
		MonthcareGroupsDTO monthDTO = monthcareGroupService.getMonthGroup(month_id);
		model.addAttribute("monthDTO", monthDTO);
		Integer branch_id = membersService.readMembersInfo(principal.getName()).getBranchId();
		List<MembersDTO> teacher = monthcareGroupService.getTeachers(branch_id);
		model.addAttribute("teachers", teacher);
		return "monthcaregroups/update_monthgroup";
	}
	
	
	@PostMapping("update")
	@PreAuthorize("isAuthenticated()")
	public String updateMonthcareGroups(MonthcareGroupsDTO monthDTO) {
		monthcareGroupService.updateMonthgroup(monthDTO.getId(), monthDTO);
		return "redirect:/month/list";
	}
}
