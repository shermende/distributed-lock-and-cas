package dev.shermende.mps.processing.db.repository;

import dev.shermende.mps.processing.db.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

public interface BalanceRepository extends JpaRepository<Balance, Long> {

    @Query("select e.amount from Balance e where e.id = :id")
    BigInteger getAmount(Long id);

    @Query("select e.version from Balance e where e.id = :id")
    Integer getVersion(Long id);

    @Modifying
    @Transactional
    @Query("update Balance e set " +
            "e.amount = e.amount + :amount, " +
            "e.version = e.version + 1 " +
            "where e.id = :id and e.version = :version")
    int updateBalance(Long id, BigInteger amount, Integer version);
}
