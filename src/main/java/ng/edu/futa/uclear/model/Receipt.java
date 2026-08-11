package ng.edu.futa.uclear.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "tx_ref", unique = true, nullable = false, length = 100)
    private String txRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id")
    private Profile payer;

    @Column(name = "payer_name", length = 200)
    private String payerName;

    @Column(name = "payer_identifier", length = 100)
    private String payerIdentifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dues_id")
    private Due due;

    @Column(name = "dues_name", length = 200)
    private String duesName;

    @Column(length = 100)
    private String category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    private Status status = Status.successful;

    private Boolean verified = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = Status.successful;
        if (this.verified == null) this.verified = false;
    }

    public enum Status {
        pending, successful, failed
    }
}
