package com.example.demo.service;

import com.example.demo.dto.TransferRequest;
import com.example.demo.dto.TransferResponse;


public interface TransferService {
    TransferResponse transferBetweenUserCards(Long userId, TransferRequest request);
}