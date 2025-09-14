package com.Major.majorProject.repository;

import com.Major.majorProject.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Long> {
    List<Slot> findByPcId(Long pcId);
}