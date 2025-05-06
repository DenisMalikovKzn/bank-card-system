package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import com.example.demo.enums.CardStatus;
import com.example.demo.utils.EncryptionUtil;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "bank_cards")
@Getter
@Setter
@Builder
@AllArgsConstructor
public class BankCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String encryptedCardNumber;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @Column(nullable = false)
    private BigDecimal balance;

    public BankCard() {

    }

    // Getters and Setters

    public String getMaskedNumber() {
        EncryptionUtil encryptionUtil = new EncryptionUtil();
        String decrypted = encryptionUtil.decrypt(encryptedCardNumber);
        return "**** **** **** " + decrypted.substring(decrypted.length() - 4);
    }

    // Автоматическая проверка срока действия
    @PrePersist
    @PreUpdate
    public void checkExpiration() {
        if (expirationDate.isBefore(LocalDate.now())) {
            this.status = CardStatus.EXPIRED;
        }
    }
}
