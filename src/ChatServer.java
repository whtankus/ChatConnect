
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    static Set<ClientHandler> clients
            = Collections.synchronizedSet(new LinkedHashSet<>());

    // Send a group message to every connected client
    public static void broadcast(String message) {

        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }

        System.out.println(message);
    }

    // Register a new client
    public static void registerClient(ClientHandler client) {

        synchronized (clients) {
            clients.add(client);
        }

        System.out.println(client.getUsername() + " joined the chat.");

        broadcast("SERVER: " + client.getUsername() + " joined the chat!");

        broadcastOnlineUsers();
    }

    // Send online users to everyone
    public static void broadcastOnlineUsers() {

        StringBuilder users = new StringBuilder("USERS:");

        synchronized (clients) {

            for (ClientHandler client : clients) {

                if (users.length() > 6) {
                    users.append(",");
                }

                users.append(client.getUsername());
            }

            String userList = users.toString();

            for (ClientHandler client : clients) {
                client.sendMessage(userList);
            }
        }

        System.out.println("Online users: " + users);
    }

    // Send a private message to a specific user
    public static void sendPrivateMessage(
            ClientHandler sender,
            String recipientName,
            String encodedMessage) {

        ClientHandler recipient = null;

        synchronized (clients) {

            for (ClientHandler client : clients) {

                if (client.getUsername().equalsIgnoreCase(recipientName)) {
                    recipient = client;
                    break;
                }
            }
        }

        if (recipient == null) {
            sender.sendMessage("ERROR:USER_NOT_FOUND:" + recipientName);
            return;
        }

        // Prevent sending a private message to yourself
        if (recipient == sender) {
            sender.sendMessage("ERROR:SELF_MESSAGE");
            return;
        }

        // Deliver to recipient
        // Deliver to recipient
        recipient.sendMessage(
                "PRIVATE|" + sender.getUsername() + "|"
                + recipient.getUsername() + "|" + encodedMessage
        );

// Echo back to sender
        sender.sendMessage(
                "PRIVATE|" + sender.getUsername() + "|"
                + recipient.getUsername() + "|" + encodedMessage
        );

        System.out.println(
                "[PRIVATE] " + sender.getUsername()
                + " -> " + recipientName
        );
    }

    // Remove disconnected client
    public static void removeClient(ClientHandler client) {

        boolean removed;

        synchronized (clients) {
            removed = clients.remove(client);
        }

        if (removed) {

            System.out.println(
                    client.getUsername() + " left the chat."
            );

            broadcast(
                    "SERVER: " + client.getUsername() + " left the chat!"
            );

            broadcastOnlineUsers();
        }
    }

    public static void main(String[] args) {

        try (ServerSocket server = new ServerSocket(5000)) {

            System.out.println("================================");
            System.out.println("       CHATCONNECT SERVER");
            System.out.println("================================");
            System.out.println("Server started on port 5000");
            System.out.println("Waiting for users...\n");

            while (true) {

                Socket socket = server.accept();

                ClientHandler handler = new ClientHandler(socket);
                handler.start();
            }

        } catch (IOException e) {

            System.out.println("Server Error: " + e.getMessage());
        }
    }
}
