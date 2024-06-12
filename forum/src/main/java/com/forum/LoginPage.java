package com.forum;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Socket;


import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import javax.swing.JTextField;

public class LoginPage implements ActionListener {
    JFrame frame = new JFrame();
    JButton loginButton = new JButton("Login");
    JButton resetButton = new JButton("Reset");
    JTextField userIDField = new JTextField();


    JLabel userIDLabel = new JLabel("userID");
    
    JLabel messageLabel = new JLabel("");

    
    Socket clientSocket;
    
   
    LoginPage(Socket clientSocket) {
      
        
        this.clientSocket = clientSocket;

        userIDLabel.setBounds(50, 100, 75, 25);
       

        messageLabel.setBounds(125, 250, 250, 35);
        messageLabel.setFont(new Font(null, Font.ITALIC, 25));

        userIDField.setBounds(125, 100, 200, 25);
        

        loginButton.setBounds(125, 200, 100, 25);
        loginButton.setFocusable(false);
        loginButton.addActionListener(this);

        resetButton.setBounds(225, 200, 100, 25);
        resetButton.setFocusable(false);
        resetButton.addActionListener(this);

        frame.add(userIDLabel);

        frame.add(messageLabel);
        frame.add(userIDField);

        frame.add(loginButton);
        frame.add(resetButton);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 420);
        frame.setLayout(null);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
       
        if (e.getSource() == resetButton) {
            userIDField.setText("");
        }
        if (e.getSource() == loginButton) {
            String userID = userIDField.getText();
 
           
                
                    messageLabel.setForeground(Color.green);
                    messageLabel.setText("Login successful");
                    
                    CreateDB.saveUserID(userID);
                    new ForumScreen(userID);
                    frame.dispose();
                
                
            
        }
    }
}
