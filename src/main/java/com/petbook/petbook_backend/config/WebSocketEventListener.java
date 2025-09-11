package com.petbook.petbook_backend.config;

import com.petbook.petbook_backend.dto.sockets.SocketEvent;
import com.petbook.petbook_backend.models.EventType;
import com.petbook.petbook_backend.service.UserServiceImpl;
import com.petbook.petbook_backend.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserServiceImpl userService;
    private final UserSessionService userSessionService;

    @EventListener
    public void handleWebSocketSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();

        if (destination != null && destination.startsWith("/topic/conversation/")) {
            Long conversationId = Long.parseLong(destination.split("/")[3]);
            Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("conversationId", conversationId);

            if (event.getUser() != null) {
                String userName = String.valueOf(event.getUser().getName());
                Long userId = userService.findUserId(userName);

                userSessionService.userOpenedConversation(userId, conversationId);

                messagingTemplate.convertAndSend(
                        "/topic/conversation/" + conversationId,
                        new SocketEvent<>(EventType.USER_CONNECTED, userSessionService.getOnlineUsersMap())

                );
            }
        }
    }



    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        if (event.getUser() != null) {
            String userName = String.valueOf(event.getUser().getName());
            Long userId = userService.findUserId(userName);

            userSessionService.userConnected(userId);
            System.out.println(userSessionService.getOnlineUsersMap());
        }
    }


    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        if (event.getUser() != null) {
            String userName = String.valueOf(event.getUser().getName());
            Long userId = userService.findUserId(userName);

            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            Long conversationId = (Long) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("conversationId");

            userSessionService.userDisconnected(userId);
            userSessionService.userClosedConversation(userId);

            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + conversationId,
                    new SocketEvent<>(EventType.USER_DISCONNECTED, userSessionService.getOnlineUsersMap())

            );
        }
    }
}
