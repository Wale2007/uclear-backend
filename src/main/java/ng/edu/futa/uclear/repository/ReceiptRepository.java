package ng.edu.futa.uclear.repository;

import ng.edu.futa.uclear.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, String> {
    List<Receipt> findByPayerIdOrderByCreatedAtDesc(String payerId);
    Optional<Receipt> findByTxRef(String txRef);
    boolean existsByTxRef(String txRef);
}
