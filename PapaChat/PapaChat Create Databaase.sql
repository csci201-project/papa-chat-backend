CREATE DATABASE IF NOT EXISTS `PapaChat`;
USE `PapaChat`;

 CREATE TABLE `users` (
  `userID` int NOT NULL AUTO_INCREMENT,
  `fname` varchar(60) NOT NULL,
  `lname` varchar(60) NOT NULL,
  `email` varchar(60) NOT NULL unique,
  `username` varchar(60) NOT NULL unique,
  `password` varchar(60) NOT NULL,
  PRIMARY KEY (`userID`),
  UNIQUE KEY `userID_UNIQUE` (`userID`),
) ;

CREATE TABLE `users` (
  `userID` int NOT NULL AUTO_INCREMENT,
  `username` varchar(60) NOT NULL,
  `password` varchar(60) NOT NULL,
  PRIMARY KEY (`userID`),
  UNIQUE KEY `userId_UNIQUE` (`userID`),
  UNIQUE KEY `username_UNIQUE` (`username`)
) ;

Error Code: 1064. You have an error in your SQL syntax; 
check the manual that corresponds to your MySQL server version for the right syntax to use near ')' at line 10
