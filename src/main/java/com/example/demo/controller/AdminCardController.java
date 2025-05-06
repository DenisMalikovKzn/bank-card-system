package com.example.demo.controller;

import com.example.demo.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.example.demo.dto.CardCreateRequest;
import com.example.demo.dto.CardResponse;
import com.example.demo.enums.CardStatus;
//import com.example.demo.utils.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class AdminCardController {
    private final CardService cardService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> createCard(@RequestBody @Valid CardCreateRequest request) {
        return ResponseEntity.ok(cardService.createCard(request));
    }

    // Методы для админа
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @RequestParam(required = false) CardStatus status,
            @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(cardService.getAllCards(status, pageable));
    }

    @PatchMapping("/admin/{cardId}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> blockCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.changeCardStatus(cardId, CardStatus.BLOCKED));
    }

    @PatchMapping("/admin/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> activateCard(@PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.changeCardStatus(cardId, CardStatus.ACTIVE));
    }

    @DeleteMapping("/admin/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }
}
