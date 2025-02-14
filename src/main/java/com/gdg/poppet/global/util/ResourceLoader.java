package com.gdg.poppet.global.util;

import com.gdg.poppet.global.exception.GlobalException;
import com.gdg.poppet.global.status.ErrorStatus;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

public class ResourceLoader {

    public static String getResourceContent(String resourcePath) {
        try {
            var resource = new ClassPathResource(resourcePath);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new GlobalException(ErrorStatus.NOT_FOUND);
        }
    }
}
