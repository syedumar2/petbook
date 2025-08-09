-- V1__init_schema.sql

-- ======================
-- USERS TABLE
-- ======================
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL UNIQUE,
    profile_image_url VARCHAR(500),
    firstname VARCHAR(100) NOT NULL,
    lastname VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    location VARCHAR(255),
    refresh_token VARCHAR(500),
    role VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for quick login lookups
CREATE UNIQUE INDEX idx_users_email ON users (email);

-- Index if filtering by location
CREATE INDEX idx_users_location ON users (location);

-- ======================
-- PETS TABLE
-- ======================
CREATE TABLE pets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL,
    breed VARCHAR(100),
    location VARCHAR(255) NOT NULL,
    description TEXT,
    adopted BOOLEAN NOT NULL DEFAULT FALSE,
    approved BOOLEAN NOT NULL DEFAULT FALSE,
    approved_at TIMESTAMP NULL;
    rejected_at TIMESTAMP NULL;
    owner_id BIGINT,
    CONSTRAINT fk_pets_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

-- Indexes to optimize pet queries
CREATE INDEX idx_pets_approved ON pets (approved);
CREATE INDEX idx_pets_location ON pets (location);
CREATE INDEX idx_pets_owner_id ON pets (owner_id);
CREATE INDEX idx_pets_owner_approved ON pets (owner_id, approved);

-- ======================
-- PET IMAGES TABLE
-- ======================
CREATE TABLE pet_images (
    pet_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    CONSTRAINT fk_pet_images_pet FOREIGN KEY (pet_id) REFERENCES pets (id)
);

CREATE INDEX idx_pet_images_pet_id ON pet_images (pet_id);
