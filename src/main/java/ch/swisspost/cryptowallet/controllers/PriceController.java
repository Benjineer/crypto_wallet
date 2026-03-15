package ch.swisspost.cryptowallet.controllers;

import ch.swisspost.cryptowallet.entities.PriceHistory;
import ch.swisspost.cryptowallet.services.PriceService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/prices")
public class PriceController {

    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping("asset/{symbol}")
    @PreAuthorize("hasAuthority('user:read')")
    public Page<PriceHistory> getPriceHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return priceService.getPriceHistory(symbol, page, size);
    }
}