package com.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class App extends JFrame {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JTextArea todoTextArea;

    private OAuth2AccessToken oAuth2AccessToken;
    private final ObjectMapper objectMapper;

    private static final String CLIENT_ID;
    private static final String CLIENT_SECRET;
    private static final String TOKEN_ENDPOINT = "http://localhost:9000/auth/realms/todo-api/protocol/openid-connect/token";
    private static final String GRANT_TYPE = "password";
    private static final String SCOPES = "todo:read,todo:write";

    private static final String TODO_ENDPOINT = "http://localhost:8090/todos";

    static {
        ResourceBundle resourceBundle = ResourceBundle.getBundle("client");
        CLIENT_ID = resourceBundle.getString("client_id");
        CLIENT_SECRET = resourceBundle.getString("client_secret");
    }

    public static void main(String[] args) {
        new App();
    }

    public App() throws HeadlessException {
        super("OAuth2 GUIクライアント");

        usernameField = new JTextField(16);
        usernameField.setText("user");
        passwordField = new JPasswordField(16);
        passwordField.setText("user");
        loginButton = new JButton("ログイン");
        loginButton.addActionListener(event -> login());
        todoTextArea = new JTextArea();

        JPanel panel = new JPanel();
        panel.add(usernameField);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(todoTextArea);
        Container contentPane = getContentPane();
        contentPane.add(panel, BorderLayout.CENTER);

        objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.registerModule(new JavaTimeModule());

        super.setBounds(100, 100, 400, 300);
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.setVisible(true);
    }

    private void login() {
        try {
            // ログイン処理
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            HttpClient client = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .connectTimeout(Duration.ofSeconds(20))
                    .build();
            HttpRequest loginRequest = HttpRequest.newBuilder()
                    .uri(URI.create(TOKEN_ENDPOINT))
                    .timeout(Duration.ofMinutes(2))
                    .header("Authorization", "Basic " + Base64.getEncoder().encodeToString((CLIENT_ID + ":" + CLIENT_SECRET).getBytes()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            String.format("grant_type=%s&username=%s&password=%s&scope=%s", GRANT_TYPE, username, password, SCOPES)))
                    .build();
            HttpResponse<String> loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());
            oAuth2AccessToken = objectMapper.readValue(loginResponse.body(), OAuth2AccessToken.class);
            System.out.println(oAuth2AccessToken);

            // アクセストークンを使ったtodoの取得
            HttpRequest todoRequest = HttpRequest.newBuilder()
                    .uri(URI.create(TODO_ENDPOINT))
                    .timeout(Duration.ofMinutes(2))
                    .header("Authorization", "Bearer " + oAuth2AccessToken.getAccessToken())
                    .GET()
                    .build();
            HttpResponse<String> todoResponse = client.send(todoRequest, HttpResponse.BodyHandlers.ofString());
            List<Todo> todoList = objectMapper.readValue(todoResponse.body(), new TypeReference<>() {});
            String todosText = todoList.stream()
                    .map(todo -> todo.getDescription())
                    .collect(Collectors.joining("\n"));
            todoTextArea.setText(todosText);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
