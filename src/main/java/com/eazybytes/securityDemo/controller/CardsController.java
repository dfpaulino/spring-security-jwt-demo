package com.eazybytes.securityDemo.controller;

import com.eazybytes.securityDemo.model.Cards;
import com.eazybytes.securityDemo.repository.CardsRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CardsController {

    private final CardsRepository cardsRepository;

    public CardsController(CardsRepository cardsRepository) {
        this.cardsRepository = cardsRepository;
    }

    @GetMapping("/myCards")
    public List<Cards> getCardsDetails (@RequestParam long id) {
        return cardsRepository.findByCustomerId(id);
    }

}
