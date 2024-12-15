DROP DATABASE IF EXISTS `papachat`;
CREATE DATABASE IF NOT EXISTS `papachat`;
USE `papachat`;

 CREATE TABLE `users` (
  `userID` int NOT NULL AUTO_INCREMENT,
  `username` varchar(60) NOT NULL unique,
  `password` varchar(60) NOT NULL,
  PRIMARY KEY (`userID`),
  UNIQUE KEY `userID_UNIQUE` (`userID`)
);

CREATE TABLE `classes` (
  `classID` int NOT NULL AUTO_INCREMENT,
  `classCode` varchar(60) NOT NULL,
  `classDuration` int NOT NULL, # Measured in minutes
  `classStartTime` int NOT NULL, # class start time at 2:30 pm is 1430
  `classDays` varchar(60) NOT NULL, # "M, T, W, Th, F, S, Su"
  PRIMARY KEY (`classID`),
  UNIQUE KEY `classID_UNIQUE` (`classID`)
);

CREATE TABLE `class_standing` (
  `classID` int NOT NULL,
  `userID` int NOT NULL,
  `points` int default 0,
  `access` VARCHAR(60) default "user", # user, moderator, admin
  PRIMARY KEY (`classID`, `userID`),
  FOREIGN KEY (`classID`) REFERENCES classes(classID),
  FOREIGN KEY (`userID`) REFERENCES users(userID)
);

CREATE TABLE `chat_history` (
  `chatHistoryID` int NOT NULL AUTO_INCREMENT,
  `classID` int NOT NULL,
  `userID` int NOT NULL,
  `datetime` datetime NOT NULL,
  `message` VARCHAR(280),
  PRIMARY KEY (`chatHistoryID`),
  FOREIGN KEY (`classID`) REFERENCES classes(classID),
  FOREIGN KEY (`userID`) REFERENCES users(userID)
);

CREATE TABLE `ban_history` (
	`banHistoryID` int NOT NULL AUTO_INCREMENT,
    `userID` int NOT NULL, # userID of the user banned
    `classID` int NOT NULL,
    `timeOfBan` datetime NOT NULL,
    `duration` int NOT NULL, # duration in minutes, -1 if indefinite,
    PRIMARY KEY(`banHistoryID`),
    FOREIGN KEY (`userID`) REFERENCES users(userID),
    FOREIGN KEY (`classID`) REFERENCES classes(classID)
);

CREATE TABLE `emotes` (
  `emoteName` VARCHAR(60) NOT NULL,
  `emoteBin` LONGBLOB NOT NULL,
  PRIMARY KEY (`emoteName`)
) ;

CREATE TABLE `banned_words` (
	`wordID` int NOT NULL AUTO_INCREMENT,
	`word` VARCHAR(60) NOT NULL,
    PRIMARY KEY (`wordID`)
);

INSERT INTO users (username, password) VALUES ("admin", "admin");

-- INSERT INTO classes (classCode, classDuration, classStartTime, classDays) VALUES ("all", 120, 1100, "T, Th");

-- OLD - TO DELETE
-- CREATE TABLE `chat_history` (
--   `chatHistoryID` int NOT NULL AUTO_INCREMENT,
--   `classID` int NOT NULL,
--   `date` datetime NOT NULL,
--   `chatHistoryFileName` VARCHAR(100),
--   PRIMARY KEY (`chatHistoryID`),
--   FOREIGN KEY (`classID`) REFERENCES classes(classID)
-- ) ;