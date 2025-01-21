package com.example.askfm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
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
}
