DROP DATABASE IF EXISTS `PapaChat`;
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
  UNIQUE KEY `userID_UNIQUE` (`userID`)
) ;

CREATE TABLE `classes` (
  `classID` int NOT NULL AUTO_INCREMENT,
  `className` varchar(60) NOT NULL,
  `classCode` varchar(60) NOT NULL,
  `classDuration` int NOT NULL, # Measured in minutes
  `classStartTime` int NOT NULL, # class start time at 2:30 pm is 1430
  `classDays` varchar(60) NOT NULL, # "M, T, W, Th, F, S, Su"
  PRIMARY KEY (`classID`),
  UNIQUE KEY `classID_UNIQUE` (`classID`)
) ;

CREATE TABLE `class_standing` (
  `classID` int NOT NULL,
  `userID` int NOT NULL,
  `points` int default 0,
  `access` VARCHAR(60) default "user", # user, moderator, admin
  PRIMARY KEY (`classID`, `userID`),
  FOREIGN KEY (`classID`) REFERENCES classes(classID),
  FOREIGN KEY (`userID`) REFERENCES users(userID)
) ;

CREATE TABLE `chat_history` (
  `chatHistoryID` int NOT NULL AUTO_INCREMENT,
  `classID` int NOT NULL,
  `userID` int NOT NULL,
  `datetime` datetime NOT NULL,
  `message` VARCHAR(280),
  PRIMARY KEY (`chatHistoryID`),
  FOREIGN KEY (`classID`) REFERENCES classes(classID),
  FOREIGN KEY (`userID`) REFERENCES users(userID)
) ;

CREATE TABLE `ban_history` (
	`banHistoryID` int NOT NULL AUTO_INCREMENT,
    `userID` int NOT NULL, # userID of the user banned
    `timeOfBan` datetime NOT NULL,
    `duration` int NOT NULL, # duration in minutes, -1 if indefinite,
    PRIMARY KEY(`banHistoryID`),
    FOREIGN KEY (`userID`) REFERENCES users(userID)
);

CREATE TABLE `emotes` (
  `emoteID` int NOT NULL AUTO_INCREMENT,
  `emoteName` VARCHAR(60) NOT NULL,
  `emoteFileName` VARCHAR(100) NOT NULL,
  PRIMARY KEY (`emoteID`)
) ;

CREATE TABLE `class_emotes` (
  `classID` int NOT NULL,
  `emoteID` int NOT NULL,
  PRIMARY KEY (`emoteID`, `classID`),
  FOREIGN KEY (`classID`) REFERENCES classes(classID),
  FOREIGN KEY (`emoteID`) REFERENCES emotes(emoteID)
) ;

-- OLD - TO DELETE
-- CREATE TABLE `chat_history` (
--   `chatHistoryID` int NOT NULL AUTO_INCREMENT,
--   `classID` int NOT NULL,
--   `date` datetime NOT NULL,
--   `chatHistoryFileName` VARCHAR(100),
--   PRIMARY KEY (`chatHistoryID`),
--   FOREIGN KEY (`classID`) REFERENCES classes(classID)
-- ) ;