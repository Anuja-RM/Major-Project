package com.Major.majorProject.repository;

import com.Major.majorProject.entity.Cafe;
import com.Major.majorProject.entity.PC;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PCRepository extends JpaRepository<PC,Long> {
    Optional<List<PC>> findAllByCafe(Cafe cafe);
}
