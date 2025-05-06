package com.example.demo.repository;

import com.example.demo.entity.BankCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BankCardRepository extends JpaRepository<BankCard, Long>, JpaSpecificationExecutor<BankCard> {

    @Query("SELECT c FROM BankCard c WHERE c.owner.id = :userId")
    Page<BankCard> findAllByOwnerId(Long userId, Pageable pageable);

    List<BankCard> findByOwnerIdAndIdIn(Long userId, List<Long> cardIds);

    Optional<BankCard> findByIdAndOwnerId(Long cardId, Long userId);
}
