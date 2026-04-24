package org.example.javaweb_ss14_bai4.repository;

import  org.example.javaweb_ss14_bai4.entity.StockReservation;
import  org.example.javaweb_ss14_bai4.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    List<StockReservation> findByOrderId(Long orderId);

    List<StockReservation> findByStatusAndExpiredAtBefore(ReservationStatus status, LocalDateTime now);
}
