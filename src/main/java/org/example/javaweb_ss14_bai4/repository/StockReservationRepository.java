package org.example.javaweb_ss14_bai4.repository;

import  org.example.javaweb_ss14_bai4.model.StockReservation;
import  org.example.javaweb_ss14_bai4.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    List<StockReservation> findByOrderId(Long orderId);

    List<StockReservation> findByStatusAndExpiredAtBefore(ReservationStatus status, LocalDateTime now);
}
