package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateDB {

    public static void saveUserID(String userID) {
        String url = "jdbc:mysql://10.34.6.84:3306/";
        String user = "root";
        String password = "password";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            Statement statement = connection.createStatement();

            String sqlCreateDatabase = "CREATE DATABASE IF NOT EXISTS db_forum";
            statement.executeUpdate(sqlCreateDatabase);

            String sqlUseDatabase = "USE db_forum";
            statement.executeUpdate(sqlUseDatabase);

            String sqlCreateTable = "CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, nom VARCHAR(15))";
            statement.executeUpdate(sqlCreateTable);

            String sqlSelect = "SELECT COUNT(*) AS count FROM users WHERE nom = '" + userID + "'";
            ResultSet resultSet = statement.executeQuery(sqlSelect);
            resultSet.next();
            int count = resultSet.getInt("count");
            if (count == 0) {
                String sqlInsert = "INSERT INTO users (nom) VALUES ('" + userID + "')";
                statement.executeUpdate(sqlInsert);
                System.out.println("UserID saved to database successfully!");
            } else {
                System.out.println("UserID already exists in the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveThread(String titre, String pseudo, String question) {
        String url = "jdbc:mysql://localhost:3306/db_forum";
        String user = "root";
        String password = "password";
        String createTableSQL = "CREATE TABLE IF NOT EXISTS threads (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "userID VARCHAR(255)," +
                "titre VARCHAR(255)," +
                "description TEXT)";
        String insertSQL = "INSERT INTO threads (userID, titre, description) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {

            statement.executeUpdate(createTableSQL);

            insertStatement.setString(1, pseudo);
            insertStatement.setString(2, titre);
            insertStatement.setString(3, question);
            insertStatement.executeUpdate();
            System.out.println("Thread saved successfully.");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        String createMessagesTableSQL = "CREATE TABLE IF NOT EXISTS messages (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "threadID INT," +
                "userID VARCHAR(255)," +
                "message TEXT," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (threadID) REFERENCES threads(id))";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(createMessagesTableSQL);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

   
    public static String getThreadsFromDB() {
        StringBuilder response = new StringBuilder();
        String url = "jdbc:mysql://localhost:3306/db_forum";
        String user = "root";
        String password = "password";
        String sql = "SELECT * FROM threads";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String titre = resultSet.getString("titre");
                String userID = resultSet.getString("userID");
                String description = resultSet.getString("description");
                response.append(id).append(", ").append(titre).append(", ").append(userID).append(", ").append(description).append("\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }
    
    public static String getThreadById(String threadId) {
        StringBuilder response = new StringBuilder();
        String url = "jdbc:mysql://localhost:3306/db_forum";
        String user = "root";
        String password = "password";
    
        String threadSQL = "SELECT * FROM threads WHERE id = ?";
        String messagesSQL = "SELECT * FROM messages WHERE threadID = ? ORDER BY timestamp ASC";
    
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement threadStmt = connection.prepareStatement(threadSQL);
             PreparedStatement messagesStmt = connection.prepareStatement(messagesSQL)) {
    
            threadStmt.setString(1, threadId);
            ResultSet threadRS = threadStmt.executeQuery();
            if (threadRS.next()) {
                String titre = threadRS.getString("titre");
                String userID = threadRS.getString("userID");
                String description = threadRS.getString("description");
                response.append(titre).append(",").append(userID).append(",").append(description).append("\n");
            }
    
            messagesStmt.setString(1, threadId);
            ResultSet messagesRS = messagesStmt.executeQuery();
            while (messagesRS.next()) {
                String messageUserID = messagesRS.getString("userID");
                String message = messagesRS.getString("message");
                String timestamp = messagesRS.getString("timestamp");
                response.append(messageUserID).append(",").append(message).append(",").append(timestamp).append("\n");
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return response.toString();
    }

    public static void saveMessage(String threadID, String userID, String message) {
        String url = "jdbc:mysql://localhost:3306/db_forum";
        String user = "root";
        String password = "password";
        String insertSQL = "INSERT INTO messages (threadID, userID, message) VALUES (?, ?, ?)";
    
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {
    
            insertStatement.setString(1, threadID);
            insertStatement.setString(2, userID);
            insertStatement.setString(3, message);
            insertStatement.executeUpdate();
            System.out.println("Message saved successfully.");
    
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    
}