package org.example.javaweb_ss14_bai4.controller;

import org.example.javaweb_ss14_bai4.entity.Order;
import org.example.javaweb_ss14_bai4.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping("/reserve")
    public String reserve(@RequestParam Long customerId,
                          @RequestParam Long productId,
                          @RequestParam Integer quantity) {
        Order order = checkoutService.checkout(customerId, productId, quantity);
        return "Reserve thành công. Order ID = " + order.getId();
    }

    @PostMapping("/pay/{orderId}")
    public String pay(@PathVariable Long orderId) {
        checkoutService.payOrder(orderId);
        return "Thanh toán thành công cho order " + orderId;
    }

    @PostMapping("/cancel/{orderId}")
    public String cancel(@PathVariable Long orderId) {
        checkoutService.cancelOrder(orderId);
        return "Hủy order thành công: " + orderId;
    }
}
