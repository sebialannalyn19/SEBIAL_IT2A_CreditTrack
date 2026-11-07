package com.scc.model;

public record CreditSale(int saleId, int customerId, int userId, double amount, String description, String saleDate, String status) {}