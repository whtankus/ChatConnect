
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class ChatClient extends JFrame {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String username;

    private JPanel messagesPanel;
    private JScrollPane scrollPane;
    private JTextField messageField;
    private JLabel statusLabel;
    private JLabel chatHeading;
    private JLabel chatSubheading;

    private DefaultListModel<String> onlineUsersModel;
    private JList<String> onlineUsersList;
    private JLabel onlineCountLabel;

    // null means group chat; otherwise it is a private recipient
    private String activeChat = null;

    // Separate conversation histories
    private Map<String, List<ChatMessage>> conversations
            = new HashMap<>();

    private final Color NAVY = new Color(15, 23, 42);
    private final Color BLUE = new Color(59, 130, 246);
    private final Color BG = new Color(11, 18, 32);
    private final Color TEXT = new Color(226, 232, 240);
    private final Color BORDER = new Color(36, 50, 71);
    private final Color SURFACE = new Color(17, 28, 46);
    private final Color MUTED = new Color(148, 163, 184);
    private final Color OWN_BUBBLE = new Color(30, 58, 95);
    private final Color OTHER_BUBBLE = new Color(30, 41, 59);

    // Message model
    private static class ChatMessage {

        String sender;
        String content;
        boolean ownMessage;
        String time;

        ChatMessage(String sender, String content,
                boolean ownMessage) {

            this.sender = sender;
            this.content = content;
            this.ownMessage = ownMessage;

            this.time = new SimpleDateFormat("hh:mm a")
                    .format(new Date());
        }
    }

    // ================= LOGIN SCREEN =================
    private static class LoginFrame extends JFrame {

        private JTextField usernameField;
        private JButton connectButton;
        private JLabel statusLabel;

        private final Color NAVY = new Color(15, 23, 42);
        private final Color BLUE = new Color(59, 130, 246);
        private final Color BG = new Color(11, 18, 32);
        private final Color SURFACE = new Color(17, 28, 46);
        private final Color TEXT = new Color(226, 232, 240);
        private final Color MUTED = new Color(148, 163, 184);
        private final Color BORDER = new Color(36, 50, 71);

        LoginFrame() {

            setTitle("ChatConnect | Login");
            setSize(470, 540);
            setMinimumSize(new Dimension(420, 500));
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);
            setResizable(false);

            JPanel mainPanel = new JPanel(new GridBagLayout());
            mainPanel.setBackground(BG);

            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBackground(SURFACE);
            card.setBorder(new CompoundBorder(
                    new LineBorder(BORDER, 1, true),
                    new EmptyBorder(35, 38, 35, 38)
            ));

            JLabel logo = new JLabel("C");
            logo.setOpaque(true);
            logo.setBackground(new Color(30, 58, 95));
            logo.setForeground(BLUE);
            logo.setFont(new Font("SansSerif", Font.BOLD, 30));
            logo.setHorizontalAlignment(SwingConstants.CENTER);
            logo.setPreferredSize(new Dimension(65, 65));
            logo.setMaximumSize(new Dimension(65, 65));
            logo.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel title = new JLabel("ChatConnect");
            title.setFont(new Font("SansSerif", Font.BOLD, 27));
            title.setForeground(TEXT);
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel subtitle = new JLabel(
                    "Connect. Communicate. Collaborate."
            );
            subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
            subtitle.setForeground(MUTED);
            subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel welcome = new JLabel("Welcome back!");
            welcome.setFont(new Font("SansSerif", Font.BOLD, 19));
            welcome.setForeground(TEXT);
            welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel description = new JLabel(
                    "Enter your username to join the conversation."
            );
            description.setFont(new Font("SansSerif", Font.PLAIN, 12));
            description.setForeground(MUTED);
            description.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel usernameLabel = new JLabel("USERNAME");
            usernameLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            usernameLabel.setForeground(TEXT);
            usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            usernameField = new JTextField();
            usernameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
            usernameField.setBackground(BG);
            usernameField.setForeground(TEXT);
            usernameField.setCaretColor(BLUE);
            usernameField.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 45)
            );
            usernameField.setBorder(new CompoundBorder(
                    new LineBorder(BORDER, 1, true),
                    new EmptyBorder(10, 13, 10, 13)
            ));

            connectButton = new JButton("Connect to Chat");
            connectButton.setFont(new Font("SansSerif", Font.BOLD, 14));
            connectButton.setForeground(Color.WHITE);
            connectButton.setBackground(BLUE);
            connectButton.setFocusPainted(false);
            connectButton.setBorder(new EmptyBorder(14, 15, 14, 15));
            connectButton.setCursor(
                    new Cursor(Cursor.HAND_CURSOR)
            );
            connectButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            connectButton.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 48)
            );

            statusLabel = new JLabel(" ");
            statusLabel.setFont(
                    new Font("SansSerif", Font.PLAIN, 12)
            );
            statusLabel.setForeground(new Color(248, 113, 113));
            statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel footer = new JLabel(
                    "Real-Time Messaging Application"
            );
            footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
            footer.setForeground(MUTED);
            footer.setAlignmentX(Component.CENTER_ALIGNMENT);

            card.add(logo);
            card.add(Box.createVerticalStrut(17));
            card.add(title);
            card.add(Box.createVerticalStrut(5));
            card.add(subtitle);
            card.add(Box.createVerticalStrut(35));
            card.add(welcome);
            card.add(Box.createVerticalStrut(7));
            card.add(description);
            card.add(Box.createVerticalStrut(25));
            card.add(usernameLabel);
            card.add(Box.createVerticalStrut(8));
            card.add(usernameField);
            card.add(Box.createVerticalStrut(20));
            card.add(connectButton);
            card.add(Box.createVerticalStrut(10));
            card.add(statusLabel);
            card.add(Box.createVerticalStrut(20));
            card.add(footer);

            mainPanel.add(card);
            add(mainPanel);

            connectButton.addActionListener(e -> connect());
            usernameField.addActionListener(e -> connect());

            setVisible(true);
            usernameField.requestFocusInWindow();
        }

        private void connect() {

            String enteredUsername = usernameField.getText().trim();

            if (enteredUsername.length() < 2
                    || enteredUsername.length() > 20
                    || !enteredUsername.matches("[A-Za-z0-9_ ]+")) {

                statusLabel.setText(
                        "Use 2-20 letters, numbers, spaces or underscores."
                );
                return;
            }

            connectButton.setEnabled(false);
            usernameField.setEnabled(false);
            connectButton.setText("Connecting...");
            statusLabel.setForeground(BLUE);
            statusLabel.setText("Connecting to server...");

            SwingWorker<ConnectionData, Void> worker
                    = new SwingWorker<ConnectionData, Void>() {

                @Override
                protected ConnectionData doInBackground()
                        throws Exception {

                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress("localhost", 5000), 5000);
                    socket.setTcpNoDelay(true);
                    socket.setKeepAlive(true);

                    BufferedReader input = new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream()
                            )
                    );

                    PrintWriter output = new PrintWriter(
                            socket.getOutputStream(), true
                    );

                    output.println(enteredUsername);

                    return new ConnectionData(
                            socket, input, output
                    );
                }

                @Override
                protected void done() {

                    try {

                        ConnectionData connection = get();

                        dispose();

                        SwingUtilities.invokeLater(()
                                -> new ChatClient(
                                        connection.socket,
                                        enteredUsername,
                                        connection.input,
                                        connection.output
                                )
                        );

                    } catch (Exception ex) {

                        connectButton.setEnabled(true);
                        usernameField.setEnabled(true);
                        connectButton.setText("Connect to Chat");

                        statusLabel.setForeground(
                                new Color(248, 113, 113)
                        );

                        statusLabel.setText(
                                "Server unavailable. Start ChatServer first."
                        );

                        usernameField.requestFocusInWindow();
                    }
                }
            };

            worker.execute();
        }
    }

    private static class ConnectionData {

        Socket socket;
        BufferedReader input;
        PrintWriter output;

        ConnectionData(Socket socket,
                BufferedReader input,
                PrintWriter output) {

            this.socket = socket;
            this.input = input;
            this.output = output;
        }
    }

    // ================= CHAT WINDOW =================
    public ChatClient(Socket socket, String username,
            BufferedReader input, PrintWriter output) {

        this.socket = socket;
        this.username = username;
        this.input = input;
        this.output = output;

        conversations.put("GROUP", new ArrayList<>());

        createGUI();
        receiveMessages();
    }

    private void createGUI() {

        setTitle("ChatConnect | Real-Time Messenger");
        setSize(1050, 680);
        setMinimumSize(new Dimension(750, 500));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BG);

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(17, 25, 17, 25));

        JLabel appTitle = new JLabel("ChatConnect");
        appTitle.setForeground(Color.WHITE);
        appTitle.setFont(new Font("SansSerif", Font.BOLD, 23));

        statusLabel = new JLabel("● Connected");
        statusLabel.setForeground(new Color(52, 211, 153));
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        header.add(appTitle, BorderLayout.WEST);
        header.add(statusLabel, BorderLayout.EAST);

        // SIDEBAR
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(235, 0));
        sidebar.setBackground(SURFACE);
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, BORDER));

        JLabel profileTitle = new JLabel("YOUR PROFILE");
        profileTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        profileTitle.setForeground(MUTED);
        profileTitle.setBorder(new EmptyBorder(25, 20, 15, 10));

        JPanel profilePanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 12, 8)
        );
        profilePanel.setBackground(SURFACE);
        profilePanel.setMaximumSize(new Dimension(235, 65));

        JLabel avatar = new JLabel(
                username.substring(0, 1).toUpperCase()
        );
        avatar.setOpaque(true);
        avatar.setBackground(OWN_BUBBLE);
        avatar.setForeground(BLUE);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 20));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(40, 40));

        JLabel userName = new JLabel(username);
        userName.setFont(new Font("SansSerif", Font.BOLD, 15));
        userName.setForeground(TEXT);

        profilePanel.add(avatar);
        profilePanel.add(userName);

        JLabel roomTitle = new JLabel("CONVERSATIONS");
        roomTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        roomTitle.setForeground(MUTED);
        roomTitle.setBorder(new EmptyBorder(25, 20, 12, 10));

        // GROUP CHAT BUTTON
        JButton groupButton = new JButton("●   Group Chat");
        styleConversationButton(groupButton);
        groupButton.addActionListener(e -> openGroupChat());

        JLabel onlineTitle = new JLabel("ONLINE MEMBERS");
        onlineTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        onlineTitle.setForeground(MUTED);
        onlineTitle.setBorder(new EmptyBorder(25, 20, 10, 10));

        onlineCountLabel = new JLabel("0");
        onlineCountLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        onlineCountLabel.setForeground(BLUE);

        JPanel onlineHeading = new JPanel(new BorderLayout());
        onlineHeading.setBackground(SURFACE);
        onlineHeading.setBorder(new EmptyBorder(0, 20, 10, 15));
        onlineHeading.setMaximumSize(new Dimension(235, 30));

        onlineHeading.add(onlineTitle, BorderLayout.WEST);
        onlineHeading.add(onlineCountLabel, BorderLayout.EAST);

        onlineUsersModel = new DefaultListModel<>();
        onlineUsersList = new JList<>(onlineUsersModel);

        onlineUsersList.setBackground(SURFACE);
        onlineUsersList.setForeground(TEXT);
        onlineUsersList.setFont(
                new Font("SansSerif", Font.PLAIN, 13)
        );
        onlineUsersList.setFixedCellHeight(35);
        onlineUsersList.setSelectionBackground(
                new Color(30, 58, 95)
        );
        onlineUsersList.setSelectionForeground(TEXT);
        onlineUsersList.setBorder(new EmptyBorder(5, 12, 5, 5));

        onlineUsersList.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index,
                        isSelected, cellHasFocus
                );

                String name = value.toString();

                if (name.equalsIgnoreCase(username)) {
                    name += " (You)";
                }

                label.setText("●   " + name);
                label.setForeground(isSelected ? new Color(147, 197, 253) : TEXT);
                label.setFont(
                        new Font("SansSerif", Font.PLAIN, 13)
                );
                label.setBorder(new EmptyBorder(5, 8, 5, 5));

                return label;
            }
        });

        // CLICK AN ONLINE USER TO OPEN PRIVATE CHAT
        onlineUsersList.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                int index = onlineUsersList.locationToIndex(
                        e.getPoint()
                );

                if (index >= 0) {

                    String selectedUser
                            = onlineUsersModel.getElementAt(index);

                    if (!selectedUser.equalsIgnoreCase(username)) {
                        openPrivateChat(selectedUser);
                    }
                }
            }
        });

        sidebar.add(profileTitle);
        sidebar.add(profilePanel);
        sidebar.add(roomTitle);
        sidebar.add(groupButton);
        sidebar.add(onlineHeading);
        sidebar.add(onlineUsersList);

        // CHAT PANEL
        JPanel chatPanel = new JPanel(new BorderLayout(0, 12));
        chatPanel.setBackground(BG);
        chatPanel.setBorder(new EmptyBorder(22, 25, 20, 25));

        chatHeading = new JLabel("Group Conversation");
        chatHeading.setFont(new Font("SansSerif", Font.BOLD, 20));
        chatHeading.setForeground(TEXT);

        chatSubheading = new JLabel(
                "Messages are visible to everyone in the group"
        );
        chatSubheading.setFont(
                new Font("SansSerif", Font.PLAIN, 12)
        );
        chatSubheading.setForeground(MUTED);

        JPanel headingContent = new JPanel();
        headingContent.setLayout(new BoxLayout(
                headingContent, BoxLayout.Y_AXIS
        ));
        headingContent.setBackground(BG);

        headingContent.add(chatHeading);
        headingContent.add(Box.createVerticalStrut(5));
        headingContent.add(chatSubheading);

        // MESSAGES
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(
                messagesPanel, BoxLayout.Y_AXIS
        ));
        messagesPanel.setBackground(SURFACE);
        messagesPanel.setBorder(new EmptyBorder(18, 15, 18, 15));

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBorder(new LineBorder(BORDER, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(SURFACE);

        // MESSAGE INPUT
        JPanel inputPanel = new JPanel(new BorderLayout(12, 0));
        inputPanel.setBackground(BG);

        messageField = new JTextField();
        messageField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageField.setBackground(SURFACE);
        messageField.setCaretColor(BLUE);
        messageField.setToolTipText("Press Enter to send a message");
        messageField.setForeground(TEXT);
        messageField.setBorder(
                new CompoundBorder(
                        new LineBorder(
                                BORDER, 1, true
                        ),
                        new EmptyBorder(12, 14, 12, 14)
                )
        );

        JButton sendButton = new JButton("Send");
        sendButton.setBackground(BLUE);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        sendButton.setFocusPainted(false);
        sendButton.setBorder(new EmptyBorder(12, 28, 12, 28));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(headingContent, BorderLayout.NORTH);
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(chatPanel, BorderLayout.CENTER);

        add(mainPanel);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                disconnect();
            }
        });

        setVisible(true);
        messageField.requestFocusInWindow();

        addSystemMessage("You joined the group conversation.");
    }

    private void styleConversationButton(JButton button) {

        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(TEXT);
        button.setBackground(new Color(30, 58, 95));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 20, 12, 10));
        button.setMaximumSize(new Dimension(235, 42));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // ================= CONVERSATION SWITCHING =================
    private void openGroupChat() {

        activeChat = null;

        chatHeading.setText("Group Conversation");
        chatSubheading.setText(
                "Messages are visible to everyone in the group"
        );

        messageField.setToolTipText("Message everyone...");
        refreshConversation();
    }

    private void openPrivateChat(String recipient) {

        activeChat = recipient;

        conversations.putIfAbsent(
                recipient, new ArrayList<>()
        );

        chatHeading.setText("Chat with " + recipient);
        chatSubheading.setText(
                "Private conversation • Only you and "
                + recipient + " can see these messages"
        );

        messageField.setToolTipText(
                "Message " + recipient + " privately..."
        );

        refreshConversation();
    }

    private void refreshConversation() {

        messagesPanel.removeAll();

        String key = activeChat == null ? "GROUP" : activeChat;

        List<ChatMessage> history = conversations.get(key);

        if (history != null) {

            for (ChatMessage message : history) {
                addMessageBubble(message);
            }
        }

        messagesPanel.revalidate();
        messagesPanel.repaint();

        SwingUtilities.invokeLater(() -> {

            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    // ================= SEND MESSAGE =================
    private void sendMessage() {

        String message = messageField.getText().trim();

        if (message.isEmpty() || output == null) {
            return;
        }

        if (message.length() > 2000) {
            addSystemMessage("Messages are limited to 2,000 characters.");
            return;
        }

        String encodedMessage = Base64.getEncoder().encodeToString(
                message.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        if (activeChat == null) {

            // GROUP MESSAGE
            output.println("GROUP|" + encodedMessage);
            if (output.checkError()) {
                connectionLost();
                return;
            }

        } else {

            // PRIVATE MESSAGE
            output.println(
                    "PRIVATE|" + activeChat + "|" + encodedMessage
            );
            if (output.checkError()) {
                connectionLost();
                return;
            }
        }

        messageField.setText("");
        messageField.requestFocusInWindow();
    }

    // ================= RECEIVE MESSAGES =================
    private void receiveMessages() {

        Thread receiveThread = new Thread(() -> {

            try {

                String message;

                while ((message = input.readLine()) != null) {

                    String receivedMessage = message;

                    SwingUtilities.invokeLater(()
                            -> handleServerMessage(receivedMessage)
                    );
                }

                SwingUtilities.invokeLater(this::connectionLost);

            } catch (IOException e) {

                SwingUtilities.invokeLater(this::connectionLost);
            }
        });

        receiveThread.setDaemon(true);
        receiveThread.start();
    }

    private void handleServerMessage(String message) {

        try {

            // ONLINE USER UPDATES
            if (message.startsWith("USERS:")) {

                updateOnlineUsers(message.substring(6));
                return;
            }

            // PRIVATE MESSAGE
            if (message.startsWith("PRIVATE|")) {

                String[] parts = message.split("\\|", 4);

                if (parts.length == 4) {

                    String sender = parts[1];
                    String recipient = parts[2];

                    String content = new String(
                            Base64.getDecoder().decode(parts[3]),
                            java.nio.charset.StandardCharsets.UTF_8
                    );

                    String conversationPartner
                            = sender.equalsIgnoreCase(username)
                            ? recipient : sender;

                    saveMessage(
                            conversationPartner,
                            sender,
                            content,
                            sender.equalsIgnoreCase(username)
                    );
                }

                return;
            }

            // GROUP MESSAGE
            if (message.startsWith("GROUP|")) {

                String[] parts = message.split("\\|", 3);

                if (parts.length == 3) {

                    String sender = parts[1];

                    String content = new String(
                            Base64.getDecoder().decode(parts[2]),
                            java.nio.charset.StandardCharsets.UTF_8
                    );

                    saveMessage(
                            "GROUP",
                            sender,
                            content,
                            sender.equalsIgnoreCase(username)
                    );
                }

                return;
            }

            // SERVER NOTIFICATIONS
            if (message.startsWith("SERVER:")) {

                if (activeChat == null) {
                    addSystemMessage(message.substring(7).trim());
                }

                return;
            }

            // SERVER ERRORS
            if (message.startsWith("ERROR:")) {

                if (message.startsWith("ERROR:USER_NOT_FOUND:")) {

                    JOptionPane.showMessageDialog(
                            this,
                            "This user is no longer online.",
                            "User unavailable",
                            JOptionPane.WARNING_MESSAGE
                    );

                } else if (message.equals("ERROR:SELF_MESSAGE")) {

                    JOptionPane.showMessageDialog(
                            this,
                            "You cannot send a private message to yourself."
                    );

                } else {

                    JOptionPane.showMessageDialog(
                            this,
                            "Unable to send this message."
                    );
                }
            }

        } catch (Exception e) {

            System.out.println(
                    "Message processing error: " + e.getMessage()
            );
        }
    }

    // SAVE MESSAGE IN THE CORRECT CONVERSATION
    private void saveMessage(String key, String sender,
            String content, boolean ownMessage) {

        conversations.putIfAbsent(key, new ArrayList<>());

        ChatMessage chatMessage = new ChatMessage(
                sender, content, ownMessage
        );

        conversations.get(key).add(chatMessage);

        if (Objects.equals(activeChat == null ? "GROUP" : activeChat, key)) {

            addMessageBubble(chatMessage);
        }
    }

    // ================= ONLINE USERS =================
    private void updateOnlineUsers(String users) {

        onlineUsersModel.clear();

        if (!users.trim().isEmpty()) {

            String[] userArray = users.split(",");

            for (String user : userArray) {

                if (!user.trim().isEmpty()) {
                    onlineUsersModel.addElement(user.trim());
                }
            }
        }

        onlineCountLabel.setText(
                String.valueOf(onlineUsersModel.size())
        );
    }

    // ================= MESSAGE BUBBLES =================
    private void addMessageBubble(ChatMessage message) {

        JPanel messageRow = new JPanel(
                new FlowLayout(
                        message.ownMessage
                                ? FlowLayout.RIGHT
                                : FlowLayout.LEFT,
                        8, 5
                )
        );

        messageRow.setBackground(SURFACE);
        messageRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel bubble = new JPanel(new BorderLayout(0, 7));

        bubble.setBackground(
                message.ownMessage
                        ? OWN_BUBBLE
                        : OTHER_BUBBLE
        );

        bubble.setBorder(new EmptyBorder(11, 14, 9, 14));

        JLabel senderLabel = new JLabel(
                message.ownMessage ? "You" : message.sender
        );

        senderLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        senderLabel.setForeground(
                message.ownMessage ? BLUE : TEXT
        );

        JTextArea messageText = new JTextArea(message.content);
        messageText.setEditable(false);
        messageText.setLineWrap(true);
        messageText.setWrapStyleWord(true);
        messageText.setOpaque(false);
        messageText.setFont(new Font("SansSerif", Font.PLAIN, 14));
        messageText.setForeground(TEXT);
        messageText.setBorder(null);

        int availableWidth = Math.max(
                180,
                Math.min(420, scrollPane.getViewport().getWidth() - 100)
        );

        messageText.setSize(
                new Dimension(availableWidth, Short.MAX_VALUE)
        );

        Dimension textSize = messageText.getPreferredSize();

        messageText.setPreferredSize(
                new Dimension(
                        Math.min(
                                availableWidth,
                                Math.max(80, textSize.width)
                        ),
                        textSize.height
                )
        );

        JLabel timeLabel = new JLabel(message.time);
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        timeLabel.setForeground(MUTED);
        timeLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        bubble.add(senderLabel, BorderLayout.NORTH);
        bubble.add(messageText, BorderLayout.CENTER);
        bubble.add(timeLabel, BorderLayout.SOUTH);

        messageRow.add(bubble);

        messagesPanel.add(messageRow);
        messagesPanel.add(Box.createVerticalStrut(5));

        messagesPanel.revalidate();
        messagesPanel.repaint();

        SwingUtilities.invokeLater(() -> {

            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    // SYSTEM MESSAGE
    private void addSystemMessage(String message) {

        JLabel label = new JLabel(message);
        label.setFont(new Font("SansSerif", Font.ITALIC, 12));
        label.setForeground(MUTED);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
        row.setBackground(SURFACE);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(label);

        messagesPanel.add(row);
        messagesPanel.add(Box.createVerticalStrut(8));

        messagesPanel.revalidate();
        messagesPanel.repaint();
    }

    // ================= CONNECTION =================
    private void connectionLost() {

        if (statusLabel == null || !isDisplayable()) {
            return;
        }

        statusLabel.setText("● Disconnected");
        statusLabel.setForeground(new Color(248, 113, 113));

        addSystemMessage("Connection to server lost.");

        onlineUsersModel.clear();
        onlineCountLabel.setText("0");
    }

    private void disconnect() {

        try {

            if (output != null) {
                output.println("/exit");
            }

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

        } catch (IOException e) {

            System.out.println(
                    "Disconnect Error: " + e.getMessage()
            );
        }

        dispose();
    }

    // ================= MAIN =================
    public static void main(String[] args) {

        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
