package com.Major.majorProject.repository;

import com.Major.majorProject.entity.PC;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PCRepository extends JpaRepository<PC,Long> {
    List<PC> findByCafeId(Long cafeId);
}
