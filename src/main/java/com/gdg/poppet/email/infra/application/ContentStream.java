package com.gdg.poppet.email.infra.application;

import lombok.Getter;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.io.IOException;
import java.util.List;

@Getter
public class ContentStream {
    private PDPageContentStream pageContentStream;
    private PDFont font;
    private float fontSpace;

    public ContentStream(PDPageContentStream pageContentStream, PDFont font) {
        this.pageContentStream = pageContentStream;
        this.font = font;
    }

    public void setFontSize(int fontSize){
        try {
            pageContentStream.setFont(font, fontSize);
            this.fontSpace = (float)(fontSize + 2)/2;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setColor(float r, float g, float b){
        try {
            this.pageContentStream.setNonStrokingColor(r, g, b);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeText(float x, float y, String text){
        try {
            pageContentStream.beginText();
            pageContentStream.newLineAtOffset(x, y + fontSpace);
            pageContentStream.showText(text);
            pageContentStream.endText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeWrappedText(float x, float y, int lineHeight, List<String> texts){
        try {
            pageContentStream.beginText();
            pageContentStream.newLineAtOffset(x, y + fontSpace);
            for (String text : texts) {
                pageContentStream.showText(text);
                pageContentStream.newLineAtOffset(0, -lineHeight);
            }
            pageContentStream.endText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close(){
        try {
            this.pageContentStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
