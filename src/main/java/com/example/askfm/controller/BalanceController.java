package com.example.askfm.controller;

import com.example.askfm.dto.DepositRequestDTO;
import com.example.askfm.dto.TransactionDTO;
import com.example.askfm.model.User;
import com.example.askfm.service.TransactionService;
import com.example.askfm.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/balance")
@RequiredArgsConstructor
public class BalanceController {
    private final UserService userService;
    private final TransactionService transactionService;

    @GetMapping
    public String showBalance(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        User user = userService.findByUsername(username);

        // Добавляем информацию о транзакциях
        List<TransactionDTO> buyerTransactions = transactionService.getUserTransactions(username);
        List<TransactionDTO> sellerTransactions = transactionService.getSellerTransactions(username);

        model.addAttribute("currentUser", user);
        model.addAttribute("buyerTransactions", buyerTransactions);
        model.addAttribute("sellerTransactions", sellerTransactions);

        return "balance/view";
    }

    @GetMapping("/transactions")
    public String showTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String type,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        String username = userDetails.getUsername();
        Page<TransactionDTO> transactions = transactionService.getAllTransactionsPaged(username, pageable);
        model.addAttribute("transactions", transactions);
        return "balance/transactions";
    }

    @GetMapping("/transactions/{id}")
    public String showTransactionDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        TransactionDTO transaction = transactionService.getTransactionById(id);
        model.addAttribute("transaction", transaction);
        return "balance/transaction-details";
    }

    @GetMapping("/transactions/filter")
    public String filterTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Model model) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        List<TransactionDTO> transactions = transactionService.getTransactionsByDateRange(startDate, endDate);
        model.addAttribute("transactions", transactions);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "balance/transactions";
    }

    // REST API endpoints для получения данных через AJAX
    @GetMapping("/api/transactions")
    @ResponseBody
    public Page<TransactionDTO> getTransactionsApi(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        return transactionService.getAllTransactionsPaged(pageable);
    }

    @GetMapping("/api/transactions/buyer")
    @ResponseBody
    public List<TransactionDTO> getBuyerTransactions(@AuthenticationPrincipal UserDetails userDetails) {
        return transactionService.getUserTransactions(userDetails.getUsername());
    }

    @GetMapping("/api/transactions/seller")
    @ResponseBody
    public List<TransactionDTO> getSellerTransactions(@AuthenticationPrincipal UserDetails userDetails) {
        return transactionService.getSellerTransactions(userDetails.getUsername());
    }

    @GetMapping("/api/transactions/date-range")
    @ResponseBody
    public List<TransactionDTO> getTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        return transactionService.getTransactionsByDateRange(startDate, endDate);
    }

    @GetMapping("/deposit")
    public String showDepositForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        model.addAttribute("depositRequest", new DepositRequestDTO());
        return "balance/deposit";
    }

    @PostMapping("/deposit")
    public String processDeposit(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute DepositRequestDTO depositRequest,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "balance/deposit";
        }

        try {
            TransactionDTO transaction = transactionService.depositBalance(
                    userDetails.getUsername(),
                    depositRequest
            );
            redirectAttributes.addFlashAttribute("success",
                    "Successfully deposited " + depositRequest.getAmount() + " coins");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to process deposit: " + e.getMessage());
        }

        return "redirect:/balance";
    }
}