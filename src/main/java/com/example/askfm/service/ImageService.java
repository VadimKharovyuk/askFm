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


    public byte[] blurImage(byte[] imageData) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            BufferedImage originalImage = ImageIO.read(bis);

            // Создаем сильно размытое изображение
            BufferedImage blurredImage = new BufferedImage(
                    originalImage.getWidth(),
                    originalImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );

            Graphics2D g2d = blurredImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.dispose();

            // Сильное размытие по Гауссу
            float[] matrix = {
                    1f / 256, 4f / 256, 6f / 256, 4f / 256, 1f / 256,
                    4f / 256, 16f / 256, 24f / 256, 16f / 256, 4f / 256,
                    6f / 256, 24f / 256, 36f / 256, 24f / 256, 6f / 256,
                    4f / 256, 16f / 256, 24f / 256, 16f / 256, 4f / 256,
                    1f / 256, 4f / 256, 6f / 256, 4f / 256, 1f / 256
            };
            BufferedImageOp blurOp = new ConvolveOp(new Kernel(5, 5, matrix), ConvolveOp.EDGE_NO_OP, null);



            // Многократное применение размытия
            for (int i = 0; i < 25; i++) {
                blurredImage = blurOp.filter(blurredImage, null);
            }

            // Сильное уменьшение разрешения
            int newWidth = originalImage.getWidth() / 8;
            int newHeight = originalImage.getHeight() / 8;

            // Уменьшаем
            BufferedImage smallImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = smallImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(blurredImage, 0, 0, newWidth, newHeight, null);
            g.dispose();

            // Увеличиваем обратно
            blurredImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            g = blurredImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(smallImage, 0, 0, originalImage.getWidth(), originalImage.getHeight(), null);

            // Добавляем затемнение
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            g.setColor(new Color(0, 0, 0, 128));
            g.fillRect(0, 0, blurredImage.getWidth(), blurredImage.getHeight());
            g.dispose();

            // Увеличиваем контраст
            RescaleOp rescaleOp = new RescaleOp(1.2f, -10.0f, null);
            blurredImage = rescaleOp.filter(blurredImage, null);

            // Сохраняем с очень низким качеством
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(0.1f);

            IIOImage iioImage = new IIOImage(blurredImage, null, null);
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(bos)) {
                writer.setOutput(ios);
                writer.write(null, iioImage, param);
            }

            return bos.toByteArray();
        }
    }

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
}
