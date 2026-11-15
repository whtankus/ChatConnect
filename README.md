# ChatConnect

ChatConnect is a desktop, real-time messaging application built with **Java Swing** and **Java Socket Programming**. It supports group conversations, private messages, and a live list of connected users.

## Features

- Desktop chat interface built with Java Swing
- Group messaging for all connected users
- Private messages between two connected users
- Live online-user list
- Separate in-memory conversation views for group and private chats
- Server/client architecture using TCP sockets

## Requirements

- JDK 8 or newer
- A terminal (or an IDE such as IntelliJ IDEA, Eclipse, or VS Code with Java support)

## Run locally

The server must be running before clients connect. The current client connects to `localhost:5000`.

### 1. Compile

Run these commands from the repository root:

```bash
javac -d out src/*.java
```

### 2. Start the server

In the first terminal:

```bash
java -cp out ChatServer
```

The server listens on TCP port **5000**.

### 3. Start one or more clients

In a second terminal (and additional terminals for more users):

```bash
java -cp out ChatClient
```

Enter a username in the login window and connect. Use **Group Chat** for a room-wide conversation, or select an online member to open a private conversation.

### Stop the application

Close a client window to disconnect. Stop the server with `Ctrl+C`.

## Project structure

```text
ChatConnect/
├── .github/
│   └── workflows/
│       └── java-ci.yml
├── src/
│   ├── ChatClient.java       # Swing desktop client and chat UI
│   ├── ChatServer.java       # TCP server and message routing
│   └── ClientHandler.java    # One server-side handler per client
└── README.md
```

## Troubleshooting

- **“Server unavailable”**: Start `ChatServer` first and make sure TCP port 5000 is available.
- **Clients on another computer cannot connect**: The current client uses `localhost`, so it only connects to a server on the same computer. Network-host configuration is not exposed yet.
- **Port 5000 is already in use**: Stop the other process using that port before starting the server.

## Current limitations

This is a learning/demo project, not a production messaging service. Messages and conversation history are held in memory and are not persisted. The current socket protocol does not provide transport encryption or user authentication. Do not use it to send sensitive information.

## Contributing

Contributions are welcome. For a proposed change, create a feature branch, keep the change focused, compile the Java sources, and describe how the change was tested in the pull request.
