package org.example.javaweb_ss14_bai4.service;

import org.example.javaweb_ss14_bai4.entity.Order;
import org.example.javaweb_ss14_bai4.entity.Product;
import org.example.javaweb_ss14_bai4.entity.StockReservation;
import org.example.javaweb_ss14_bai4.entity.enums.OrderStatus;
import org.example.javaweb_ss14_bai4.entity.enums.ReservationStatus;
import org.example.javaweb_ss14_bai4.exception.BusinessException;
import org.example.javaweb_ss14_bai4.repository.OrderRepository;
import org.example.javaweb_ss14_bai4.repository.ProductRepository;
import org.example.javaweb_ss14_bai4.repository.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final StockReservationRepository reservationRepository;

    @Transactional
    public Order checkout(Long customerId, Long productId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("Số lượng phải > 0");
        }

        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy sản phẩm"));

        if (Boolean.TRUE.equals(product.getDeleted())) {
            throw new BusinessException("Sản phẩm đã ngừng bán");
        }

        if (product.getStock() < quantity) {
            throw new BusinessException("Không đủ tồn kho");
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plusMinutes(15);

        Order order = Order.builder()
                .customerId(customerId)
                .status(OrderStatus.PENDING)
                .createdAt(now)
                .expiredAt(expiredAt)
                .build();
        orderRepository.save(order);

        StockReservation reservation = StockReservation.builder()
                .orderId(order.getId())
                .productId(productId)
                .quantity(quantity)
                .status(ReservationStatus.RESERVED)
                .reservedAt(now)
                .expiredAt(expiredAt)
                .productDeletedSnapshot(product.getDeleted())
                .build();
        reservationRepository.save(reservation);

        return order;
    }

    @Transactional
    public void payOrder(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy order"));

        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.EXPIRED) {
            throw new BusinessException("Order đã bị hủy hoặc hết hạn");
        }

        if (order.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Đã quá thời gian thanh toán 15 phút");
        }

        List<StockReservation> reservations = reservationRepository.findByOrderId(orderId);
        for (StockReservation reservation : reservations) {
            if (reservation.getStatus() != ReservationStatus.RESERVED) {
                throw new BusinessException("Reservation không còn hợp lệ");
            }
            reservation.setStatus(ReservationStatus.COMPLETED);
        }

        order.setStatus(OrderStatus.PAID);

        reservationRepository.saveAll(reservations);
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy order"));

        if (order.getStatus() == OrderStatus.PAID) {
            throw new BusinessException("Order đã thanh toán, không thể hủy");
        }

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.EXPIRED) {
            return;
        }

        List<StockReservation> reservations = reservationRepository.findByOrderId(orderId);

        for (StockReservation reservation : reservations) {
            if (reservation.getStatus() == ReservationStatus.RESERVED) {
                Product product = productRepository.findByIdForUpdate(reservation.getProductId())
                        .orElse(null);

                if (product != null) {
                    product.setStock(product.getStock() + reservation.getQuantity());
                    productRepository.save(product);
                }

                reservation.setStatus(ReservationStatus.RELEASED);
            }
        }

        order.setStatus(OrderStatus.CANCELLED);

        reservationRepository.saveAll(reservations);
        orderRepository.save(order);
    }


    @Transactional
    public void releaseExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<StockReservation> expiredReservations =
                reservationRepository.findByStatusAndExpiredAtBefore(ReservationStatus.RESERVED, now);

        for (StockReservation reservation : expiredReservations) {
            Order order = orderRepository.findByIdForUpdate(reservation.getOrderId()).orElse(null);

            if (order != null && order.getStatus() == OrderStatus.PAID) {
                reservation.setStatus(ReservationStatus.COMPLETED);
                reservationRepository.save(reservation);
                continue;
            }

            Product product = productRepository.findByIdForUpdate(reservation.getProductId()).orElse(null);

            if (product != null) {
                product.setStock(product.getStock() + reservation.getQuantity());
                productRepository.save(product);
            }

            reservation.setStatus(ReservationStatus.EXPIRED);
            reservationRepository.save(reservation);

            if (order != null && order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.EXPIRED);
                orderRepository.save(order);
            }
        }
    }
}
