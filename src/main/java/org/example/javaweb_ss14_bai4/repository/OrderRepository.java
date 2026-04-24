package org.example.javaweb_ss14_bai4.repository;

import org.example.javaweb_ss14_bai4.model.Order;
import org.example.javaweb_ss14_bai4.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findByIdForUpdate(Long id);
    List<Order> findByStatusAndExpiredAtBefore(OrderStatus status, LocalDateTime time);
}
