package com.medicarebook.repository;

import com.medicarebook.entity.DoctorProfile;
import com.medicarebook.entity.DoctorStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<DoctorProfile, Long> {

    Optional<DoctorProfile> findByUserId(Long userId);

    List<DoctorProfile> findByStatus(DoctorStatus status);

    Page<DoctorProfile> findByStatus(DoctorStatus status, Pageable pageable);

    @Query("SELECT d FROM DoctorProfile d WHERE d.status = :status AND " +
           "(LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(d.specialization) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<DoctorProfile> searchApprovedDoctors(@Param("status") DoctorStatus status, @Param("query") String query);

    void deleteByUserId(Long userId);
}
