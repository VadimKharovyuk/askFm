package com.example.askfm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class ImageService {

    public byte[] resizeImage(byte[] imageData, int targetWidth) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            BufferedImage originalImage = ImageIO.read(bis);

            // Вычисляем новую высоту, сохраняя пропорции
            int targetHeight = (int) ((double) targetWidth / originalImage.getWidth() * originalImage.getHeight());

            BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
            resizedImage.getGraphics().drawImage(originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, null);

            ImageIO.write(resizedImage, "jpg", bos);
            return bos.toByteArray();
        }
    }

    public String getBase64Avatar(byte[] avatar) {
        if (avatar != null) {
            return Base64.getEncoder().encodeToString(avatar);
        }
        return null;
    }



    public byte[] blurImage(byte[] imageData) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            BufferedImage originalImage = ImageIO.read(bis);

            // Создаем размытое превью для заблокированных фото
            BufferedImage blurredImage = new BufferedImage(
                    originalImage.getWidth(),
                    originalImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D g2d = blurredImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, null);

            // Применяем размытие
            float[] matrix = {
                    0.111f, 0.111f, 0.111f,
                    0.111f, 0.111f, 0.111f,
                    0.111f, 0.111f, 0.111f,
            };

            BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix));
            blurredImage = op.filter(blurredImage, null);

            ImageIO.write(blurredImage, "jpg", bos);
            return bos.toByteArray();
        }
    }

    public byte[] addWatermark(byte[] imageData, String watermarkText) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            BufferedImage originalImage = ImageIO.read(bis);

            Graphics2D g2d = originalImage.createGraphics();
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            g2d.setColor(new Color(255, 255, 255, 128));

            // Добавляем водяной знак
            FontMetrics fm = g2d.getFontMetrics();
            int x = (originalImage.getWidth() - fm.stringWidth(watermarkText)) / 2;
            int y = originalImage.getHeight() / 2;

            g2d.drawString(watermarkText, x, y);
            g2d.dispose();

            ImageIO.write(originalImage, "jpg", bos);
            return bos.toByteArray();
        }
    }
}
