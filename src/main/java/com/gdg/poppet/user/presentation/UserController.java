package com.gdg.poppet.user.presentation;

import com.gdg.poppet.global.response.ApiResponse;
import com.gdg.poppet.global.status.SuccessStatus;
import com.gdg.poppet.user.application.UserService;
import com.gdg.poppet.user.application.dto.response.EmailPeriodDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/emails/period")
    public ResponseEntity<ApiResponse<EmailPeriodDto>> getEmailPeriod(
            @RequestParam("name") String name
    ) {
        return ApiResponse.success(SuccessStatus.GET_EMAIL_PERIOD_SUCCESS, userService.getEmailPeriod(name));
    }

    @PatchMapping("/emails/period")
    public ResponseEntity<ApiResponse<EmailPeriodDto>> patchEmailPeriod(
            @RequestParam("name") String name,
            @RequestParam("period") int period
    ) {
        return ApiResponse.success(SuccessStatus.PATCH_EMAIL_PERIOD_SUCCESS, userService.patchEmailPeriod(name, period));
    }

}
