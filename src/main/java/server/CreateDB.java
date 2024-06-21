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
        String url = "jdbc:mysql://10.34.6.84:3306/db_forum";
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
                "likes INT DEFAULT 0," +
                "dislikes INT DEFAULT 0," +
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
        String url = "jdbc:mysql://10.34.6.84:3306/db_forum";
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
        String url = "jdbc:mysql://10.34.6.84:3306/db_forum";
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
                String messageID = messagesRS.getString("id");
                String messageUserID = messagesRS.getString("userID");
                String message = messagesRS.getString("message");
                String timestamp = messagesRS.getString("timestamp");
                int likes = messagesRS.getInt("likes");
                int dislikes = messagesRS.getInt("dislikes");
                response.append(messageID).append(",").append(messageUserID).append(",").append(message)
                        .append(",").append(timestamp).append(",").append(likes).append(",").append(dislikes).append("\n");
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return response.toString();
    }
    

    public static int saveMessage(String threadID, String userID, String message) {
        String url = "jdbc:mysql://10.34.6.84:3306/db_forum";
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
        return 0;
    }
    
    public static int likeMessage(String messageId) {
        String url = "jdbc:mysql://10.34.6.84:3306/db_forum";
        String user = "root";
        String password = "password";
        
        String updateSQL = "UPDATE messages SET likes = likes + 1 WHERE id = ?";
        String selectSQL = "SELECT likes FROM messages WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement updateStmt = connection.prepareStatement(updateSQL);
             PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {

            updateStmt.setString(1, messageId);
            updateStmt.executeUpdate();

            selectStmt.setString(1, messageId);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("likes");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int dislikeMessage(String messageId) {
        String url = "jdbc:mysql://10.34.6.84:3306/db_forum";
        String user = "root";
        String password = "password";

        String updateSQL = "UPDATE messages SET dislikes = dislikes + 1 WHERE id = ?";
        String selectSQL = "SELECT dislikes FROM messages WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement updateStmt = connection.prepareStatement(updateSQL);
             PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {

            updateStmt.setString(1, messageId);
            updateStmt.executeUpdate();

            selectStmt.setString(1, messageId);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("dislikes");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void updateLikes(String messageId, boolean isLike) {
        String url = "jdbc:mysql://10.34.6.84:3306/db_forum";
        String user = "root";
        String password = "password";
        String updateSQL = isLike ? 
            "UPDATE messages SET likes = likes + 1 WHERE id = ?" : 
            "UPDATE messages SET dislikes = dislikes + 1 WHERE id = ?";
        
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement updateStatement = connection.prepareStatement(updateSQL)) {
            
            updateStatement.setString(1, messageId);
            updateStatement.executeUpdate();
            System.out.println((isLike ? "Like" : "Dislike") + " updated successfully.");
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static void deleteMessage(String messageId) {
        String url = "jdbc:mysql://10.34.6.84:3306/db_forum";
        String user = "root";
        String password = "password";
        String deleteSQL = "DELETE FROM messages WHERE id = ?";
    
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(deleteSQL)) {
    
            statement.setString(1, messageId);
            statement.executeUpdate();
            System.out.println("Message deleted successfully.");
    
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void deleteThread(String threadId) {
        String url = "jdbc:mysql://10.34.6.84:3306/db_forum";
        String user = "root";
        String password = "password";
        String deleteMessagesSQL = "DELETE FROM messages WHERE threadID = ?";
        String deleteThreadSQL = "DELETE FROM threads WHERE id = ?";
    
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement deleteMessagesStmt = connection.prepareStatement(deleteMessagesSQL);
             PreparedStatement deleteThreadStmt = connection.prepareStatement(deleteThreadSQL)) {
    
            deleteMessagesStmt.setString(1, threadId);
            deleteMessagesStmt.executeUpdate();
    
            deleteThreadStmt.setString(1, threadId);
            deleteThreadStmt.executeUpdate();
    
            System.out.println("Thread and associated messages deleted successfully.");
    
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    

}