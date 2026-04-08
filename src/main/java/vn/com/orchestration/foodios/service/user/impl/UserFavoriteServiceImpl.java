package vn.com.orchestration.foodios.service.user.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.orchestration.foodios.dto.common.ApiResult;
import vn.com.orchestration.foodios.dto.common.BaseRequest;
import vn.com.orchestration.foodios.dto.merchant.GetMerchantsResponse;
import vn.com.orchestration.foodios.dto.user.GetFavoriteMerchantsResponse;
import vn.com.orchestration.foodios.dto.user.ToggleFavoriteRequest;
import vn.com.orchestration.foodios.dto.user.ToggleFavoriteResponse;
import vn.com.orchestration.foodios.entity.merchant.Merchant;
import vn.com.orchestration.foodios.entity.user.User;
import vn.com.orchestration.foodios.entity.user.UserFavoriteMerchant;
import vn.com.orchestration.foodios.exception.BusinessException;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContext;
import vn.com.orchestration.foodios.jwt.identity.IdentityUserContextProvider;
import vn.com.orchestration.foodios.repository.MerchantRepository;
import vn.com.orchestration.foodios.repository.UserFavoriteMerchantRepository;
import vn.com.orchestration.foodios.repository.UserRepository;
import vn.com.orchestration.foodios.service.user.UserFavoriteService;
import vn.com.orchestration.foodios.utils.ExceptionUtils;

import java.util.UUID;

import static vn.com.orchestration.foodios.constant.ErrorConstant.*;

@Service
@RequiredArgsConstructor
public class UserFavoriteServiceImpl implements UserFavoriteService {

    private final UserFavoriteMerchantRepository favoriteRepository;
    private final MerchantRepository merchantRepository;
    private final UserRepository userRepository;
    private final IdentityUserContextProvider identityUserContextProvider;

    @Override
    @Transactional
    public ToggleFavoriteResponse toggleFavorite(ToggleFavoriteRequest request) {
        User user = getCurrentUser(request);
        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "Merchant not found"));

        var favoriteOpt = favoriteRepository.findByUserIdAndMerchantId(user.getId(), merchant.getId());
        boolean isFavorite;
        String message;

        if (favoriteOpt.isPresent()) {
            favoriteRepository.delete(favoriteOpt.get());
            isFavorite = false;
            message = "Removed from favorites";
        } else {
            UserFavoriteMerchant favorite = UserFavoriteMerchant.builder()
                    .user(user)
                    .merchant(merchant)
                    .build();
            favoriteRepository.save(favorite);
            isFavorite = true;
            message = "Added to favorites";
        }

        return ToggleFavoriteResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(ToggleFavoriteResponse.ToggleFavoriteResponseData.builder()
                        .isFavorite(isFavorite)
                        .message(message)
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetFavoriteMerchantsResponse getFavorites(BaseRequest request, Integer pageNumber, Integer pageSize) {
        User user = getCurrentUser(request);
        validatePagination(request, pageNumber, pageSize);

        PageRequest pageable = PageRequest.of(
                pageNumber - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<UserFavoriteMerchant> favorites = favoriteRepository.findByUserId(user.getId(), pageable);

        return GetFavoriteMerchantsResponse.builder()
                .result(ApiResult.builder().responseCode(SUCCESS_CODE).description(SUCCESS_MESSAGE).build())
                .data(GetFavoriteMerchantsResponse.GetFavoriteMerchantsResponseData.builder()
                        .items(favorites.getContent().stream().map(f -> mapToMerchantPayload(f.getMerchant())).toList())
                        .pageNumber(pageNumber)
                        .pageSize(pageSize)
                        .totalItems(favorites.getTotalElements())
                        .totalPages(favorites.getTotalPages())
                        .hasNext(favorites.hasNext())
                        .build())
                .build();
    }

    private User getCurrentUser(BaseRequest request) {
        IdentityUserContext currentUser = identityUserContextProvider.requireCurrentUser();
        return userRepository.findById(UUID.fromString(currentUser.subject()))
                .orElseThrow(() -> businessException(request, RECORD_NOT_FOUND, "User not found"));
    }

    private GetMerchantsResponse.MerchantPayload mapToMerchantPayload(Merchant merchant) {
        return GetMerchantsResponse.MerchantPayload.builder()
                .id(merchant.getId().toString())
                .displayName(merchant.getDisplayName())
                .legalName(merchant.getLegalName())
                .slug(merchant.getSlug())
                .logoUrl(merchant.getLogoUrl())
                .description(merchant.getDescription())
                .cuisineCategory(merchant.getCuisineCategory())
                .contactEmail(merchant.getContactEmail())
                .supportHotline(merchant.getSupportHotline())
                .status(merchant.getStatus().name())
                .createdAt(merchant.getCreatedAt() != null ? merchant.getCreatedAt().toString() : null)
                .build();
    }

    private void validatePagination(BaseRequest request, Integer pageNumber, Integer pageSize) {
        if (pageNumber == null || pageNumber < 1) {
            throw businessException(request, INVALID_INPUT_ERROR, INVALID_PAGE_NUMBER);
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            throw businessException(request, INVALID_INPUT_ERROR, INVALID_PAGE_SIZE);
        }
    }

    private BusinessException businessException(BaseRequest request, String code, String message) {
        return new BusinessException(
                request.getRequestId(),
                request.getRequestDateTime(),
                request.getChannel(),
                ExceptionUtils.buildResultResponse(code, message)
        );
    }
}
