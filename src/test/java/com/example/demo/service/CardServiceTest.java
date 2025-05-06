package com.example.demo.service;

import com.example.demo.dto.CardCreateRequest;
import com.example.demo.dto.CardFilter;
import com.example.demo.dto.CardResponse;
import com.example.demo.entity.BankCard;
import com.example.demo.entity.User;
import com.example.demo.enums.CardStatus;
import com.example.demo.exceptions.IllegalCardOperationException;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.repository.BankCardRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.EncryptionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private BankCardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @InjectMocks
    private CardServiceImpl cardServiceImpl;

    @Test
    void createCard_ShouldCreateNewCard() {
        User testUser = new User(1L, "user@example.com", "password", new HashSet<>());

        // Генерируем зашифрованные данные
        String mockDecrypted = "4111111111111111";

        String mockEncrypted = "KGt9hc/F2IXGvKNDdVIv3gTtpIOO1sd4qNwFZU8xyIOVNqytUClxkImojrqTcRRC";
        System.out.println(mockEncrypted);

        BankCard testCard = BankCard.builder()
                .id(1L)
                .encryptedCardNumber(mockEncrypted) // Используем зашифрованные данные
                .owner(testUser)
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        CardCreateRequest request = new CardCreateRequest(
                mockDecrypted,
                1L,
                LocalDate.now().plusYears(2)
        );

        // Настройка моков
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(encryptionUtil.encrypt(mockDecrypted)).thenReturn(mockEncrypted);
        //when(encryptionUtil.decrypt(mockEncrypted)).thenReturn(mockDecrypted);
        when(cardRepository.save(any())).thenReturn(testCard);

        // Act
        CardResponse response = cardServiceImpl.createCard(request);

        // Assert
        assertEquals("**** **** **** 1111", response.maskedNumber());
    }


    @Test
    void createCard_ShouldThrowWhenUserNotFound() {
        CardCreateRequest request = new CardCreateRequest(
                "4111111111111111",
                999L,
                LocalDate.now().plusYears(2)
        );

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> cardServiceImpl.createCard(request));
    }

    @Test
    void getUserCards_ShouldFilterByStatus() {
        User testUser = new User(1L, "user@example.com", "password", new HashSet<>());
        String mockEncrypted = "KGt9hc/F2IXGvKNDdVIv3gTtpIOO1sd4qNwFZU8xyIOVNqytUClxkImojrqTcRRC";

        BankCard testCard = BankCard.builder()
                .id(1L)
                .encryptedCardNumber(mockEncrypted) // Используем зашифрованные данные
                .owner(testUser)
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();
        // Arrange
        CardFilter filter = new CardFilter(CardStatus.ACTIVE);
        Pageable pageable = PageRequest.of(0, 10);
        Page<BankCard> mockPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        // Act
        Page<CardResponse> result = cardServiceImpl.getUserCards(1L, filter, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void getAllCards_ShouldReturnAllWhenNoFilter() {
        User testUser = new User(1L, "user@example.com", "password", new HashSet<>());
        String mockEncrypted = "KGt9hc/F2IXGvKNDdVIv3gTtpIOO1sd4qNwFZU8xyIOVNqytUClxkImojrqTcRRC";

        BankCard testCard = BankCard.builder()
                .id(1L)
                .encryptedCardNumber(mockEncrypted) // Используем зашифрованные данные
                .owner(testUser)
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<BankCard> mockPage = new PageImpl<>(List.of(testCard));

        when(cardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(mockPage);

        // Act
        Page<CardResponse> result = cardServiceImpl.getAllCards(null, pageable);

        // Assert
        assertEquals(1, result.getContent().size());
    }

    @Test
    void changeCardStatus_ShouldUpdateStatus() {
        User testUser = new User(1L, "user@example.com", "password", new HashSet<>());
        String mockEncrypted = "KGt9hc/F2IXGvKNDdVIv3gTtpIOO1sd4qNwFZU8xyIOVNqytUClxkImojrqTcRRC";

        BankCard testCard = BankCard.builder()
                .id(1L)
                .encryptedCardNumber(mockEncrypted) // Используем зашифрованные данные
                .owner(testUser)
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any())).thenReturn(testCard);

        // Act
        CardResponse response = cardServiceImpl.changeCardStatus(1L, CardStatus.BLOCKED);

        // Assert
        assertEquals(CardStatus.BLOCKED, response.status());
        verify(cardRepository).save(testCard);
    }

    @Test
    void changeCardStatus_ShouldThrowForExpiredCard() {
        User testUser = new User(1L, "user@example.com", "password", new HashSet<>());
        String mockEncrypted = "KGt9hc/F2IXGvKNDdVIv3gTtpIOO1sd4qNwFZU8xyIOVNqytUClxkImojrqTcRRC";

        BankCard testCard = BankCard.builder()
                .id(1L)
                .encryptedCardNumber(mockEncrypted) // Используем зашифрованные данные
                .owner(testUser)
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();
        testCard.setStatus(CardStatus.EXPIRED);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act & Assert
        assertThrows(IllegalCardOperationException.class,
                () -> cardServiceImpl.changeCardStatus(1L, CardStatus.ACTIVE));
    }

    @Test
    void deleteCard_ShouldDeleteWhenBalanceZero() {
        User testUser = new User(1L, "user@example.com", "password", new HashSet<>());
        String mockEncrypted = "KGt9hc/F2IXGvKNDdVIv3gTtpIOO1sd4qNwFZU8xyIOVNqytUClxkImojrqTcRRC";

        BankCard testCard = BankCard.builder()
                .id(1L)
                .encryptedCardNumber(mockEncrypted) // Используем зашифрованные данные
                .owner(testUser)
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();
        testCard.setBalance(BigDecimal.ZERO);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act
        cardServiceImpl.deleteCard(1L);

        // Assert
        verify(cardRepository).delete(testCard);
    }

    @Test
    void deleteCard_ShouldThrowWhenBalanceNotZero() {
        User testUser = new User(1L, "user@example.com", "password", new HashSet<>());
        String mockEncrypted = "KGt9hc/F2IXGvKNDdVIv3gTtpIOO1sd4qNwFZU8xyIOVNqytUClxkImojrqTcRRC";

        BankCard testCard = BankCard.builder()
                .id(1L)
                .encryptedCardNumber(mockEncrypted) // Используем зашифрованные данные
                .owner(testUser)
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();
        testCard.setBalance(new BigDecimal("100.00"));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // Act & Assert
        assertThrows(IllegalCardOperationException.class,
                () -> cardServiceImpl.deleteCard(1L));
    }

    @Test
    void requestCardBlock_ShouldBlockCard() {
        User testUser = new User(1L, "user@example.com", "password", new HashSet<>());
        String mockEncrypted = "KGt9hc/F2IXGvKNDdVIv3gTtpIOO1sd4qNwFZU8xyIOVNqytUClxkImojrqTcRRC";

        BankCard testCard = BankCard.builder()
                .id(1L)
                .encryptedCardNumber(mockEncrypted) // Используем зашифрованные данные
                .owner(testUser)
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();
        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any())).thenReturn(testCard);

        // Act
        cardServiceImpl.requestCardBlock(1L, 1L);

        // Assert
        assertEquals(CardStatus.BLOCKED, testCard.getStatus());
        verify(cardRepository).save(testCard);
    }

    @Test
    void requestCardBlock_ShouldThrowForExpiredCard() {
        User testUser = new User(1L, "user@example.com", "password", new HashSet<>());
        String mockEncrypted = "KGt9hc/F2IXGvKNDdVIv3gTtpIOO1sd4qNwFZU8xyIOVNqytUClxkImojrqTcRRC";

        BankCard testCard = BankCard.builder()
                .id(1L)
                .encryptedCardNumber(mockEncrypted) // Используем зашифрованные данные
                .owner(testUser)
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();
        testCard.setStatus(CardStatus.EXPIRED);
        when(cardRepository.findByIdAndOwnerId(1L, 1L)).thenReturn(Optional.of(testCard));

        // Act & Assert
        assertThrows(IllegalCardOperationException.class,
                () -> cardServiceImpl.requestCardBlock(1L, 1L));
    }

    @Test
    void mapToResponse_ShouldCorrectlyMapFields() {
        User testUser = new User(1L, "user@example.com", "password", new HashSet<>());
        String mockEncrypted = "KGt9hc/F2IXGvKNDdVIv3gTtpIOO1sd4qNwFZU8xyIOVNqytUClxkImojrqTcRRC";

        BankCard testCard = BankCard.builder()
                .id(1L)
                .encryptedCardNumber(mockEncrypted) // Используем зашифрованные данные
                .owner(testUser)
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();

        // Act
        CardResponse response = cardServiceImpl.mapToResponse(testCard);

        // Assert
        assertEquals(testCard.getId(), response.id());
        assertEquals("**** **** **** 1111", response.maskedNumber());
        assertEquals(testCard.getOwner().getEmail(), response.ownerEmail());
    }
}