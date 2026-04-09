package vn.com.orchestration.foodios.service.wallet;

import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.wallet.GetWalletTransactionsResponse;
import vn.com.orchestration.foodios.dto.wallet.TopUpRequest;
import vn.com.orchestration.foodios.dto.wallet.WalletResponse;

public interface WalletService {
    WalletResponse getOrCreateWallet(BaseRequest request);
    WalletResponse topUp(TopUpRequest request);
    GetWalletTransactionsResponse getTransactions(BaseRequest request);
    GetWalletTransactionsResponse getAllTransactions(BaseRequest request);
}
