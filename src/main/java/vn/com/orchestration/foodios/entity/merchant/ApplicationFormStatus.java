package vn.com.orchestration.foodios.entity.merchant;

import java.util.Arrays;
import java.util.List;

public enum ApplicationFormStatus {
    DRAFT,
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    NEEDS_REVISION;

    public static List<String> valuesAsString() {
        return Arrays.stream(values()).map(Enum::name).toList();
    }
}
