Projet java forum:

Front-end: Alexis / Noah

Back-end: Léo / Alexandre

------------------------------------

objectif:

-faire une interface graphique avec seulement java: V

-faire un forum en full java: V

-système login/mot de passe: V

------------------------------------

Lien qui nous ont aidés:

https://www.guru99.com/java-swing-gui.html (affichage de l'application)

https://www.youtube.com/watch?v=Hiv3gwJC5kw (système login/password)

------------------------------------

Installation MySQL (Alexandre)

sudo apt-get install mysql-server

sudo mysql_secure_installation

    1. setup VALIDATE PASSWORD plugin.  
       select no.
    2. Set the password for the root MySql account.
    3. Remove anonymous users?  
       select yes.
    4. Disallow root login remotely?
       select no.
    5. Remove test database and access to it? 
       chose yes.
    6. Reload privilege tables now? 
       chose yes.
sudo mysql -u root -p

ALTER USER 'root'@'localhost' IDENTIFIED WITH 'mysql_native_password' BY 'password';
FLUSH PRIVILEGES;
exit


------------------------------------

JBDC driver

https://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-j_8.3.0-1ubuntu22.04_all.deb

cd Downloads
sudo apt install ./mysql-connector-j_8.3.0-1ubuntu22.04_all.deb
