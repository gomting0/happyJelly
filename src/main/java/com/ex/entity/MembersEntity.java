package com.ex.entity;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="Members")

// 시퀀스 생성
@SequenceGenerator(name="members_seq", sequenceName="members_seq", initialValue=1, allocationSize=0)
public class MembersEntity {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="members_seq")
	// Members 테이블 식별번호
	private Integer member_id;
	
	@Column(unique=true, nullable=false)
	// 사용자 아이디
	private String username;
	
	@Column(nullable=false)
	// 사용자 이름
	private String name;
	
	// 사용자 이메일
	private String email;
	
	// 사용자 전화번호
	private String phone;
	
	// 사용자 가입 날짜
	private LocalDate join_date;
	
	// 유저 타입 : check 'REGULAR', 'STAFF'
	private String user_type;
	
	@Column(nullable=false)
	// 사용자 비밀번호
	private String password;
	
	@JsonManagedReference
	@OneToMany(mappedBy="member", cascade=CascadeType.REMOVE)
	private List<DogsEntity> dogs;
	
	
	
}
