CREATE USER IF NOT EXISTS 'myuser'@'%' IDENTIFIED BY 'mypass';
GRANT ALL PRIVILEGES ON * . * TO 'myuser'@'%';
CREATE DATABASE IF NOT EXISTS password_manager;