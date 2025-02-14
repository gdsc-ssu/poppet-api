package com.gdg.poppet.chat.infra.speech.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Configuration
public class GoogleCloudConfig {
    @Value("${spring.cloud.gcp.credentials.location}")
    String gcsCredentials;

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        return GoogleCredentials.fromStream(new FileInputStream(gcsCredentials));
    }

    @Bean
    public SpeechClient speechClient(GoogleCredentials credentials) throws IOException {
        SpeechSettings speechSettings = SpeechSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();
        return SpeechClient.create(speechSettings);
    }

    @Bean
    public TextToSpeechClient textToSpeechClient(GoogleCredentials credentials) throws IOException {
        TextToSpeechSettings textToSpeechSettings = TextToSpeechSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)
                .build();
        return TextToSpeechClient.create(textToSpeechSettings);
    }
}
