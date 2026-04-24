package org.example.javaweb_ss14_bai4.scheduler;


import org.example.javaweb_ss14_bai4.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationReleaseScheduler {

    private final CheckoutService checkoutService;

    @Scheduled(fixedDelay = 30000, initialDelay = 10000)
    public void releaseExpiredReservations() {
        log.info("Start scanning expired reservations...");
        checkoutService.releaseExpiredReservations();
        log.info("Finish scanning expired reservations.");
    }
}
