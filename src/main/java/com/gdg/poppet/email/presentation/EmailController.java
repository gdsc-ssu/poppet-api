package com.gdg.poppet.email.presentation;

import com.gdg.poppet.global.response.ApiResponse;
import com.gdg.poppet.global.status.SuccessStatus;
import com.gdg.poppet.email.application.dto.request.EmailRequestDto;
import com.gdg.poppet.email.application.dto.response.EmailDto;
import com.gdg.poppet.email.application.dto.response.EmailPeriodDto;
import com.gdg.poppet.email.application.service.EmailService;
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
    public ResponseEntity<ApiResponse<List<EmailDto>>> postEmail(
            @RequestParam("name") String name,
            @RequestBody EmailRequestDto emailDto
    ) {
        return ApiResponse.success(SuccessStatus.POST_EMAIL_SUCCESS, emailService.postEmail(name, emailDto));
    }

    @PatchMapping("/{emailId}")
    public ResponseEntity<ApiResponse<String>> patchEmail(
            @RequestParam("name") String name,
            @PathVariable("emailId") Long emailId,
            @RequestBody EmailRequestDto emailDto
    ) {
        emailService.patchEmail(name, emailId, emailDto);
        return ApiResponse.success(SuccessStatus.PATCH_EMAIL_SUCCESS);
    }

    @DeleteMapping("/{emailId}")
    public ResponseEntity<ApiResponse<String>> deleteEmail(
            @RequestParam("name") String name,
            @PathVariable("emailId") Long emailId
    ) {
        emailService.deleteEmail(name, emailId);
        return ApiResponse.success(SuccessStatus.DELETE_EMAIL_SUCCESS);
    }
}
