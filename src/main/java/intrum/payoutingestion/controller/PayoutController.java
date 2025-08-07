package intrum.payoutingestion.controller;

import intrum.payoutingestion.services.PayoutProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payout")
@RequiredArgsConstructor
@Slf4j
public class PayoutController {

    private final PayoutProcessor payoutProcessor;

    @PostMapping("/process")
    public ResponseEntity<String> triggerProcessing() {
        log.info("Manual trigger of payout processing");
        payoutProcessor.process();
        log.info("Payout processing triggered successfully");
        return ResponseEntity.ok("Payout processing triggered successfully");
    }
}