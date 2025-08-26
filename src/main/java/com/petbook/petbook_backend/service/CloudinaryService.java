package com.petbook.petbook_backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.petbook.petbook_backend.exceptions.rest.ImageUploadException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;


    public Map<String, String> uploadFile(MultipartFile file) {
        try {

            File tempFile = File.createTempFile("temp", file.getOriginalFilename());
            file.transferTo(tempFile);


            Map uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());

            Map<String, String> imgUrlAndId = new HashMap<>();

            imgUrlAndId.put(uploadResult.get("secure_url").toString(), uploadResult.get("public_id").toString());
            return imgUrlAndId;
        } catch (IOException e) {
            throw new ImageUploadException("Image upload failed", e);
        }
    }

    public void deleteFile(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            throw new RuntimeException("Image deletion from cloudinary failed", e);
        }
    }

}
