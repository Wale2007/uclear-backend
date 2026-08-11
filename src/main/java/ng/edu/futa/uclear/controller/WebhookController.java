package ng.edu.futa.uclear.controller;

import lombok.RequiredArgsConstructor;
import ng.edu.futa.uclear.model.Due;
import ng.edu.futa.uclear.model.Profile;
import ng.edu.futa.uclear.model.Receipt;
import ng.edu.futa.uclear.repository.DueRepository;
import ng.edu.futa.uclear.repository.ProfileRepository;
import ng.edu.futa.uclear.repository.ReceiptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles Flutterwave payment webhook callbacks.
 *
 * Flutterwave sends a POST request to this endpoint when a payment is completed.
 * We verify the secret hash before recording the payment — this prevents fake callbacks.
 */
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final ReceiptRepository receiptRepository;
    private final ProfileRepository profileRepository;
    private final DueRepository dueRepository;

    @Value("${app.flutterwave.secret-hash}")
    private String flwSecretHash;

    /**
     * POST /api/webhooks/flutterwave
     * Flutterwave sends payment events here.
     */
    @PostMapping("/flutterwave")
    public ResponseEntity<?> handleFlutterwaveWebhook(
            @RequestHeader(value = "verif-hash", required = false) String verifHash,
            @RequestBody Map<String, Object> payload) {

        // 1. Verify the webhook signature
        if (verifHash == null || !verifHash.equals(flwSecretHash)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid webhook signature"));
        }

        // 2. Only handle successful charge events
        String event = (String) payload.get("event");
        if (!"charge.completed".equals(event)) {
            return ResponseEntity.ok(Map.of("message", "Event ignored: " + event));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) payload.get("data");
        if (data == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing data payload"));
        }

        String status = (String) data.get("status");
        String txRef  = (String) data.get("tx_ref");

        if (!"successful".equals(status) || txRef == null) {
            return ResponseEntity.ok(Map.of("message", "Payment not successful, ignored."));
        }

        // 3. Prevent duplicate processing
        if (receiptRepository.existsByTxRef(txRef)) {
            return ResponseEntity.ok(Map.of("message", "Receipt already exists for txRef: " + txRef));
        }

        // 4. Parse txRef to find payer: format is EDUES-{matricOrStaffId}-{timestamp}
        String payerId = extractPayerIdFromTxRef(txRef);

        // 5. Record the verified receipt
        Receipt receipt = new Receipt();
        receipt.setId(UUID.randomUUID().toString());
        receipt.setTxRef(txRef);
        receipt.setDuesName((String) data.getOrDefault("narration", "Due Payment"));
        receipt.setAmount(new BigDecimal(data.get("amount").toString()));
        receipt.setPaymentMethod((String) data.getOrDefault("payment_type", "card"));
        receipt.setStatus(Receipt.Status.successful);
        receipt.setVerified(true); // Server-verified via webhook

        // Try to link payer profile
        profileRepository.findByMatricNo(payerId)
                .or(() -> profileRepository.findByStaffId(payerId))
                .ifPresent(p -> {
                    receipt.setPayer(p);
                    receipt.setPayerName(p.getName());
                    receipt.setPayerIdentifier(p.getMatricNo() != null ? p.getMatricNo() : p.getStaffId());
                });

        receiptRepository.save(receipt);

        return ResponseEntity.ok(Map.of("message", "Webhook received and receipt recorded."));
    }

    private String extractPayerIdFromTxRef(String txRef) {
        // txRef format: EDUES-SEN/22/9292-1722960000000
        // Extract the middle part
        try {
            String[] parts = txRef.split("-", 3);
            return parts.length >= 2 ? parts[1] : txRef;
        } catch (Exception e) {
            return txRef;
        }
    }
}
