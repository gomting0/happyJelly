package com.ex.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ex.entity.AttendanceEntity;
import java.time.LocalDate;


@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceEntity, Integer>{

	List<AttendanceEntity> findByAttendancedate(LocalDate attendancedate);
}
