package com.petbook.petbook_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.petbook.petbook_backend.exceptions.rest.ImageUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;


    public String uploadFile(MultipartFile file) {
        try {

            File tempFile = File.createTempFile("temp", file.getOriginalFilename());
            file.transferTo(tempFile);


            Map uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());

            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new ImageUploadException("Image upload failed", e);
        }
    }
}
