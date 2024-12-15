package com.papa.app.handler;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.papa.app.DBConnection;
import com.papa.app.dto.ChatMessage;
import com.papa.app.service.KafkaService;

public class WebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Map<WebSocketSession, String>> topicSessions = new ConcurrentHashMap<>();
    private final KafkaService kafkaService;

    public WebSocketHandler(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String topic = extractTopicFromSession(session);
        System.out.println("New WebSocket connection established for topic: " + topic);
        topicSessions.computeIfAbsent(topic, k -> new ConcurrentHashMap<>())
                .put(session, topic);
        System.out.println("Total sessions for topic " + topic + ": " + topicSessions.get(topic).size());

        // Load history from Kafka when client connects
        kafkaService.getHistory(topic).thenAccept(messages -> {
            messages.forEach(msg -> {
                try {
                    ChatMessage chatMessage = objectMapper.readValue(msg, ChatMessage.class);
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }
    
    /**
     * Censors bad words in the given text by replacing them with asterisks.
     * 
     * @param text The original text.
     * @return The censored text.
     */
//    private String censorBadWords(String text) {
//        if (text == null) {
//            return null; // Handle null messages gracefully
//        }
//
//        // List of inappropriate/bad words to censor
//        List<String> badWords = Arrays.asList("fuck", "shit", "bitch", "UCLA", "FUCK", "SHIT", "BITCH", "ucla", "FUCKING", "fucking");
//
//        for (String badWord : badWords) {
//            // Replace bad word with asterisks
//            String replacement = "*".repeat(badWord.length());
//            text = text.replaceAll("(?i)\\b" + badWord + "\\b", replacement);
//        }
//
//        return text;
//    }
    
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
    	// save message, check if it is a command
    	// If command check access - if has access 
    	// If not command check for banned words and censor
    	//System.out.println(message.getPayload().toString());
        String payload = message.getPayload().toString();
        ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);
        String chatMsg = chatMessage.getMessage();
        String topic = extractTopicFromSession(session);
        String username = chatMessage.getUser();
        DBConnection dbc = new DBConnection();
        boolean sendMessage = dbc.isUserBanned(username, topic) != 0;
        int dbResponse = dbc.saveChat(topic, username, chatMsg);
        if(dbResponse != 0) System.out.println("Error: " + dbResponse);
    	// Check for Command
        if(chatMsg.charAt(0) == '/') {
        	// Is a Command
        	sendMessage = false;
        	int access = dbc.getUserAccess(username, topic);
        	String[] messagesplit = chatMsg.split(" ");
        	if((access == 0 || access == 1) && messagesplit[0].equals("/ban")) {
            	// Command is to ban
                String userToBan = messagesplit[1];
                int duration = Integer.parseInt(messagesplit[2]);
                dbc.banUser(userToBan, topic, duration);
                System.out.println("banning " + userToBan + " from " + topic);
        	} else if(access == 0 && messagesplit[0].equals("/mod")) {
        		// Command to mod a user
        		String userToMod = messagesplit[1];
                dbc.enrollUser(userToMod, topic, "moderator");
                System.out.println("Making " + userToMod + " a mod for " + topic);
        	}
        } else {
        	// Not a Command
        	chatMessage.setMessage(dbc.censorBannedWords(chatMsg));
        }
        dbc.closeDBConnection();
        String jsonMessage = objectMapper.writeValueAsString(chatMessage);

        // Broadcast immediately to all clients
        Map<WebSocketSession, String> sessions = topicSessions.get(topic);
        if (sessions != null) {
            TextMessage textMessage = new TextMessage(jsonMessage);
            for (WebSocketSession clientSession : sessions.keySet()) {
                if (clientSession.isOpen()) {
                    if (sendMessage)
                    {
                        clientSession.sendMessage(textMessage);
                    }
                }
            }
        }

        // Send to Kafka asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                    kafkaService.send(topic, jsonMessage);
            } catch (Exception e) {
                System.err.println("Failed to send to Kafka: " + e.getMessage());
            }
        });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        String topic = extractTopicFromSession(session);
        System.out.println("Connection closed for session: " + session.getId() + " in topic: " + topic);
        Map<WebSocketSession, String> sessions = topicSessions.get(topic);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                topicSessions.remove(topic);
            }
            System.out.println("Remaining sessions in topic " + topic + ": " +
                    (sessions.isEmpty() ? 0 : sessions.size()));
        }
    }

    private String extractTopicFromSession(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}