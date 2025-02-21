package com.example.askfm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Iterator;

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
    public byte[] blurImage(byte[] imageData) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            BufferedImage originalImage = ImageIO.read(bis);

            // Сразу уменьшаем размер
            int newWidth = originalImage.getWidth() / 4;
            int newHeight = originalImage.getHeight() / 4;

            BufferedImage smallImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = smallImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();

            // Применяем размытие с большим радиусом (15x15 матрица)
            float[] matrix = new float[225]; // 15 x 15 = 225
            float weight = 1.0f / 225.0f;
            for (int i = 0; i < 225; i++) {
                matrix[i] = weight;
            }
            BufferedImageOp blurOp = new ConvolveOp(new Kernel(15, 15, matrix));
            BufferedImage blurredImage = blurOp.filter(smallImage, null);

// Применяем размытие второй раз для усиления эффекта
            blurredImage = blurOp.filter(blurredImage, null);

            // Увеличиваем обратно
            BufferedImage finalImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            g = finalImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(blurredImage, 0, 0, originalImage.getWidth(), originalImage.getHeight(), null);

            // Добавляем затемнение
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g.setColor(new Color(0, 0, 0, 128));
            g.fillRect(0, 0, finalImage.getWidth(), finalImage.getHeight());
            g.dispose();

            // Сохраняем с низким качеством
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.2f);

            IIOImage iioImage = new IIOImage(finalImage, null, null);
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(bos)) {
                writer.setOutput(ios);
                writer.write(null, iioImage, param);
            }

            return bos.toByteArray();
        }
    }
}