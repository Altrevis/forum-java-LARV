package com.forum;
import java.sql.*;

public class CreateDB {
    public static void main(String[] args) {
      
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