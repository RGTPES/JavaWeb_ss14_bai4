package org.example.javaweb_ss14_bai4.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.javaweb_ss14_bai4.model.Order;
import org.example.javaweb_ss14_bai4.model.Product;
import org.example.javaweb_ss14_bai4.model.enums.OrderStatus;
import org.example.javaweb_ss14_bai4.repository.OrderRepository;
import org.example.javaweb_ss14_bai4.repository.ProductRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationReleaseScheduler {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredReservations() {
        List<Order> orders = orderRepository.findByStatusAndExpiredAtBefore(
                OrderStatus.PENDING,
                LocalDateTime.now()
        );

        for (Order order : orders) {
            Product product = productRepository.findByIdForUpdate(order.getProductId()).orElse(null);

            if (product != null) {
                product.setStock(product.getStock() + order.getQuantity());
                productRepository.save(product);
            }

            order.setStatus(OrderStatus.EXPIRED);
            orderRepository.save(order);
        }
    }
}