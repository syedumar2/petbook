package com.petbook.petbook_backend.exceptions;

public class PetListingNotFoundException extends RuntimeException {
    public PetListingNotFoundException(String listingNotFound) {
        super(listingNotFound);
    }
}
