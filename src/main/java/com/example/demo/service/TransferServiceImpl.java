package com.example.demo.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.example.demo.dto.TransferRequest;
import com.example.demo.dto.TransferResponse;
import com.example.demo.entity.BankCard;
import com.example.demo.entity.TransferHistory;
import com.example.demo.enums.CardStatus;
import com.example.demo.exceptions.CardNotFoundException;
import com.example.demo.exceptions.IllegalCardOperationException;
import com.example.demo.exceptions.InsufficientFundsException;
import com.example.demo.repository.BankCardRepository;
import com.example.demo.repository.TransferHistoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TransferServiceImpl implements TransferService{
    private final BankCardRepository cardRepository;
    private final TransferHistoryRepository transferHistoryRepository;

    public TransferResponse transferBetweenUserCards(Long userId, TransferRequest request) {
        // Проверка что карты принадлежат пользователю
        List<BankCard> cards = cardRepository.findByOwnerIdAndIdIn(userId,
                List.of(request.fromCardId(), request.toCardId()));

        if (cards.size() != 2) {
            throw new CardNotFoundException("One or both cards not found");
        }

        BankCard fromCard = cards.stream()
                .filter(c -> c.getId().equals(request.fromCardId()))
                .findFirst()
                .orElseThrow();

        BankCard toCard = cards.stream()
                .filter(c -> c.getId().equals(request.toCardId()))
                .findFirst()
                .orElseThrow();

        // Валидация перевода
        validateTransfer(fromCard, toCard, request.amount());

        // Выполнение перевода
        fromCard.setBalance(fromCard.getBalance().subtract(request.amount()));
        toCard.setBalance(toCard.getBalance().add(request.amount()));

        // Сохранение истории
        TransferHistory history = new TransferHistory();
        history.setFromCard(fromCard);
        history.setToCard(toCard);
        history.setAmount(request.amount());
        history.setTimestamp(LocalDateTime.now());
        transferHistoryRepository.save(history);

        return new TransferResponse(
                history.getId(),
                fromCard.getId(),
                toCard.getId(),
                request.amount(),
                history.getTimestamp()
        );
    }

    private void validateTransfer(BankCard fromCard, BankCard toCard, BigDecimal amount) {
        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalCardOperationException("Both cards must be active");
        }

        if (fromCard.getExpirationDate().isBefore(LocalDate.now())) {
            throw new IllegalCardOperationException("Source card is expired");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
    }
}
