package com.gdg.poppet.global.util;

import com.gdg.poppet.global.exception.GlobalException;
import com.gdg.poppet.global.status.ErrorStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

public class ResourceLoader {

    public static String getResourceContent(String resourcePath) {
        try {
            if (resourcePath.startsWith("/")) {
                // 절대 경로 파일 접근
                return Files.readString(Paths.get(resourcePath), StandardCharsets.UTF_8);
            } else {
                // 클래스패스 접근
                var resource = new ClassPathResource(resourcePath);
                return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new GlobalException(ErrorStatus.NOT_FOUND);
        }
    }
}
