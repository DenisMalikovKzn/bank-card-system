package com.example.demo.repository;

import com.example.demo.entity.TransferHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransferHistoryRepository extends JpaRepository<TransferHistory, Long> {
    List<TransferHistory> findByFromCard_Owner_IdOrToCard_Owner_Id(Long userId, Long userId2, Pageable pageable);
}
