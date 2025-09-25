package com.petbook.petbook_backend.security.utils;

import com.petbook.petbook_backend.models.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static Long getCurrentUserId() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return userDetails.getId();
    }

}
