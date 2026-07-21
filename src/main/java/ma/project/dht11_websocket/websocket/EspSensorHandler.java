package ma.project.dht11_websocket.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class EspSensorHandler extends TextWebSocketHandler {

    // Thread-safe set to track all live connections (Both ESP and Android clients)
    private final CopyOnWriteArraySet<WebSocketSession> activeSessions = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        activeSessions.add(session);
        System.out.println("New client connected [ID: " + session.getId() + "]. Total active: " + activeSessions.size());

        // Push an instant confirmation frame so the Android app updates its connection indicator color immediately
        session.sendMessage(new TextMessage("Connected to Spring Backend"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Received pipeline data: " + payload);

        // Broadcast the raw incoming payload to EVERY connected client session
        // This ensures your Jetpack Compose app mirrors whatever the ESP updates in real time
        broadcastToClients(payload);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        activeSessions.remove(session);
        System.out.println("Client disconnected [ID: " + session.getId() + "]. Remaining: " + activeSessions.size());
    }

    /**
     * Iterates through active pipes and pushes data packets asynchronously.
     */
    private void broadcastToClients(String payload) {
        TextMessage textMessage = new TextMessage(payload);

        for (WebSocketSession session : activeSessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (IOException e) {
                    System.err.println("Failed to stream to session " + session.getId() + ": " + e.getMessage());
                }
            }
        }
    }
}