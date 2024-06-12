package com.forum;
import java.sql.*;
import java.util.HashMap;

public class CreateDB {
    public static void main(String[] args) {
      
    }

    public static void saveTemplate(String userID, String title, String description) {
        IDandPassword idAndPassword = new IDandPassword();
    @SuppressWarnings("unchecked")
    HashMap<String, String> loginInfo = idAndPassword.getLoginInfo();
    userID = loginInfo.keySet().iterator().next();
        
        
    
        String url = "jdbc:mysql://localhost:3306/db_foum";
        String user = "root";
        String password = "password";
	        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS forum_templates (" +
            "userID VARCHAR(50), " +
            "titre VARCHAR(100), " +
            "description TEXT" +
            ")";

      

            String sqlInsertTemplate = "INSERT INTO forum_templates (userID, titre, description) VALUES (?, ?, ?)";

            try (Connection connection = DriverManager.getConnection(url, user, password);
                 PreparedStatement createStatement = connection.prepareStatement(sqlCreateTable);
                 PreparedStatement insertStatement = connection.prepareStatement(sqlInsertTemplate)) {
    
                
                createStatement.executeUpdate();
    
                
                insertStatement.setString(1, userID);
                insertStatement.setString(2, title);
                insertStatement.setString(3, description);
                insertStatement.executeUpdate();

            System.out.println("Template enregistré dans la base de données avec succès !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void saveUserID(String userID) {
        String url = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String password = "password";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            Statement statement = connection.createStatement();

            String sqlCreateDatabase = "CREATE DATABASE IF NOT EXISTS db_forum";
            statement.executeUpdate(sqlCreateDatabase);

            String sqlUseDatabase = "USE db_foum";
            statement.executeUpdate(sqlUseDatabase);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        url = "jdbc:mysql://localhost:3306/db_forum";
        
    
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            Statement statement = connection.createStatement();
            
            String sqlCreateTable = "CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, nom VARCHAR(15))";
            statement.executeUpdate(sqlCreateTable);

            // Vérifie si userID existe déjà dans la base de données
            String sqlSelect = "SELECT COUNT(*) AS count FROM users WHERE nom = '" + userID + "'";
            ResultSet resultSet = statement.executeQuery(sqlSelect);
            resultSet.next();
            int count = resultSet.getInt("count");
            if (count == 0) {
                // Si userID n'existe pas, effectue l'insertion
                String sqlInsert = "INSERT INTO users (nom) VALUES ('" + userID + "')";
                statement.executeUpdate(sqlInsert);
                System.out.println("UserID enregistré dans la base de données avec succès !");
            } else {
                // Si userID existe déjà, affiche un message d'erreur
                System.out.println("UserID existe déjà dans la base de données.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}