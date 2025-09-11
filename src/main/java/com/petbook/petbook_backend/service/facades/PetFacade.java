package com.petbook.petbook_backend.service.facades;

import com.petbook.petbook_backend.dto.request.AddPetRequest;
import com.petbook.petbook_backend.dto.request.UpdatePetRequest;
import com.petbook.petbook_backend.dto.response.PetInfoPrivateResponse;
import com.petbook.petbook_backend.service.CloudinaryService;
import com.petbook.petbook_backend.service.PetService;
import com.petbook.petbook_backend.service.events.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetFacade {

    private final PetService petService;
    private final CloudinaryService cloudinaryService;


    public PetInfoPrivateResponse addPetWithImages(AddPetRequest request, List<MultipartFile> images) {
        List<Map<String, String>> imageUrls = images.stream()
                .map(cloudinaryService::uploadFile).collect(Collectors.toList());
        request.setImageUrls(imageUrls);


        return petService.addPetPost(request);
    }
    public PetInfoPrivateResponse updatePetWithImages(Long petId,UpdatePetRequest request,List<MultipartFile> images){

        if (images != null && !images.isEmpty()) {
            List<Map<String,String>> imageUrls = images.stream()
                    .map(cloudinaryService::uploadFile).collect(Collectors.toList());
            request.setImageUrls(imageUrls);
        }

        return petService.updatePetPost(request, petId);
    }
}
