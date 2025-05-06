package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import com.example.demo.dto.CardCreateRequest;
import com.example.demo.dto.CardFilter;
import com.example.demo.dto.CardResponse;
import com.example.demo.entity.BankCard;
import com.example.demo.entity.User;
import com.example.demo.enums.CardStatus;
import com.example.demo.exceptions.CardNotFoundException;
import com.example.demo.exceptions.IllegalCardOperationException;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.repository.BankCardRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.EncryptionUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final BankCardRepository cardRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    public CardResponse createCard(CardCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        BankCard card = new BankCard();
        card.setEncryptedCardNumber(encryptionUtil.encrypt(request.cardNumber()));
        card.setOwner(user);
        card.setExpirationDate(request.expirationDate());
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        return mapToResponse(cardRepository.save(card));
    }

    public Page<CardResponse> getUserCards(Long userId, CardFilter filter, Pageable pageable) {
        Specification<BankCard> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("owner").get("id"), userId));

        if (filter.status() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), filter.status()));
        }

        return cardRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    public Page<CardResponse> getAllCards(CardStatus status, Pageable pageable) {
        Specification<BankCard> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        return cardRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    public CardResponse changeCardStatus(Long cardId, CardStatus newStatus) {
        BankCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new IllegalCardOperationException("Cannot change status for expired card");
        }

        card.setStatus(newStatus);
        return mapToResponse(cardRepository.save(card));
    }

    public void deleteCard(Long cardId) {
        BankCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (card.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalCardOperationException("Cannot delete card with non-zero balance");
        }

        cardRepository.delete(card);
    }

    CardResponse mapToResponse(BankCard card) {
        return new CardResponse(
                card.getId(),
                card.getMaskedNumber(),
                card.getOwner().getEmail(),
                card.getExpirationDate(),
                card.getStatus(),
                card.getBalance()
        );
    }

    // Запрос блокировки
    public void requestCardBlock(Long userId, Long cardId) {
        BankCard card = cardRepository.findByIdAndOwnerId(cardId, userId)
                .orElseThrow(() -> new CardNotFoundException("Card not found or access denied"));

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new IllegalCardOperationException("Cannot block expired card");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

}
