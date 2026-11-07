package com.scc.model;

public record Customer(int customerId, Integer userId, String fullName, String contactNo, String address, double creditLimit) {}