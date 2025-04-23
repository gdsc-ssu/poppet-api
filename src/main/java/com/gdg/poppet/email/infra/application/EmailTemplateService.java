package com.gdg.poppet.email.infra.application;

import com.gdg.poppet.chat.domain.model.ChatRoom;
import com.gdg.poppet.user.domain.model.User;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@NoArgsConstructor
public class EmailTemplateService {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final int fontSize = 18;

    public ByteArrayResource makeEmailBackground(User user, ChatRoom chatRoom) {
        // 배경용 PDF 파일 로드
        PDDocument doc = loadPDDocument("email/email_background.pdf");

        // 배경용 PDF 편집
        drawBackgroundImg(doc, user, chatRoom);

        // 이미지로 변환 후 반환
        return parsePdfToImg(doc);
    }

    private ByteArrayResource parsePdfToImg(PDDocument doc) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            doc.close();

            // 메모리에 파일 저장
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            PDDocument loadedDoc = PDDocument.load(bais);

            // 파일 로드 후 이미지로 변환
            PDFRenderer renderer = new PDFRenderer(loadedDoc);
            BufferedImage image = renderer.renderImageWithDPI(0, 300);

            // 이미지 output 로드
            ByteArrayOutputStream imageOut = new ByteArrayOutputStream();
            ImageIO.write(image, "png", imageOut);
            ByteArrayResource imageResource = new ByteArrayResource(imageOut.toByteArray());

            loadedDoc.close();
            return imageResource;
        } catch (IOException e) {
            log.error("[*] 이메일 배경 PDF을 이미지로 변환 중 오류 발생, {}", e.getMessage());
            return null;
        }
    }

    private void drawBackgroundImg(PDDocument doc, User user, ChatRoom chatRoom) {
        PDPage page = doc.getPage(0);

        try {
            PDPageContentStream pageContentStream = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true);
            InputStream fontFile = loadFontFile("fonts/Pretendard.ttf");
            PDFont font = PDType0Font.load(doc, fontFile);

            ContentStream contentStream = new ContentStream(pageContentStream, font);
            contentStream.setFontSize(fontSize);
            contentStream.setColor(0.3f, 0.3f, 0.3f);

            drawUserInfo(contentStream, user.getUsername(), user.getEmailPeriod().getValue());
            drawDateTime(contentStream, chatRoom.getCreatedAt());
            drawSummary(contentStream, chatRoom.getSummary());

            contentStream.close();
        } catch (IOException e) {
            log.error("[*] 이메일 배경 PDF 편집 중 오류 발생, {}", e.getMessage());
        }
    }

    private void drawUserInfo(ContentStream contentStream, String username, int emailPeriod) {
        int nameX = 273;
        int nameY = 556;
        int periodX = 343;
        int periodY = 522;

        contentStream.writeText(nameX, nameY, username);
        contentStream.writeText(periodX, periodY, emailPeriod + "일");
    }

    private void drawDateTime(ContentStream contentStream, LocalDateTime createdAt) {
        int createdAtX = 308;
        int createdAtY = 488;
        int nowY = 454;

        contentStream.writeText(createdAtX, createdAtY, dateTimeFormatter.format(createdAt));
        contentStream.writeText(createdAtX, nowY, dateTimeFormatter.format(LocalDateTime.now()));
    }

    private void drawSummary(ContentStream contentStream, String summary) {
        contentStream.setFontSize(16);
        int summaryX = 215;
        int summaryY = 299;
        int lineHeight = 50;
        int maxChars = 45;

        // 개행문자 제거
        StringBuilder summaryStringBuilder = new StringBuilder();
        for (String line : summary.split("\n")) {
            summaryStringBuilder.append(line);
        }
        String summaryLine = summaryStringBuilder.toString();

        // 45자 단위로 문장 분리
        List<String> texts = new ArrayList<>();
        for (int start = 0; start < summaryLine.length(); start += maxChars) {
            int end = Math.min(summaryLine.length(), start + maxChars);
            texts.add(summaryLine.substring(start, end));
        }
        contentStream.writeWrappedText(summaryX, summaryY, lineHeight, texts);
    }

    private PDDocument loadPDDocument(String path){
        ClassPathResource pdfResource = new ClassPathResource(path);
        try (InputStream inputStream = pdfResource.getInputStream()) {
            return PDDocument.load(inputStream);
        } catch (IOException e) {
            log.error("[*] 이메일 배경 PDF 파일 로드 중 오류 발생, {}", e.getMessage());
            return null;
        }
    }

    private InputStream loadFontFile(String path){
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return resource.getInputStream();
        } catch (IOException e) {
            log.error("[*] 이메일 배경 편집을 위한 폰트 파일 로드 중 오류 발생, {}", e.getMessage());
            return null;
        }
    }
}
