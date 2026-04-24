package org.example.javaweb_ss14_bai4.service;

import lombok.RequiredArgsConstructor;
import org.example.javaweb_ss14_bai4.model.Order;
import org.example.javaweb_ss14_bai4.model.Product;
import org.example.javaweb_ss14_bai4.model.enums.OrderStatus;
import org.example.javaweb_ss14_bai4.repository.OrderRepository;
import org.example.javaweb_ss14_bai4.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CartReserveService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public String checkout(Long customerId, Long productId, Integer quantity) {
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new RuntimeException("San pham khong ton tai"));

        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("So luong khong hop le");
        }

        if (product.getStock() == null || product.getStock() < quantity) {
            return "Het hang";
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);

        Order order = Order.builder()
                .customerId(customerId)
                .productId(productId)
                .quantity(quantity)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .build();

        orderRepository.save(order);

        return "Giu hang thanh cong. Order ID = " + order.getId();
    }

    @Transactional
    public String payOrder(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new RuntimeException("Don hang khong ton tai"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Don hang khong o trang thai PENDING");
        }

        if (order.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Don hang da het han");
        }

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        return "Thanh toan thanh cong cho order " + orderId;
    }

    @Transactional
    public String cancelOrder(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new RuntimeException("Don hang khong ton tai"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Chi huy duoc don PENDING");
        }

        Product product = productRepository.findByIdForUpdate(order.getProductId())
                .orElseThrow(() -> new RuntimeException("San pham khong ton tai"));

        product.setStock(product.getStock() + order.getQuantity());
        productRepository.save(product);

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        return "Huy order thanh cong: " + orderId;
    }
}