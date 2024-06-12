package com.forum;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;

public class ForumScreen3 extends JFrame {
    private JList<String> threadList;

    public ForumScreen3(String userID) {
        setTitle("Rejoindre un fil");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        ArrayList<String> threads = getThreadsFromDatabase();

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String thread : threads) {
            listModel.addElement(thread);
        }
        threadList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(threadList);

        add(scrollPane, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JButton createThreadButton = new JButton("Accueil");
        JButton joinThreadButton = new JButton("Créer un fil");
        JButton refreshButton = new JButton("Re-lancer"); // Bouton de rafraîchissement

        createThreadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Ouvrir la fenêtre d'accueil (ForumScreen)
                dispose();
                new ForumScreen(userID);
            }
        });

        threadList.addListSelectionListener(new ListSelectionListener() {
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            String selectedThread = threadList.getSelectedValue();
            if (selectedThread != null) {
                dispose();
                String threadInfo = getThreadInfo(selectedThread);
                new ForumScreen4(userID, threadInfo);
            }
        }
    }
});

        joinThreadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Ouvrir la fenêtre pour créer un fil (ForumTemplate)
                dispose();
                new ForumTemplate(userID);
            }
        });

        refreshButton.addActionListener(new ActionListener() { // Action pour le bouton de rafraîchissement
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                new ForumScreen3(userID); // Ouvrir une nouvelle instance de ForumScreen3
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createThreadButton);
        buttonPanel.add(joinThreadButton);
        buttonPanel.add(refreshButton); // Ajouter le bouton de rafraîchissement

        add(buttonPanel, BorderLayout.NORTH);
        menuBar.add(createThreadButton);
        menuBar.add(joinThreadButton);
        menuBar.add(refreshButton); // Ajouter le bouton de rafraîchissement au menu

        add(menuBar, BorderLayout.NORTH);

        setVisible(true);
        setLocationRelativeTo(null);
    }

    private ArrayList<String> getThreadsFromDatabase() {
        ArrayList<String> threads = new ArrayList<>();
        String url = "jdbc:mysql://localhost:3306/db_forum";
        String user = "root";
        String password = "password";
        String sqlSelect = "SELECT * FROM threads";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sqlSelect)) {
            while (resultSet.next()) {
                String userID = resultSet.getString("userID");
                String titre = resultSet.getString("titre");
                String description = resultSet.getString("description");

                threads.add("UserID: " + userID + ", Titre: " + titre + ", Description: " + description);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return threads;
    }

    private String getThreadInfo(String selectedThread) {
        String threadInfo = null;
        String url = "jdbc:mysql://localhost:3306/db_forum";
        String user = "root";
        String password = "password";
        String sqlSelect = "SELECT * FROM threads WHERE titre = ?";
    
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sqlSelect)) {
            statement.setString(1, selectedThread.split(", ")[1].split(": ")[1]);  // Извлечение заголовка из строки
            ResultSet resultSet = statement.executeQuery();
    
            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String userID = resultSet.getString("userID");
                String titre = resultSet.getString("titre");
                String description = resultSet.getString("description");
                threadInfo = id + ", UserID: " + userID + ", Titre: " + titre + ", Description: " + description;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return threadInfo;
    }
}
