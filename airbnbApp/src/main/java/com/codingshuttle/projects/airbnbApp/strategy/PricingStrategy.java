package com.codingshuttle.projects.airbnbApp.strategy;

import com.codingshuttle.projects.airbnbApp.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);
}
