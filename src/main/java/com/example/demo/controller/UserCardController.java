package com.example.demo.controller;

import com.example.demo.service.CardService;
import com.example.demo.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.example.demo.dto.CardFilter;
import com.example.demo.dto.CardResponse;
import com.example.demo.dto.TransferRequest;
import com.example.demo.dto.TransferResponse;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/cards")
@RequiredArgsConstructor
public class UserCardController {
    private final CardService cardService;
    private final TransferService transferService;

    // Просмотр карт с фильтрацией
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<CardResponse>> getUserCards(
            @RequestParam(required = false) CardFilter filter,
            @PageableDefault(sort = "expirationDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //User user = securityUtils.getCurrentUser();
        return ResponseEntity.ok(cardService.getUserCards(user.getId(), filter, pageable));
    }

    // Запрос блокировки карты
    @PostMapping("/{cardId}/block-request")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> requestBlockCard(@PathVariable Long cardId) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        cardService.requestCardBlock(user.getId(), cardId);
        return ResponseEntity.accepted().build();
    }

    // Перевод между картами
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransferResponse> transferBetweenCards(
            @RequestBody @Valid TransferRequest request
    ) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.ok(transferService.transferBetweenUserCards(user.getId(), request));
    }
}
