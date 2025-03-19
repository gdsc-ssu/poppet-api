package com.gdg.poppet.user.presentation;

import com.gdg.poppet.global.response.ApiResponse;
import com.gdg.poppet.global.status.SuccessStatus;
import com.gdg.poppet.user.application.dto.response.EmailDto;
import com.gdg.poppet.user.application.dto.response.EmailPeriodDto;
import com.gdg.poppet.user.application.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/emails")
public class EmailController {

    private final EmailService emailService;

    @GetMapping("/period")
    public ResponseEntity<ApiResponse<EmailPeriodDto>> getEmailPeriod(
            @RequestParam("name") String name
    ) {
        return ApiResponse.success(SuccessStatus.GET_EMAIL_PERIOD_SUCCESS, emailService.getEmailPeriod(name));
    }

    @PatchMapping("/period")
    public ResponseEntity<ApiResponse<EmailPeriodDto>> patchEmailPeriod(
            @RequestParam("name") String name,
            @RequestParam("period") int period
    ) {
        return ApiResponse.success(SuccessStatus.PATCH_EMAIL_PERIOD_SUCCESS, emailService.patchEmailPeriod(name, period));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<EmailDto>>> getEmailList(
            @RequestParam("name") String name
    ) {
        return ApiResponse.success(SuccessStatus.GET_EMAIL_LIST_SUCCESS, emailService.getEmailList(name));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<List<EmailDto>>> postEmailList(
            @RequestParam("name") String name,
            @RequestParam("email") String email
    ) {
        emailService.postEmail(name, email);
        return ApiResponse.success(SuccessStatus.POST_EMAIL_SUCCESS);
    }
}
