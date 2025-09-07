package com.Major.majorProject.repository;

import com.Major.majorProject.entity.PC;
import com.Major.majorProject.entity.UserBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UserBookingRepository extends JpaRepository<UserBooking, Long> {
    List<UserBooking> findByPcIdAndBookingDate(Long pcId, LocalDate bookingDate);
}