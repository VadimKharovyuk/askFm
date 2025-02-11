package com.example.askfm.valid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

public class ValidFileValidator implements ConstraintValidator<ValidFile, MultipartFile> {
    private String[] allowedContentTypes;
    private long maxSize;

    @Override
    public void initialize(ValidFile constraintAnnotation) {
        this.allowedContentTypes = constraintAnnotation.allowedContentTypes();
        this.maxSize = constraintAnnotation.maxSize();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true; // пропускаем пустые файлы
        }

        // Проверяем размер
        if (file.getSize() > maxSize) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Розмір файлу не повинен перевищувати 3MB")
                    .addConstraintViolation();
            return false;
        }

        // Проверяем тип файла
        if (!Arrays.asList(allowedContentTypes).contains(file.getContentType())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Дозволені формати: JPEG, PNG, GIF")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}