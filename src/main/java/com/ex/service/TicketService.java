package com.ex.service;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.ex.data.TicketDTO;
import com.ex.entity.TicketEntity;
import com.ex.repository.TicketRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketService {
	
	@Autowired
	private final TicketRepository ticketRepository;
	
	public Page<TicketEntity> getTicketsAll(int page){
		List<Sort.Order> sorts = new ArrayList<>();
		sorts.add((Sort.Order.desc("id").desc("salesstatus")));
		Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
		return ticketRepository.findAll(pageable);
	}
	
	// 판매중인 이용권 티켓만 목록 조회 (판매상태=1)
	public List<TicketDTO> getTicketsList(){
		List<TicketDTO> list = null;
		TicketDTO tDto = null;
		List<TicketEntity> dbTicketList = ticketRepository.findBySalesstatus(1);
		list = new ArrayList<>(dbTicketList.size());
		for(TicketEntity t: dbTicketList) {
			tDto = new TicketDTO().builder()
					.id(t.getId())
					.ticketname(t.getTicketname())
					.price(t.getPrice())
					.groupType(t.getGroupType())
					.salesstatus(t.getSalesstatus())
					.dayofweek(t.getDayofweek())
					.ticketcount(t.getTicketcount())
					.build();
			
			list.add(tDto);
		}
		return list;
	}
	
	// 이용권 상세 조회
	public TicketDTO getTicketById(Integer id) {
		TicketEntity te = ticketRepository.findById(id).get();
		TicketDTO ticketDto = TicketDTO.builder()
							.id(te.getId())
							.ticketname(te.getTicketname())
							.price(te.getPrice())
							.groupType(te.getGroupType())
							.salesstatus(te.getSalesstatus())
							.dayofweek(te.getDayofweek())
							.ticketcount(te.getTicketcount())
							.build();
		return ticketDto;
		
	}
	
	// 이용권 추가
	public void addTicket(TicketDTO ticketDTO) {
		String day;
		if (ticketDTO.getGroupType().equals("DAYCARE")) {
		    day = null;
		} else {
			day = ticketDTO.getDayofweek();
		}
        TicketEntity ticket = TicketEntity.builder()
        						.ticketname(ticketDTO.getTicketname())
						        .price(ticketDTO.getPrice())
						        .groupType(ticketDTO.getGroupType())
						        .salesstatus(1)
						        .dayofweek(day)
						        .ticketcount(ticketDTO.getTicketcount())
						        .build();
        
        ticketRepository.save(ticket);
    }
	
	// 이용권 수정 (판매상태만 변경)
	public void updateTicket(TicketDTO ticketDTO, Integer id) {
		TicketEntity te = ticketRepository.findById(id).orElseThrow(() -> new RuntimeException("Ticket not found"));
//		te.setTicketname(ticketDTO.getTicketname());
//		te.setPrice(ticketDTO.getPrice());
//		te.setGroupType(ticketDTO.getGroupType());
		te.setSalesstatus(ticketDTO.getSalesstatus());
//		te.setDayofweek(ticketDTO.getDayofweek());
		
		ticketRepository.save(te);
	}

}
