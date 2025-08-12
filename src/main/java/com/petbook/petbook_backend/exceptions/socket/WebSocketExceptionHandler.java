package com.petbook.petbook_backend.exceptions.socket;

import com.petbook.petbook_backend.dto.response.ApiResponse;
import com.petbook.petbook_backend.exceptions.rest.ChatAccessDeniedException;
import com.petbook.petbook_backend.exceptions.rest.ConversationNotFoundException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketExceptionHandler {

    @MessageExceptionHandler(ChatAccessDeniedException.class)
    @SendToUser("/queue/errors")
    public ApiResponse<Void> handleChatAccessDenied(ChatAccessDeniedException ex)
    {
        return ApiResponse.failure(ex.getMessage());
    }


    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public ApiResponse<Void> handleGenericException(Exception ex) {
        return ApiResponse.failure("An unexpected error occurred");
    }

    @MessageExceptionHandler(ConversationNotFoundException.class)
    @SendToUser("/queue/errors")
    public ApiResponse<Void> handleConversationNotFound(ConversationNotFoundException ex) {
        return ApiResponse.failure(ex.getMessage());
    }
}
