package ma.project.dht11_websocket.config;


import ma.project.dht11_websocket.websocket.EspSensorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@EnableWebSecurity
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF protection because IoT clients don't use web sessions/cookies
                .csrf(csrf -> csrf.disable())
                // Permit all connections to your WebSocket pathways without requiring login
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**").permitAll()
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    private final EspSensorHandler espSensorHandler;

    // Inject our custom handler
    public WebSocketConfig(EspSensorHandler espSensorHandler) {
        this.espSensorHandler = espSensorHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Path for ESP Client data upload
        registry.addHandler(espSensorHandler, "/ws/sensor-data")
                .setAllowedOrigins("*");

        // Path for Kotlin Jetpack Compose App or other microservices
        // registry.addHandler(yourJetpackAppHandler, "/ws/apps").setAllowedOrigins("*");
    }
}