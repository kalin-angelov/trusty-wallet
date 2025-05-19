package app.transaction.repository;

import app.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findAllTransactionByOwnerId(UUID ownerId);

    List<Transaction> findAllBySenderOrReceiver(String sender, String receiver);

    Optional<Transaction> findByIdAndOwnerId(UUID id, UUID ownerId);
}
