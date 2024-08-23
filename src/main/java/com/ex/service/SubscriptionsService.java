package com.ex.service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.ex.data.KakaoPayDTO;
import com.ex.data.SubscriptionsDTO;
import com.ex.entity.AdmissionsEntity;
import com.ex.entity.DogsEntity;
import com.ex.entity.MembersEntity;
import com.ex.entity.SubscriptionsEntity;
import com.ex.repository.AdmissionsRepository;
import com.ex.repository.MembersRepository;
import com.ex.repository.SubscriptionsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SubscriptionsService {

	private final SubscriptionsRepository subscriptionsRepository;
	private final AdmissionsRepository admissionsRepository;
	private final MembersRepository membersRepository;
	
	public SubscriptionsEntity createSubscription(String username, Integer admissionId, KakaoPayDTO kakaoDTO) {
		MembersEntity me = membersRepository.findByUsername(username).get();
		AdmissionsEntity ae = admissionsRepository.findById(admissionId).get();
		
		LocalDate today = LocalDate.now();
		LocalDate nextMonthFirstDay = today.plusMonths(1).withDayOfMonth(1);
		LocalDate nextMonthLastDay = nextMonthFirstDay.withDayOfMonth(nextMonthFirstDay.lengthOfMonth());
		
		String auto = null;
		if(kakaoDTO.getAuto()==null) {
			auto = "N";
		}else {
			auto = "Y";
		}
		
		SubscriptionsEntity se = SubscriptionsEntity.builder().startDate(nextMonthFirstDay).endDate(nextMonthLastDay).admissions(ae)
				.autoRenewal(auto).status("ACTIVE").paymentDate(kakaoDTO.getCreated_at()).dogs(ae.getDogs()).member(me)
				.amount(Integer.parseInt(kakaoDTO.getTotal_amount())).paymethod(kakaoDTO.getPayment_method_type())
				.ticket(ae.getMonthcaregroups().getTicket()).build();
		subscriptionsRepository.save(se);
		return se;
	}
	
	public List<SubscriptionsDTO> mysubsInfo(String username){
		List<SubscriptionsDTO> subsDTOList = new ArrayList<>();
		SubscriptionsDTO subDTO = null;
		MembersEntity me = membersRepository.findByUsername(username).get();
		List<DogsEntity> de = me.getDogs();
		for(DogsEntity dog : de) {
			List<AdmissionsEntity> ae = dog.getAdmissions();
			for(AdmissionsEntity admissions : ae) {
				Optional <SubscriptionsEntity> op = subscriptionsRepository.findByAdmissions(admissions);
				if(op.isPresent()) {
					SubscriptionsEntity subs = op.get();
					subDTO = new SubscriptionsDTO().builder().subscriptionId(subs.getSubscriptionId())
							.startDate(subs.getStartDate()).endDate(subs.getEndDate()).autoRenewal(subs.getAutoRenewal())
							.status(subs.getStatus()).usageCount(subs.getUsageCount()).amount(subs.getAmount())
							.paymentDate(subs.getPaymentDate()).paymethod(subs.getPaymethod()).refund(subs.getRefund())
							.admissions(subs.getAdmissions()).ticket(subs.getTicket()).dogs(subs.getDogs())
							.member(subs.getMember()).build();
					subsDTOList.add(subDTO);
				}
			}
		}		
		return subsDTOList.stream()
	            .sorted(Comparator.comparing(SubscriptionsDTO::getPaymentDate).reversed())
	            .collect(Collectors.toList());
	} 
	
	@Scheduled(cron = "0 0 0 1 * ?")
	public void subsExpired() {
		LocalDate today = LocalDate.now();
		LocalDate lastMonthFirstDay = today.minusMonths(1).withDayOfMonth(1);
		List<SubscriptionsEntity> subscriptionsList = subscriptionsRepository.findByStartDate(lastMonthFirstDay);
		if(!subscriptionsList.isEmpty()) {
			for(SubscriptionsEntity subs : subscriptionsList) {
				if(subs.getRefund()==null) {
					subs.setStatus("EXPIRED");
					subscriptionsRepository.save(subs);
				}				
			}
		}		
	}
}
