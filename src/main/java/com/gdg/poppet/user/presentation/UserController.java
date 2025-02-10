package com.gdg.poppet.user.presentation;

import com.gdg.poppet.global.dto.ApiResponse;
import com.gdg.poppet.global.status.SuccessStatus;
import com.gdg.poppet.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

}
