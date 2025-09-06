package com.Major.majorProject.repository;

import com.Major.majorProject.entity.Cafe;
import com.Major.majorProject.entity.CafeOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CafeRepository extends JpaRepository<Cafe,Long> {
    List<Cafe> findAllByOwner(CafeOwner owner);
}
