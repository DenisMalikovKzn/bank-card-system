package com.example.demo.dto;

import com.example.demo.enums.CardStatus;
import org.springframework.web.bind.annotation.RequestParam;

public record CardFilter(CardStatus status) {
    public static CardFilter fromRequestParams(
            @RequestParam(required = false) CardStatus status
    ) {
        return new CardFilter(status);
    }
}
