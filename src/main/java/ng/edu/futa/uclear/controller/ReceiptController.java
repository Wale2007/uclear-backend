package ng.edu.futa.uclear.controller;

import lombok.RequiredArgsConstructor;
import ng.edu.futa.uclear.model.Receipt;
import ng.edu.futa.uclear.model.Profile;
import ng.edu.futa.uclear.model.Due;
import ng.edu.futa.uclear.repository.ReceiptRepository;
import ng.edu.futa.uclear.repository.ProfileRepository;
import ng.edu.futa.uclear.repository.DueRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptRepository receiptRepository;
    private final ProfileRepository profileRepository;
    private final DueRepository dueRepository;

    /**
     * GET /api/receipts
     * Returns receipts for the authenticated user (from JWT token).
     */
    @GetMapping
    public ResponseEntity<List<Receipt>> getMyReceipts(Authentication auth) {
        String payerId = (String) auth.getPrincipal();
        return ResponseEntity.ok(receiptRepository.findByPayerIdOrderByCreatedAtDesc(payerId));
    }

    /**
     * GET /api/receipts/public/{txRef}
     * Public endpoint — QR code verification. No token needed.
     */
    @GetMapping("/public/{txRef}")
    public ResponseEntity<?> getReceiptByTxRef(@PathVariable String txRef) {
        return receiptRepository.findByTxRef(txRef)
                .<ResponseEntity<?>>map(receipt -> {
                    String payerId = "";
                    if (receipt.getPayer() != null) {
                        payerId = receipt.getPayer().getMatricNo() != null
                                ? receipt.getPayer().getMatricNo()
                                : (receipt.getPayer().getStaffId() != null ? receipt.getPayer().getStaffId() : "");
                    }
                    Map<String, Object> result = new java.util.HashMap<>();
                    result.put("id", receipt.getId());
                    result.put("txRef", receipt.getTxRef());
                    result.put("payerName", receipt.getPayer() != null ? receipt.getPayer().getName() : "Unknown");
                    result.put("payerId", payerId);
                    result.put("duesName", receipt.getDuesName());
                    result.put("category", receipt.getCategory());
                    result.put("amount", receipt.getAmount());
                    result.put("paymentMethod", receipt.getPaymentMethod());
                    result.put("status", receipt.getStatus().name());
                    result.put("date", receipt.getCreatedAt().toString());
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.status(404).body(Map.of("error", "Receipt not found")));
    }

    /**
     * POST /api/receipts
     * Called after Flutterwave payment succeeds — creates a payment record.
     * Body: { "txRef", "payerId", "duesId", "duesName", "category", "amount", "paymentMethod" }
     */
    @PostMapping
    public ResponseEntity<?> createReceipt(@RequestBody Map<String, Object> body, Authentication auth) {
        String payerId = (String) auth.getPrincipal();

        if (receiptRepository.existsByTxRef((String) body.get("txRef"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Receipt with this tx_ref already exists"));
        }

        Profile payer = profileRepository.findById(payerId).orElse(null);
        Due due = dueRepository.findById((String) body.get("duesId")).orElse(null);

        Receipt receipt = new Receipt();
        receipt.setId(UUID.randomUUID().toString());
        receipt.setTxRef((String) body.get("txRef"));
        receipt.setPayer(payer);
        if (payer != null) {
            receipt.setPayerName(payer.getName());
            receipt.setPayerIdentifier(payer.getMatricNo() != null ? payer.getMatricNo() : payer.getStaffId());
        }
        receipt.setDue(due);
        receipt.setDuesName((String) body.get("duesName"));
        receipt.setCategory((String) body.get("category"));
        receipt.setAmount(new BigDecimal(body.get("amount").toString()));
        receipt.setPaymentMethod((String) body.get("paymentMethod"));
        receipt.setStatus(Receipt.Status.successful);
        receipt.setVerified(false);

        Receipt saved = receiptRepository.save(receipt);
        return ResponseEntity.ok(saved);
    }
}
