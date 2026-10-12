package com.medicarebook.repository;

import com.medicarebook.entity.Appointment;
import com.medicarebook.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByUserIdOrderByAppointmentDateDescCreatedAtDesc(Long userId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateDescCreatedAtDesc(Long doctorId);

    List<Appointment> findAllByOrderByCreatedAtDesc();

    // Check if slot is already occupied (status NOT CANCELLED)
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND a.appointmentDate = :date AND a.appointmentTime = :time " +
           "AND a.status <> :cancelledStatus")
    boolean existsByDoctorSlotConflict(
            @Param("doctorId") Long doctorId,
            @Param("date") LocalDate date,
            @Param("time") String time,
            @Param("cancelledStatus") AppointmentStatus cancelledStatus
    );

    @Modifying
    @Query("DELETE FROM Appointment a WHERE a.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Appointment a WHERE a.doctor.id = :doctorId")
    void deleteByDoctorId(@Param("doctorId") Long doctorId);
}
