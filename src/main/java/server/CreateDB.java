package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class to handle database operations related to forum functionality.
 */
public class CreateDB {

    public static String url = "jdbc:mysql://10.34.6.84:3306/db_forum";
    public static String user = "root";
    public static String password = "password";
        
    /**
     * Saves a user ID to the database if it doesn't already exist.
     *
     * @param userID The user ID to save.
     */
    public static void saveUserID(String userID) {

        String url = "jdbc:mysql://10.34.6.84:3306/";
        String user = "root";
        String password = "password";
        
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            Statement statement = connection.createStatement();

            // Create database if it doesn't exist
            String sqlCreateDatabase = "CREATE DATABASE IF NOT EXISTS db_forum";
            statement.executeUpdate(sqlCreateDatabase);

            // Use the forum database
            String sqlUseDatabase = "USE db_forum";
            statement.executeUpdate(sqlUseDatabase);

            // Create users table if it doesn't exist
            String sqlCreateTable = "CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, nom VARCHAR(15))";
            statement.executeUpdate(sqlCreateTable);

            // Check if the user ID already exists
            String sqlSelect = "SELECT COUNT(*) AS count FROM users WHERE nom = '" + userID + "'";
            ResultSet resultSet = statement.executeQuery(sqlSelect);
            resultSet.next();
            int count = resultSet.getInt("count");

            // Insert the user ID if it doesn't exist
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

    /**
     * Saves a thread with its title, creator's pseudonym, and question to the database.
     *
     * @param titre    The title of the thread.
     * @param pseudo   The pseudonym of the thread creator.
     * @param question The question associated with the thread.
     */
    public static void saveThread(String titre, String pseudo, String question) {
        
        // SQL statements to create necessary tables and insert thread information
        String createThreadsTableSQL = "CREATE TABLE IF NOT EXISTS threads (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "userID VARCHAR(255)," +
                "titre VARCHAR(255)," +
                "description TEXT)";
        String insertThreadSQL = "INSERT INTO threads (userID, titre, description) VALUES (?, ?, ?)";

        String createMessagesTableSQL = "CREATE TABLE IF NOT EXISTS messages (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "threadID INT," +
                "userID VARCHAR(255)," +
                "message TEXT," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "likes INT DEFAULT 0," +
                "dislikes INT DEFAULT 0," +
                "FOREIGN KEY (threadID) REFERENCES threads(id))";

        String createUserReactionsTableSQL = "CREATE TABLE IF NOT EXISTS user_reactions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "userID VARCHAR(255)," +
                "messageID INT," +
                "reaction ENUM('like', 'dislike')," +
                "UNIQUE KEY (userID, messageID))";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             PreparedStatement insertStatement = connection.prepareStatement(insertThreadSQL)) {

            // Create the threads table if it doesn't exist
            statement.executeUpdate(createThreadsTableSQL);

            // Insert the new thread into the threads table
            insertStatement.setString(1, pseudo);
            insertStatement.setString(2, titre);
            insertStatement.setString(3, question);
            insertStatement.executeUpdate();
            System.out.println("Thread saved successfully.");

            // Create the messages table if it doesn't exist
            statement.executeUpdate(createMessagesTableSQL);

            // Create the user_reactions table if it doesn't exist
            statement.executeUpdate(createUserReactionsTableSQL);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Retrieves all threads from the database and returns them as a formatted string.
     *
     * @return A string containing all threads in the database.
     */
    public static String getThreadsFromDB() {
        StringBuilder response = new StringBuilder();
        String sql = "SELECT * FROM threads";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            // Append each thread's information to the response StringBuilder
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

    /**
     * Retrieves a specific thread and its associated messages from the database.
     *
     * @param threadId The ID of the thread to retrieve.
     * @return A string containing the thread's information and its messages.
     */
    public static String getThreadById(String threadId) {
        StringBuilder response = new StringBuilder();
        
        String threadSQL = "SELECT * FROM threads WHERE id = ?";
        String messagesSQL = "SELECT * FROM messages WHERE threadID = ? ORDER BY timestamp ASC";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement threadStmt = connection.prepareStatement(threadSQL);
             PreparedStatement messagesStmt = connection.prepareStatement(messagesSQL)) {

            // Retrieve thread information
            threadStmt.setString(1, threadId);
            ResultSet threadRS = threadStmt.executeQuery();
            if (threadRS.next()) {
                String titre = threadRS.getString("titre");
                String userID = threadRS.getString("userID");
                String description = threadRS.getString("description");
                response.append(titre).append(",").append(userID).append(",").append(description).append("\n");
            }

            // Retrieve associated messages
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

    /**
     * Saves a message to the database for a specific thread.
     *
     * @param threadID The ID of the thread to which the message belongs.
     * @param userID   The ID of the user posting the message.
     * @param message  The message content.
     * @return 0 if successful, or an error code otherwise (currently unhandled).
     */
    public static int saveMessage(String threadID, String userID, String message) {
        String insertSQL = "INSERT INTO messages (threadID, userID, message) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement insertStatement = connection.prepareStatement(insertSQL)) {

            // Insert the message into the messages table
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

    /**
     * Increases the like count for a message in the database.
     *
     * @param messageId The ID of the message to like.
     * @return The updated number of likes if successful, or -1 if an error occurs.
     */
    public static int likeMessage(String messageId) {
        
        String updateSQL = "UPDATE messages SET likes = likes + 1 WHERE id = ?";
        String selectSQL = "SELECT likes FROM messages WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement updateStmt = connection.prepareStatement(updateSQL);
             PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {

            // Update the like count for the specified message ID
            updateStmt.setString(1, messageId);
            updateStmt.executeUpdate();

            // Retrieve the updated like count
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

    /**
     * Increases the dislike count for a message in the database.
     *
     * @param messageId The ID of the message to dislike.
     * @return The updated number of dislikes if successful, or -1 if an error occurs.
     */
    public static int dislikeMessage(String messageId) {
        
        String updateSQL = "UPDATE messages SET dislikes = dislikes + 1 WHERE id = ?";
        String selectSQL = "SELECT dislikes FROM messages WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement updateStmt = connection.prepareStatement(updateSQL);
             PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {

            // Update the dislike count for the specified message ID
            updateStmt.setString(1, messageId);
            updateStmt.executeUpdate();

            // Retrieve the updated dislike count
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

    /**
     * Updates the like or dislike count for a message based on the reaction type (like or dislike).
     *
     * @param messageId The ID of the message to update.
     * @param isLike    Indicates whether it's a like (true) or dislike (false).
     */
    public static void updateLikes(String messageId, boolean isLike) {
        String updateSQL = isLike ?
                "UPDATE messages SET likes = likes + 1 WHERE id = ?" :
                "UPDATE messages SET dislikes = dislikes + 1 WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement updateStatement = connection.prepareStatement(updateSQL)) {

            // Update the like or dislike count for the specified message ID
            updateStatement.setString(1, messageId);
            updateStatement.executeUpdate();
            System.out.println((isLike ? "Like" : "Dislike") + " updated successfully.");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Handles user reactions (likes or dislikes) to messages in the database.
     *
     * @param userID    The ID of the user reacting.
     * @param messageID The ID of the message being reacted to.
     * @param isLike    Indicates whether it's a like (true) or dislike (false).
     */
    public static void handleReaction(String userID, String messageID, boolean isLike) {
        
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            connection.setAutoCommit(false);

            // Check if the user has already reacted to the message
            String selectSQL = "SELECT reaction FROM user_reactions WHERE userID = ? AND messageID = ?";
            try (PreparedStatement selectStmt = connection.prepareStatement(selectSQL)) {
                selectStmt.setString(1, userID);
                selectStmt.setString(2, messageID);
                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    String existingReaction = rs.getString("reaction");

                    // If the new reaction is the same as the existing one, do nothing
                    if ((isLike && "like".equals(existingReaction)) || (!isLike && "dislike".equals(existingReaction))) {
                        return;
                    }

                    // Update the reaction
                    String updateReactionSQL = "UPDATE user_reactions SET reaction = ? WHERE userID = ? AND messageID = ?";
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateReactionSQL)) {
                        updateStmt.setString(1, isLike ? "like" : "dislike");
                        updateStmt.setString(2, userID);
                        updateStmt.setString(3, messageID);
                        updateStmt.executeUpdate();
                    }

                    // Update the message like/dislike count
                    String updateMessageSQL = isLike ?
                            "UPDATE messages SET likes = likes + 1, dislikes = dislikes - 1 WHERE id = ?" :
                            "UPDATE messages SET likes = likes - 1, dislikes = dislikes + 1 WHERE id = ?";
                    try (PreparedStatement updateMessageStmt = connection.prepareStatement(updateMessageSQL)) {
                        updateMessageStmt.setString(1, messageID);
                        updateMessageStmt.executeUpdate();
                    }
                } else {
                    // Insert the new reaction
                    String insertReactionSQL = "INSERT INTO user_reactions (userID, messageID, reaction) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertReactionSQL)) {
                        insertStmt.setString(1, userID);
                        insertStmt.setString(2, messageID);
                        insertStmt.setString(3, isLike ? "like" : "dislike");
                        insertStmt.executeUpdate();
                    }

                    // Update the message like/dislike count
                    String updateMessageSQL = isLike ?
                            "UPDATE messages SET likes = likes + 1 WHERE id = ?" :
                            "UPDATE messages SET dislikes = dislikes + 1 WHERE id = ?";
                    try (PreparedStatement updateMessageStmt = connection.prepareStatement(updateMessageSQL)) {
                        updateMessageStmt.setString(1, messageID);
                        updateMessageStmt.executeUpdate();
                    }
                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes a message from the database based on its ID.
     *
     * @param messageId The ID of the message to delete.
     */
    public static boolean deleteMessageById(String messageID) {
        String sql = "DELETE FROM messages WHERE id = ?";
    
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
    
            statement.setString(1, messageID);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
    
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    
        return false;
    }

    /**
     * Deletes a thread and all associated messages from the database.
     *
     * @param threadId The ID of the thread to delete.
     */
    public static void deleteThread(String threadId) {
        String deleteMessagesSQL = "DELETE FROM messages WHERE threadID = ?";
        String deleteThreadSQL = "DELETE FROM threads WHERE id = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement deleteMessagesStmt = connection.prepareStatement(deleteMessagesSQL);
             PreparedStatement deleteThreadStmt = connection.prepareStatement(deleteThreadSQL)) {

            // Delete messages associated with the thread
            deleteMessagesStmt.setString(1, threadId);
            deleteMessagesStmt.executeUpdate();

            // Delete the thread itself
            deleteThreadStmt.setString(1, threadId);
            deleteThreadStmt.executeUpdate();

            System.out.println("Thread and associated messages deleted successfully.");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

}
