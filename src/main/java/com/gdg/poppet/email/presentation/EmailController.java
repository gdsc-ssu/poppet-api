package com.gdg.poppet.email.presentation;

import com.gdg.poppet.global.response.ApiResponse;
import com.gdg.poppet.global.status.SuccessStatus;
import com.gdg.poppet.email.application.dto.request.EmailRequestDto;
import com.gdg.poppet.email.application.dto.response.EmailDto;
import com.gdg.poppet.email.application.dto.response.EmailPeriodDto;
import com.gdg.poppet.email.application.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/emails")
public class EmailController {

    private final EmailService emailService;

    @GetMapping("/period")
    public ResponseEntity<ApiResponse<EmailPeriodDto>> getEmailPeriod(
            @AuthenticationPrincipal UserDetails user
    ) {
        System.out.println(user);
        return ApiResponse.success(SuccessStatus.GET_EMAIL_PERIOD_SUCCESS, emailService.getEmailPeriod(user.getUsername()));
    }

    @PatchMapping("/period")
    public ResponseEntity<ApiResponse<EmailPeriodDto>> patchEmailPeriod(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam("period") int period
    ) {
        return ApiResponse.success(SuccessStatus.PATCH_EMAIL_PERIOD_SUCCESS, emailService.patchEmailPeriod(user.getUsername(), period));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<EmailDto>>> getEmailList(
            @AuthenticationPrincipal UserDetails user
    ) {
        return ApiResponse.success(SuccessStatus.GET_EMAIL_LIST_SUCCESS, emailService.getEmailAddressList(user.getUsername()));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<List<EmailDto>>> postEmail(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody EmailRequestDto emailDto
    ) {
        System.out.println(user);
        return ApiResponse.success(SuccessStatus.POST_EMAIL_SUCCESS, emailService.postEmailAddress(user.getUsername(), emailDto));
    }

    @PatchMapping("/{emailId}")
    public ResponseEntity<ApiResponse<String>> patchEmail(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable("emailId") Long emailId,
            @RequestBody EmailRequestDto emailDto
    ) {
        emailService.patchEmailAddress(user.getUsername(), emailId, emailDto);
        return ApiResponse.success(SuccessStatus.PATCH_EMAIL_SUCCESS);
    }

    @DeleteMapping("/{emailId}")
    public ResponseEntity<ApiResponse<String>> deleteEmail(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable("emailId") Long emailId
    ) {
        emailService.deleteEmailAddress(user.getUsername(), emailId);
        return ApiResponse.success(SuccessStatus.DELETE_EMAIL_SUCCESS);
    }
}
