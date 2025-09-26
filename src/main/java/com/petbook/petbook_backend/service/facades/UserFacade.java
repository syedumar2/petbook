package com.petbook.petbook_backend.service.facades;

import com.petbook.petbook_backend.dto.request.UpdateUserRequest;
import com.petbook.petbook_backend.dto.response.UserDetailsResponse;
import com.petbook.petbook_backend.service.CloudinaryService;
import com.petbook.petbook_backend.service.PetService;
import com.petbook.petbook_backend.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserFacade {
    private final PetService petService;
    private final CloudinaryService cloudinaryService;
    private final UserServiceImpl userService;

    public UserDetailsResponse updateUserPfp(UpdateUserRequest request, MultipartFile image){
        if (image != null && !image.isEmpty()) {
            Map<String, String> imageUrls  = cloudinaryService.uploadFile(image);
            for(Map.Entry map : imageUrls.entrySet()){
                System.out.println(map.getValue());
                request.setProfileImageUrl(map.getKey().toString());
                request.setPublicId(map.getValue().toString());
            }
        }
        return userService.updateUser(request);
    }


}
