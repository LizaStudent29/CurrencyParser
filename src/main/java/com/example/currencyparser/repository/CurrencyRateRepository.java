package com.example.currencyparser.repository;

import com.example.currencyparser.entity.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    List<CurrencyRate> findByDate(LocalDate date);

    Optional<CurrencyRate> findFirstByDateAndCode(LocalDate date, String code);

    List<CurrencyRate> findByCode(String code);
}
