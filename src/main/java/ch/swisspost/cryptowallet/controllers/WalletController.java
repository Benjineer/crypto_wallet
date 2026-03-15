package ch.swisspost.cryptowallet.controllers;

import ch.swisspost.cryptowallet.dtos.BuyRequest;
import ch.swisspost.cryptowallet.dtos.WalletPerformanceResponse;
import ch.swisspost.cryptowallet.dtos.WalletResponse;
import ch.swisspost.cryptowallet.dtos.WalletValueResponse;
import ch.swisspost.cryptowallet.services.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<WalletResponse> createWallet(@AuthenticationPrincipal(expression = "username") String userId) {
        return ResponseEntity.created(URI.create("/api/v1/wallet/balance")).body(walletService.createWallet(userId));
    }

    @PostMapping("/asset/buy")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<Void> buyAsset(@AuthenticationPrincipal(expression = "username") String userId,
                                         @RequestBody BuyRequest request) {
        walletService.buyAsset(userId, request.symbol(), request.quantity());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/balance")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<WalletValueResponse> getCurrentBalance(@AuthenticationPrincipal(expression = "username") String userId) {
        return ResponseEntity.ok(walletService.getCurrentWalletValue(userId));
    }

    @GetMapping("/balance/historical")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<BigDecimal> getHistoricalBalance(
            @AuthenticationPrincipal(expression = "username") String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(walletService.getHistoricalValue(userId, date));
    }

    @GetMapping("/performance")
    @PreAuthorize("hasAuthority('user:read')")
    public ResponseEntity<WalletPerformanceResponse> getPerformance(
            @AuthenticationPrincipal(expression = "username") String userId) {
        return ResponseEntity.ok(walletService.getPerformance(userId));
    }
}