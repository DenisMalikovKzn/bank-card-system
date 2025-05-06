package com.example.demo.service;

import com.example.demo.dto.TransferRequest;
import com.example.demo.dto.TransferResponse;
import com.example.demo.entity.BankCard;
import com.example.demo.entity.TransferHistory;
import com.example.demo.entity.User;
import com.example.demo.enums.CardStatus;
import com.example.demo.exceptions.CardNotFoundException;
import com.example.demo.exceptions.IllegalCardOperationException;
import com.example.demo.exceptions.InsufficientFundsException;
import com.example.demo.repository.BankCardRepository;
import com.example.demo.repository.TransferHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {
    @Mock
    private BankCardRepository cardRepository;

    @Mock
    private TransferHistoryRepository transferHistoryRepository;

    @InjectMocks
    private TransferServiceImpl transferService;

    private final User testUser = new User(1L, "user@example.com", "password", new HashSet<>());
    private final BankCard fromCard = BankCard.builder()
            .id(1L)
            .balance(new BigDecimal("1000.00"))
            .status(CardStatus.ACTIVE)
            .expirationDate(LocalDate.now().plusYears(1))
            .owner(testUser)
            .build();

    private final BankCard toCard = BankCard.builder()
            .id(2L)
            .balance(new BigDecimal("500.00"))
            .status(CardStatus.ACTIVE)
            .expirationDate(LocalDate.now().plusYears(2))
            .owner(testUser)
            .build();

    private final TransferRequest validRequest = new TransferRequest(1L, 2L, new BigDecimal("200.00"));

    @Test
    void transferBetweenUserCards_ShouldTransferSuccessfully() {
        // Arrange
        when(cardRepository.findByOwnerIdAndIdIn(eq(1L), anyList()))
                .thenReturn(List.of(fromCard, toCard));
        when(transferHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        TransferResponse response = transferService.transferBetweenUserCards(1L, validRequest);

        // Assert
        assertEquals(new BigDecimal("800.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("700.00"), toCard.getBalance());
        verify(transferHistoryRepository).save(any(TransferHistory.class));
    }

    @Test
    void transferBetweenUserCards_ShouldThrowWhenCardsNotFound() {
        // Arrange
        when(cardRepository.findByOwnerIdAndIdIn(eq(1L), anyList()))
                .thenReturn(List.of(fromCard));

        // Act & Assert
        assertThrows(CardNotFoundException.class,
                () -> transferService.transferBetweenUserCards(1L, validRequest));
    }

    @Test
    void validateTransfer_ShouldThrowWhenCardsNotActive() {
        when(cardRepository.findByOwnerIdAndIdIn(eq(1L), anyList()))
                .thenReturn(List.of(fromCard, toCard));
        // Arrange
        fromCard.setStatus(CardStatus.BLOCKED);

        // Act & Assert
        assertThrows(IllegalCardOperationException.class,
                () -> transferService.transferBetweenUserCards(1L, validRequest));
    }

    @Test
    void validateTransfer_ShouldThrowWhenSourceCardExpired() {
        when(cardRepository.findByOwnerIdAndIdIn(eq(1L), anyList()))
                .thenReturn(List.of(fromCard, toCard));
        // Arrange
        fromCard.setExpirationDate(LocalDate.now().minusDays(1));

        // Act & Assert
        assertThrows(IllegalCardOperationException.class,
                () -> transferService.transferBetweenUserCards(1L, validRequest));
    }

    @Test
    void validateTransfer_ShouldThrowWhenAmountInvalid() {
        when(cardRepository.findByOwnerIdAndIdIn(eq(1L), anyList()))
                .thenReturn(List.of(fromCard, toCard));
        // Arrange
        TransferRequest invalidRequest = new TransferRequest(1L, 2L, new BigDecimal("-100.00"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> transferService.transferBetweenUserCards(1L, invalidRequest));
    }

    @Test
    void validateTransfer_ShouldThrowWhenInsufficientFunds() {
        when(cardRepository.findByOwnerIdAndIdIn(eq(1L), anyList()))
                .thenReturn(List.of(fromCard, toCard));
        // Arrange
        TransferRequest invalidRequest = new TransferRequest(1L, 2L, new BigDecimal("1500.00"));

        // Act & Assert
        assertThrows(InsufficientFundsException.class,
                () -> transferService.transferBetweenUserCards(1L, invalidRequest));
    }

    @Test
    void validateTransfer_ShouldThrowWhenCardsBelongToDifferentUsers() {
        // Arrange
        User otherUser = new User(2L, "other@example.com", "password", new HashSet<>());
        toCard.setOwner(otherUser);

        // Act & Assert
        assertThrows(CardNotFoundException.class,
                () -> transferService.transferBetweenUserCards(1L, validRequest));
    }

    @Test
    void transferHistory_ShouldContainCorrectData() {
        // Arrange
        when(cardRepository.findByOwnerIdAndIdIn(eq(1L), anyList()))
                .thenReturn(List.of(fromCard, toCard));
        ArgumentCaptor<TransferHistory> historyCaptor = ArgumentCaptor.forClass(TransferHistory.class);

        // Act
        transferService.transferBetweenUserCards(1L, validRequest);

        // Assert
        verify(transferHistoryRepository).save(historyCaptor.capture());
        TransferHistory history = historyCaptor.getValue();

        assertEquals(fromCard, history.getFromCard());
        assertEquals(toCard, history.getToCard());
        assertEquals(validRequest.amount(), history.getAmount());
        assertNotNull(history.getTimestamp());
    }

}