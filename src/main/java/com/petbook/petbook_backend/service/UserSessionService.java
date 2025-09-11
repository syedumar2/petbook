package com.petbook.petbook_backend.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service

public class UserSessionService {
    private final Map<Long, Boolean> onlineUsers = new ConcurrentHashMap<>();
    private final Map<Long, Long> activeConversations = new ConcurrentHashMap<>();

    public void userConnected(Long userId) {
        onlineUsers.put(userId, true);
    }

    public Map<Long,Boolean> getOnlineUsersMap(){
        return this.onlineUsers;
    }

    public void userDisconnected(Long userId) {
        onlineUsers.remove(userId);
        activeConversations.remove(userId);
    }

    public boolean isUserOnline(Long userId) {
        return onlineUsers.containsKey(userId);
    }

    public void userOpenedConversation(Long userId, Long conversationId) {
        activeConversations.put(userId, conversationId);
    }

    public void userClosedConversation(Long userId) {
        activeConversations.remove(userId);
    }

    public boolean isUserInConversation(Long userId, Long conversationId) {
        return Objects.equals(activeConversations.get(userId), conversationId);
    }
}
