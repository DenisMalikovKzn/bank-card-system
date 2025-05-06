package com.example.demo.service;

import com.example.demo.dto.CardCreateRequest;
import com.example.demo.dto.CardFilter;
import com.example.demo.dto.CardResponse;
import com.example.demo.enums.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardService {
    CardResponse createCard(CardCreateRequest request);
    Page<CardResponse> getUserCards(Long userId, CardFilter filter, Pageable pageable);
    Page<CardResponse> getAllCards(CardStatus status, Pageable pageable);
    CardResponse changeCardStatus(Long cardId, CardStatus newStatus);
    void deleteCard(Long cardId);
    void requestCardBlock(Long userId, Long cardId);
}

