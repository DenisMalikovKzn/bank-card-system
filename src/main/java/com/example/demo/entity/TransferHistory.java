package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.example.demo.enums.TransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_history")
@Getter
@Setter
public class TransferHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_card_id", nullable = false)
    private BankCard fromCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_card_id", nullable = false)
    private BankCard toCard;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime transactionDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TransferStatus status;

    @Column(length = 500)
    private String description;

    @PrePersist
    protected void onCreate() {
        this.transactionDate = LocalDateTime.now();
        if (this.status == null) {
            this.status = TransferStatus.COMPLETED;
        }
    }

    // Конструкторы, геттеры и сеттеры
    public TransferHistory() {}

    public TransferHistory(BankCard fromCard, BankCard toCard, BigDecimal amount) {
        this.fromCard = fromCard;
        this.toCard = toCard;
        this.amount = amount;
    }
}
