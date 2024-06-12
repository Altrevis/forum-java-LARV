package com.forum;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ForumScreen4 extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private String userID;
    private int threadID;

public ForumScreen4(String userID, String selectedThreadInfo) {
    this.userID = userID;
    this.threadID = extractThreadID(selectedThreadInfo);

    setTitle("Thread");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(600, 400);
    setLayout(new BorderLayout());

    chatArea = new JTextArea();
    chatArea.setEditable(false);
    JScrollPane scrollPane = new JScrollPane(chatArea);
    add(scrollPane, BorderLayout.CENTER);

    JLabel label = new JLabel(selectedThreadInfo);
    add(label, BorderLayout.NORTH);

    JButton createThreadButton = new JButton("Créer un fil");
    JButton joinThreadButton = new JButton("Rejoindre un fil");
    JButton refreshButton = new JButton("Re-lancer");

    createThreadButton.addActionListener(e -> {
        dispose();
        new ForumTemplate(userID);
    });

    joinThreadButton.addActionListener(e -> {
        dispose();
        new ForumScreen3(userID);
    });

    refreshButton.addActionListener(e -> {
        dispose();
        new ForumScreen4(userID, selectedThreadInfo);
    });

    JPanel buttonPanel = new JPanel();
    buttonPanel.add(createThreadButton);
    buttonPanel.add(joinThreadButton);
    buttonPanel.add(refreshButton);
    add(buttonPanel, BorderLayout.NORTH);

    JPanel messagePanel = new JPanel();
    messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

    JLabel messageLabel = new JLabel("Enter Message: ");
    messageField = new JTextField(20);
    JButton sendButton = new JButton("Send");
    JButton resetButton = new JButton("Reset");

    sendButton.addActionListener(new SendButtonListener());
    resetButton.addActionListener(e -> messageField.setText(""));

    messagePanel.add(messageLabel);
    messagePanel.add(messageField);
    messagePanel.add(sendButton);
    messagePanel.add(resetButton);

    add(messagePanel, BorderLayout.SOUTH);

    loadMessages();

    setVisible(true);
    setLocationRelativeTo(null);
}

private int extractThreadID(String selectedThreadInfo) {
    return Integer.parseInt(selectedThreadInfo.split(",")[0].trim());
}

    

    private void loadMessages() {
        String url = "jdbc:mysql://localhost:3306/db_forum";
        String user = "root";
        String password = "password";
        String sqlSelect = "SELECT * FROM messages WHERE threadID = ? ORDER BY timestamp";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sqlSelect)) {
            statement.setInt(1, threadID);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String userID = resultSet.getString("userID");
                String message = resultSet.getString("message");
                chatArea.append(userID + ": " + message + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private class SendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                saveMessage(threadID, userID, message);
                chatArea.append(userID + ": " + message + "\n");
                messageField.setText("");
            }
        }

        private void saveMessage(int threadID, String userID, String message) {
            String url = "jdbc:mysql://localhost:3306/db_forum";
            String user = "root";
            String password = "password";
            String insertSQL = "INSERT INTO messages (threadID, userID, message) VALUES (?, ?, ?)";

            try (Connection connection = DriverManager.getConnection(url, user, password);
                 PreparedStatement statement = connection.prepareStatement(insertSQL)) {
                statement.setInt(1, threadID);
                statement.setString(2, userID);
                statement.setString(3, message);
                statement.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}