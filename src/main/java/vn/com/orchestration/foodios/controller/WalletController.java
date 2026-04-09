package vn.com.orchestration.foodios.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.wallet.GetWalletTransactionsResponse;
import vn.com.orchestration.foodios.dto.wallet.TopUpRequest;
import vn.com.orchestration.foodios.dto.wallet.WalletResponse;
import vn.com.orchestration.foodios.exception.ResponseUtil;
import vn.com.orchestration.foodios.service.wallet.WalletService;
import vn.com.orchestration.foodios.utils.HttpUtils;

import static vn.com.orchestration.foodios.constant.ApiConstant.API_PATH;
import static vn.com.orchestration.foodios.constant.ApiConstant.API_VERSION;

@RestController
@RequestMapping(API_PATH + API_VERSION + "/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/balance")
    public ResponseEntity<WalletResponse> getBalance(HttpServletRequest request) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        WalletResponse response = walletService.getOrCreateWallet(baseRequest);
        return HttpUtils.buildResponse(baseRequest, response);
    }

    @PostMapping("/top-up")
    public ResponseEntity<WalletResponse> topUp(@Valid @RequestBody TopUpRequest request) {
        WalletResponse response = walletService.topUp(request);
        return HttpUtils.buildResponse(request, response);
    }

    @GetMapping("/transactions")
    public ResponseEntity<GetWalletTransactionsResponse> getTransactions(HttpServletRequest request) {
        BaseRequest baseRequest = ResponseUtil.getBaseRequestOrDefault(request);
        GetWalletTransactionsResponse response = walletService.getTransactions(baseRequest);
        return HttpUtils.buildResponse(baseRequest, response);
    }
}
