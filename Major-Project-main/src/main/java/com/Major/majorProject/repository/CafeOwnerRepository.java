package com.Major.majorProject.repository;

import com.Major.majorProject.entity.CafeOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CafeOwnerRepository extends JpaRepository<CafeOwner,Long> {
    Optional<CafeOwner> findByEmail(String email);
}
