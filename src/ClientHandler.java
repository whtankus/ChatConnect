
import java.io.*;
import java.net.*;
import java.util.Base64;

public class ClientHandler extends Thread {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    private String username;
    private boolean registered = false;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    // Send a message to this particular client
    public synchronized void sendMessage(String message) {

        if (output != null) {
            output.println(message);
        }
    }

    @Override
    public void run() {

        try {

            input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );

            output = new PrintWriter(
                    socket.getOutputStream(), true
            );

            // Receive username
            username = input.readLine();

            if (username == null || username.trim().isEmpty()) {
                return;
            }

            username = username.trim();

            // Register user with server
            ChatServer.registerClient(this);
            registered = true;

            String message;

            // Continuously receive messages
            while ((message = input.readLine()) != null) {

                if (message.equalsIgnoreCase("/exit")) {
                    break;
                }

                if (message.trim().isEmpty()) {
                    continue;
                }

                // Handle private message
                if (message.startsWith("PRIVATE|")) {

                    handlePrivateMessage(message);

                } // Handle group message
                else if (message.startsWith("GROUP|")) {

                    String encodedMessage
                            = message.substring(6);

                    String decodedMessage = new String(
                            Base64.getDecoder().decode(encodedMessage),
                            "UTF-8"
                    );

                    if (!decodedMessage.trim().isEmpty()) {

                        ChatServer.broadcast(
                                "GROUP|" + username + "|"
                                + encodedMessage
                        );
                    }
                }
            }

        } catch (IOException | IllegalArgumentException e) {

            System.out.println(
                    username + " disconnected unexpectedly."
            );

        } finally {

            if (registered) {
                ChatServer.removeClient(this);
            }

            try {

                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }

            } catch (IOException e) {

                System.out.println("Error closing client connection.");
            }
        }
    }

    // Process private message command
    private void handlePrivateMessage(String message) {

        try {

            String[] parts = message.split("\\|", 3);

            if (parts.length != 3) {
                sendMessage("ERROR:INVALID_PRIVATE_MESSAGE");
                return;
            }

            String recipient = parts[1];
            String encodedMessage = parts[2];

            // Validate encoded message
            String decodedMessage = new String(
                    Base64.getDecoder().decode(encodedMessage),
                    java.nio.charset.StandardCharsets.UTF_8
            );

            if (recipient.trim().isEmpty()
                    || decodedMessage.trim().isEmpty()) {
                return;
            }

            ChatServer.sendPrivateMessage(
                    this,
                    recipient,
                    encodedMessage
            );

        } catch (IllegalArgumentException e) {

            sendMessage("ERROR:INVALID_PRIVATE_MESSAGE");
        }
    }
}
