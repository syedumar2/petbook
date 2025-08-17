
# PetBook — Pet Adoption Platform Backend

PetBook is a feature-rich pet adoption backend built with Spring Boot 3.5.2 and MySQL, designed to facilitate pet adoption in India. This is a portfolio project demonstrating secure user authentication, pet management, real-time messaging between users, and admin moderation capabilities.



# PetBook — Pet Adoption Platform Backend

**PetBook** is a feature-rich pet adoption backend built with **Spring Boot 3.5.2** and **MySQL**, designed to facilitate pet adoption in India. This is a **portfolio project** demonstrating secure user authentication, pet management, real-time messaging between users, and admin moderation capabilities.  

---

## Table of Contents
1. [Features](#features)  
2. [Tech Stack](#tech-stack)  
3. [Setup & Installation](#setup--installation)  
4. [API Endpoints](#api-endpoints)  
5. [Architecture & Structure](#architecture--structure)  
6. [Future Plans](#future-plans)  

---

## Features

### **User & Auth**
- Signup & login with **JWT authentication** (access + refresh tokens).  
- Role-based authorization (`USER` and `ADMIN`).  
- Refresh token and logout support.  
- Fetch authenticated user profile.  

### **Pet Management**
- Add, update, and delete pets with optional multiple images (via **Cloudinary**).  
- Fetch all pets with sorting, pagination, or filtering.  
- Search pets by name, type, breed, or location.  
- Track pet approval status (`PENDING`, `APPROVED`, `REJECTED`).  

### **Admin Functions**
- View all users and their details.  
- Approve or reject pet listings.  
- View approved or unapproved pets.  

### **Real-Time Chat**
- Start a conversation between two users, optionally linked to a pet.  
- Real-time messaging using **Spring WebSocket + STOMP + SockJS**.  
- Mark messages as read and fetch conversation history.  
- Messages tied to authenticated users for secure communication.  

### **Other**
- Consistent API response wrapper (`ApiResponse<T>`).  
- DTO-based request/response pattern.  
- Validation for all incoming requests.  
- Exception handling via `@ControllerAdvice`.  

---

## Tech Stack
- **Backend:** Spring Boot 3.5.2, Spring Security, Spring Data JPA  
- **Database:** MySQL  
- **Authentication:** JWT with refresh token  
- **Real-Time Messaging:** WebSocket, STOMP, SockJS  
- **File Storage:** Cloudinary for pet images  
- **Other Libraries:** Lombok, Jakarta Validation  

---

## Setup & Installation

1. **Clone the repository:**
```
git clone https://github.com/<your-username>/petbook-backend.git
cd petbook-backend
```

2. **Configure the database:**

   - Create a MySQL database.
   - Update `application.properties` or `application.yml` with your DB credentials.

3. **Run the application:**

   ```
   ./mvnw spring-boot:run
   ```
   Or build a JAR and run:
   ``` 
   ./mvnw clean package
    java -jar target/petbook-backend-0.0.1-SNAPSHOT.jar
    ```
## API Endpoints (Highlights)

### Auth
- `POST /api/auth/register` — Register a new user  
- `POST /api/auth/login` — Login and receive JWT  
- `POST /api/auth/refresh` — Refresh access token  
- `POST /api/auth/logout` — Logout  
- `POST /api/auth/admin/register` — Create an admin user  

### Users
- `GET /api/user/me` — Get current user profile  
- `GET /api/user/me/pets` — Get user’s pets  
- `POST /api/user/me/pets` — Add a pet (multipart)  
- `PUT /api/user/me/pets/{petId}` — Update pet  
- `DELETE /api/user/me/pets/{petId}` — Delete pet  
- `PATCH /api/user/me` — Update user profile  

### Pets
- `GET /api/pets/get` — List all pets  
- `GET /api/pets/get/{field}` — List pets sorted by field  
- `GET /api/pets/get/page` — List pets with pagination  
- `GET /api/pets/get/page-sort` — List pets with pagination & sorting  
- `GET /api/pets/search` — Search pets by name, type, breed, location  
- `POST /api/pets/get` — Find pets by example query  

### Admin
- `GET /api/admin/users` — Fetch all users  
- `GET /api/admin/pets/approved` — List approved pets  
- `GET /api/admin/pets/unapproved` — List unapproved pets  
- `POST /api/admin/pets/{petId}/approve` — Approve pet  
- `POST /api/admin/pets/{petId}/reject` — Reject pet  

### Chat
- `POST /api/chat/start` — Start conversation
- WebSocket `/app/chat.sendMessage` — Send message
- WebSocket `/app/chat.markRead` — Mark messages as read
- `GET /api/chat/{conversationId}/messages` — Fetch conversation messages
- `DELETE /api/chat/delete/{conversationId}` — End conversation
- `GET /api/chat/getMyConversations` — Get all conversations for the logged-in user

---

## Architecture & Structure
- Pattern: Controller → Service → Repository → Entity  
- DTOs for request and response payloads  
- Service layer handles business logic including validation  
- WebSocket layer for real-time messaging with STOMP  
- Role-based access: Admin & User separation  

---

## Future Plans
- Implement user blacklisting and deletion for admins  
- Building Frontend using a SPA library like React
- Deployment on Render

---

## Notes
- This project is intended as a **portfolio backend project**  
- All implemented endpoints are fully functional ✅  
- Frontend will handle notifications and additional UI features  





