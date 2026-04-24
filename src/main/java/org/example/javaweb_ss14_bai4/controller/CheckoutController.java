package org.example.javaweb_ss14_bai4.controller;

import lombok.RequiredArgsConstructor;
import org.example.javaweb_ss14_bai4.service.CartReserveService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartReserveService checkoutService;

    @PostMapping("/reserve")
    public String reserve(@RequestParam Long customerId,
                          @RequestParam Long productId,
                          @RequestParam Integer quantity) {
        return checkoutService.checkout(customerId, productId, quantity);
    }

    @PostMapping("/pay/{orderId}")
    public String pay(@PathVariable Long orderId) {
        return checkoutService.payOrder(orderId);
    }

    @PostMapping("/cancel/{orderId}")
    public String cancel(@PathVariable Long orderId) {
        return checkoutService.cancelOrder(orderId);
    }
}