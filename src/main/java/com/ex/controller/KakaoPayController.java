package com.ex.controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import java.security.Principal;
import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ex.data.KakaoPayDTO;
import com.ex.entity.SubscriptionsEntity;
import com.ex.service.AdmissionsService;
import com.ex.service.DogAssignmentsService;
import com.ex.service.KakaoPayService;
import com.ex.service.SubscriptionsService;

@Controller
@RequiredArgsConstructor
@Log
@RequestMapping("/kakao/*")
public class KakaoPayController {
	
    private final KakaoPayService kakaoPay;
    private final AdmissionsService admissionsService;
    private final SubscriptionsService subscriptionsService;
    private final DogAssignmentsService dogAssignmentsService;  
    private String auto;

    @PostMapping("/kakaoPay")
    public String kakaoPay(KakaoPayDTO kakaoDTO, Principal principal, @RequestParam("admissionId") int admissionId, @RequestParam("autoRenewal") String auto){
    	String redirectUrl = null;
    	Integer admission_id = admissionId;
    	kakaoDTO.setPartner_user_id(principal.getName());
    	String partner_order_id = UUID.randomUUID().toString().replace("-", "");
    	kakaoDTO.setPartner_order_id(partner_order_id);
    	kakaoDTO.setAdmissioId(admissionId);    	
    	
    	if(auto==null) {	    	
	    	redirectUrl = "redirect:" + kakaoPay.kakaoPayReady(kakaoDTO) + "?admissionId=" + admission_id;
		 }else {
			 this.auto = auto;
			redirectUrl = "redirect:" + kakaoPay.kakaoPayReady2(kakaoDTO) + "?admissionId=" + admission_id;		   
		 }
    	 return 	redirectUrl;
    }

    @GetMapping("/kakaoPaySuccess")
    public String kakaoPaySuccess(@RequestParam("pg_token") String pgToken,
                                  RedirectAttributes redirectAttributes,
                                  @RequestParam("admissionId") int admissionId,
                                  Principal principal) {
    	KakaoPayDTO kakaoDTO = null;
    	if(auto==null) {
    		kakaoDTO = kakaoPay.payApprove(pgToken);
    	}else{
    		kakaoDTO = kakaoPay.payApprove2(pgToken);
    	}
        
        
        String reason = null;
        admissionsService.updateAdmissionStatus(admissionId, "DONE", reason);
        kakaoDTO.setAuto(auto);
        SubscriptionsEntity subs = subscriptionsService.createSubscription(principal.getName(), admissionId, kakaoDTO);
        admissionsService.setSubscription(subs, admissionId);
        
        //추가
        dogAssignmentsService.assignDogToClass(admissionId);
        
        redirectAttributes.addFlashAttribute("kakaoDTO", kakaoDTO);
        return "redirect:/kakao/completed";
    }
    
    @GetMapping("/completed")
    public String complete(@ModelAttribute("kakaoDTO") KakaoPayDTO kakaoDTO, Model model) {
    	model.addAttribute("kakaoDTO", kakaoDTO);
    	return "kakaoPay/kakaoPaySuccess";
    }
    
    
    @GetMapping("/cancel")
    public String  cancel() {
    	return "kakaoPay/kakaoPayCancel";
    }
   
}