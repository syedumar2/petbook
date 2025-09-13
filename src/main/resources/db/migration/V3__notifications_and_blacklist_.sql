ALTER TABLE pet_images
ADD COLUMN public_id VARCHAR(255) NOT NULL;

ALTER TABLE users
ADD COLUMN public_id VARCHAR(255) NOT NULL;

ALTER TABLE pets
ADD COLUMN gender VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE')) NOT NULL;


CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipient_id BIGINT,
    message TEXT,
    notification_type VARCHAR(50),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_recipient FOREIGN KEY (recipient_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_notifications_user_unread_desc ON notifications (recipient_id,is_read,created_at DESC);


CREATE TABLE blacklisted_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    blacklisted_at TIMESTAMP NOT NULL,
    reason VARCHAR(255),
    CONSTRAINT fk_blacklisted_user_user
        FOREIGN KEY (user_id) REFERENCES user(id)
        ON DELETE CASCADE
);


ALTER TABLE pets
ADD COLUMN created_at TIMESTAMP NOT NULL;
