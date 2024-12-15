package com.papa.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// users: userID [int], fname [str], lname [str], email [str], username [str], password [str]
// classes: classID [int], className [str], classCode [str],
//			classDuration [int] (measured in minutes), classStartTime [int] (24hr, ie: 2:30pm is 1430), classDays [str] (everyday= "M, T, W, Th, F, S, Su")
// class_standing: classID [int], userID [int], points [int], access [str]
// chat_history: chatHistoryID [int], classID [int], userID [int], datetime [datetime], message [str] (280 char max)
// ban_history: banHistoryID [int], userID [int], timeOfBan [datetime], duration [int] (int in minutes)
// emotes: emoteID [int], emoteName [str], emoteFileName [str]
// class_emotes: classID [int], emoteID [int]

// TODO
// change user info - add to registerUser function
// get a class's list of emotes, emote library,

public class DBConnection {
	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;
	private String DBusername = "root"; // CHANGE
	private String DBpassword = "sql@csc1"; // CHANGE

	// ========================================== Database ==========================================

	/*
	 * Instantiate a connection with DB. Change DBusername and DBpassword to server's SQL databse
	 */
	public DBConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/papachat?useSSL=false",
				DBusername,
				DBpassword
			);
		} catch (ClassNotFoundException | SQLException e) {
			System.err.println("Database connection failed: " + e.getMessage());
			e.printStackTrace();
		}

		if (conn == null) {
			throw new RuntimeException("Failed to establish database connection");
		}
	}
	/**
	 * Generic query function that expects a rs, ie: select but not insert
	 *
	 * @param query	custom SELECT query, eg: <i>"SELECT * FROM users;"</i>
	 * @return a 2D array of the query result
	 */
	public ArrayList<ArrayList<String>> query(String query) {
		ArrayList<ArrayList<String>> result = new ArrayList<>();

		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while(rs.next()) {
				ArrayList<String> row = new ArrayList<>();
				for(int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
					row.add(rs.getString(i+1));
				}
				result.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * Correctly closes all connections to the DB with this instance.
	 */
	public void closeDBConnection() {
		try {
			if(rs != null) {
				rs.close();
			}
			if(ps != null) {
				ps.close();
			}
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// =========================================== Users ============================================

	/**
	 * Registers a user into the DB if they do not exist. If user exists, update their info.
	 *
	 * @param username			user's username, eg: <i>"root"</i>
	 * @param password			user's password, eg: <i>"password"</i>
	 * @return 0 if user does not already exists and is successfully registered<br>
	 * 		   1 if user info was updated successfully<br>
	 *		   2 if there was an error in registration
	 */
	public int registerUser(String username, String password) {
		try {
			int userID = getUserID(username);
			if(userID == -1) {
				// User does not exist - register
				ps = conn.prepareStatement("INSERT INTO users (username, password) VALUES (? ,?)");
				ps.setString(1, username);
				ps.setString(2, password);
				int success = ps.executeUpdate();
				if(success > 0) {
					return 0;
				}
			} else {
				// User exist - return 1
				return 1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 2;
	}
	/**
	 * Checks if the username exists if password is null, checks if password is valid is password is not null
	 *
	 * @param username			user's username, eg: <i>"root"</i>
	 * @param password			user's password, eg: <i>"password"</i>
	 * @return true if username and password are correct<br>
	 *		   false if username does not exits and password is null<br>
	 *		   false if password is incorrect and not null
	 */
	public boolean authenticateUser(String username, String password) {
		boolean valid = false;
		if(password == "" || password == null ) {
			try {
				ps = conn.prepareStatement("SELECT * FROM users WHERE username=?");
				ps.setString(1, username);
				rs = ps.executeQuery();
				valid = rs.next();
				return valid;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			try {
				ps = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password=?");
				ps.setString(1, username);
				ps.setString(2, password);
				rs = ps.executeQuery();
				valid = rs.next();
				return valid;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return valid;
	}
	/**
	 * OUTDATED DO NOT USE Retrieve user information with keys: fname, lname, email
	 *
	 * @param username			user's username, eg: <i>"andrecro"</i>
	 * @return HashMap<String, String> of a user's information with keys mentioned above<br>
	 *		   null if username does not exits
	 */
	public HashMap<String, String> getUserInfo(String username){
		HashMap<String, String> userInfo = new HashMap<>();
		try {
			ps = conn.prepareStatement("SELECT * FROM users WHERE username=?");
			ps.setString(1, username);
			rs = ps.executeQuery();
			if(rs.next()) {
				userInfo.put("fname", rs.getString("fname"));
				userInfo.put("lname", rs.getString("lname"));
				userInfo.put("email", rs.getString("email"));
				return userInfo;
			}
			else {
				return null;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	// ======================================== Ban History =========================================

	/**
	 * Checks if a user is banned in a class and returns the remaining minutes. If integer returned is negative, the user is no longer banned.
	 *
	 * @param username			The username of the user, eg: <i>"andrecro"</i>
	 * @param classCode			The code of the class, eg: <i>"CSCI201"</i>
	 * @param duration 			The duration in minutes of the ban, eg: <i>120</i> (for 2 hrs)
	 * @return true if ban was successful <br>
	 * 		   false if username does not exist
	 */
	public boolean banUser(String username, String classCode, int duration) {
		int userID = getUserID(username);
		if(userID == -1) {
			return false;
		}
		try {
			ps = conn.prepareStatement("INSERT INTO ban_history (userID, timeOfBan, duration) VALUES (?, NOW(), ?)");
			ps.setInt(1, userID);
			ps.setInt(2, duration);
			int success = ps.executeUpdate();
			return success > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * Checks if a user is banned in a class and returns the remaining minutes. If integer returned is negative, the user is no longer banned.
	 *
	 * @param username			The username of the user, eg: <i>"andrecro"</i>
	 * @param classCode			The code of the class, eg: <i>"CSCI201"</i>
	 * @return integer minutes remaining if user is banned<br>
	 * 		   -1 if username or classCode does not exists
	 */
	public int isUserBanned(String username, String classCode) {
		try {
			// Get userID and classID if username and classCode exists, otherwise return -1
			int userID = getUserID(username);
			if(userID == -1) {
				return -1;
			}
			int classID = getClassID(classCode);
			if(classID == -1) {
				return -1;
			}

			// Check if username is enrolled in a class
			ps = conn.prepareStatement("SELECT timeOfBan, duration FROM ban_history WHERE userID=? AND classID=? ORDER BY timeOfBan DESC");
			ps.setInt(1, userID);
			ps.setInt(2, classID);
			rs = ps.executeQuery();
			if(rs.next()) {
                Timestamp timeOfBan = rs.getTimestamp("timeOfBan");
                int duration = rs.getInt("duration");
                LocalDateTime banStartTime = timeOfBan.toLocalDateTime();
                LocalDateTime currentTime = LocalDateTime.now();
                LocalDateTime banEndTime = banStartTime.plusMinutes(duration);
                Duration remainingDuration = Duration.between(currentTime, banEndTime);
                return(int) remainingDuration.toMinutes();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	// ========================================== Classes ===========================================
	/**
	 * Registers a class if one does not already exist
	 *
	 * @param classCode			The code of the class, eg: <i>"CSCI201"</i>
	 * @param classDuration		The duration of the class in minutes, eg: <i>80</i> to indicate 1hr and 20 mins
	 * @param classStartTime	The start time of the class using the 24-hour clock, eg: <i>1430</i> for 2:30pm
	 * @param classDays			The days of the class, eg: <i>"M, T, W, Th, F, S, Su"</i> for all the days of the week
	 * @return 0 if class does not already exists and is successfully registered<br>
	 * 		   1 if class already exists<br>
	 * 		   2 if there was an error in registration
	 */
	public int registerClass(String classCode, int classDuration, int classStartTime, String classDays) {
		try {
			// Check if the class already exists
			int classID = getClassID(classCode);
			if(classID == -1) {
				return 1;
			}
			// Register the class
			ps = conn.prepareStatement("INSERT INTO classes (classCode, classDuration, classStartTime, classDays) VALUES (?, ? ,?, ?)");
			ps.setString(1, classCode);
			ps.setInt(2, classDuration);
			ps.setInt(3, classStartTime);
			ps.setString(4, classDays);
			int success = ps.executeUpdate();
			if(success > 0) {
				return 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 2;
	}

	/**
	 * Retrieve user information with keys: className, classDuration, classStartTime, classDays
	 *
	 * @param classCode			class's classCode, eg: <i>"CSCI201"</i>
	 * @return HashMap<String, String> of a user's information with keys mentioned above<br>
	 *		   null if username does not exits
	 */
	public HashMap<String, String> getClassInfo(String classCode){
		HashMap<String, String> classInfo = new HashMap<>();
		try {
			ps = conn.prepareStatement("SELECT * FROM classes WHERE classCode=?");
			ps.setString(1, classCode);
			rs = ps.executeQuery();
			if(rs.next()) {
				classInfo.put("classDuration", rs.getString("classDuration"));
				classInfo.put("classStartTime", rs.getString("classStartTime"));
				classInfo.put("classDays", rs.getString("classDays"));
				return classInfo;
			}
			else {
				return null;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// ====================================== Class Standing ========================================

	/**
	 * Enrolls a user in a class. If the user is already enrolled in that class, only their access will be updated.
	 *
	 * @param username			user's username, eg: <i>"root"</i>
	 * @param classCode			class' code, eg: <i>"CSCI201"</i>
	 * @param accessLevel 		User's access for this specific class, eg: <i>"user", "moderator" or "admin"</i>
	 * @return true if user is successfully enrolled or updated<br>
	 * 		   false if there was an error in enrollment/update
	 */
	public boolean enrollUser(String username, String classCode, String accessLevel) {
		int points = 0;
		try {
			// Get userID if username exists, otherwise return false
			int userID = getUserID(username);
			if(userID == -1) {
				return false;
			}
			// Get classID if classCode exists, otherwise return false
			int classID = getClassID(classCode);
			if(classID == -1) {
				return false;
			}
			// Check if username is enrolled in a class
			ps = conn.prepareStatement("SELECT userID FROM class_standing WHERE userID=? AND classID=?");
			ps.setInt(1, userID);
			ps.setInt(2, classID);
			rs = ps.executeQuery();
			if(!rs.next()) {
				// Username is not enrolled in that class - enroll user
				ps = conn.prepareStatement("INSERT INTO class_standing (classID, userID, points, access) VALUES (?, ?, ?, ?)");
				ps.setInt(1, classID);
				ps.setInt(2, userID);
				ps.setInt(3, points);
				ps.setString(4, accessLevel);
				int success = ps.executeUpdate();
				return success > 0;
			}
			else {
				// Username is enrolled in that class already - update access
				ps = conn.prepareStatement("UPDATE class_standing SET access=? WHERE classID=? AND userID=?");
				ps.setString(1, accessLevel);
				ps.setInt(2, classID);
				ps.setInt(3, userID);
				int success = ps.executeUpdate();
				return success > 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	/**
	 * If the user is enrolled in that class add to their points. (insert a negative integer to deduct).
	 *
	 * @param username			user's username, eg: <i>"root"</i>
	 * @param classCode			class' code, eg: <i>"CSCI201"</i>
	 * @param points 		User's access for this specific class, eg: <i>"user", "moderator" or "admin"</i>
	 * @return true if user is successfully enrolled or updated<br>
	 * 		   false if there was an error in enrollment/update
	 */
	public boolean updateUserPoints(String username, String classCode, int points) {
		try {
			// Get userID if username exists, otherwise return false
			ps = conn.prepareStatement("SELECT userID FROM users WHERE username=?");
			ps.setString(1, username);
			rs = ps.executeQuery();
			if(!rs.next()) {
				return false;
			}
			int userID = rs.getInt("userID");
			// Get classID if classCode exists, otherwise return false
			ps = conn.prepareStatement("SELECT classID FROM classes WHERE classCode=?");
			ps.setString(1, classCode);
			rs = ps.executeQuery();
			if(!rs.next()) {
				return false;
			}
			int classID = rs.getInt("classID");
			// Check if username is enrolled in a class
			ps = conn.prepareStatement("SELECT points FROM class_standing WHERE userID=? AND classID=?");
			ps.setInt(1, userID);
			ps.setInt(2, classID);
			rs = ps.executeQuery();
			if(rs.next()) {
				// Username is enrolled in that class already - update points
				int oldPoints = rs.getInt("points");
				ps = conn.prepareStatement("UPDATE class_standing SET points=? WHERE classID=? AND userID=?");
				ps.setInt(1, oldPoints+points);
				ps.setInt(2, classID);
				ps.setInt(3, userID);
				int success = ps.executeUpdate();
				return success > 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}


	// ======================================= Chat History =========================================

	/**
	 * Saves one chat message from a user for a class. Time stamp is when this function is called to insert the message into the database.
	 *
	 * @param classCode				The code of the class, eg: <i>"CSCI201"</i>
	 * @param username				The username of the user, eg: <i>"andrecro"</i>
	 * @param message				The message from the user for the class, eg: <i>"stunlocked"</i>
	 * @return 0 if chat history was saved successfully<br>
	 * 		   1 if the className does not exist<br>
	 * 		   2 if the username does not exist<br>
	 * 		   3 if the message is longer than 280 char
	 * 		   4 if there was an error in saving
	 */
	public int saveChat(String classCode, String username, String message) {
		if(message.length() > 280) {
			return 3;
		}
		try {
			// Check if className exists
			ps = conn.prepareStatement("SELECT classID FROM classes WHERE className=?");
			ps.setString(1, classCode);
			rs = ps.executeQuery();
			if (rs.next()) {
				// Check if username exists
                int classID = rs.getInt("classID");
                ps = conn.prepareStatement("SELECT userID FROM users WHERE username=?");
                ps.setString(1, username);
                rs = ps.executeQuery();
                if(rs.next()) {
                	// Save chat history
                	int userID = rs.getInt("userID");
                	ps = conn.prepareStatement("INSERT INTO chat_history (classID, userID, date, message) VALUES (?, ?, NOW(), ?)");
        			ps.setInt(1, classID);
        			ps.setInt(2, userID);
        			ps.setString(4, message);
        			int success = ps.executeUpdate();
        			if(success > 0) {
						return 0;
					}
                } else {
					return 2;
				}
            } else {
				return 1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 4;
	}
	/**
	 * Retrieve the chat history of a user sorted by most recent. <br>
	 * Each chat line is formatted as one string "classCode timeStamp (YYYY-MM-DD HH:MM:SS) message", <br>
	 * eg: <i>"CSCI 201 2024-12-10 14:23:45 Who else is freezing!?</i>
	 *
	 * @param username	The username of the user, eg: <i>"andrecro"</i>
	 * @return List<string> of formatted chat messages with classCode and timeStamp<br>
	 * 		   null if username does not exist or other errors
	 */
	public List<String> getChatHistory(String username) {
		List<String> history = new ArrayList<>();
		try {
			// Check if username exists
			ps = conn.prepareStatement("SELECT userID FROM classes WHERE username=?");
			ps.setString(1, username);
			rs = ps.executeQuery();
			if (rs.next()) {
				int userID = rs.getInt("userID");
				ps = conn.prepareStatement("SELECT classCode, datetime, message FROM chat_history WHERE userID=? ORDER BY date DESC");
				ps.setInt(1, userID);
				rs = ps.executeQuery();
				while(rs.next()) {
					history.add(rs.getString(rs.getString("classCode")+ " " + rs.getTimestamp("datetime") + " " + rs.getString("message")));
				}
				return history;
            } else {
                return null;
            }
		} catch (SQLException e){
			e.printStackTrace();
		}
		return null;
	}

	// ======================================= Banned Words =========================================
	
	public String censorBannedWords(String message) {
        String censoredString = message;
        try {
        	ps = conn.prepareStatement("SELECT word FROM banned_words");
            rs = ps.executeQuery();

            while (rs.next()) {
            	// Make banned word a pattern and find a match if exists
                String bannedWord = rs.getString("word");
                Pattern pattern = Pattern.compile(bannedWord, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(censoredString);

                // Censor with Astrix
                if (matcher.find()) {
                    String replacement = "*".repeat(bannedWord.length());
                    censoredString = matcher.replaceAll(replacement);
                }
            }
        } catch (SQLException e) {
			e.printStackTrace();
		}
        return censoredString;
	}
	
	public boolean addBannedWord(String word) {
		try {
			// Check if the word already exists
			ps = conn.prepareStatement("SELECT word FROM banned_words WHERE word=?");
			ps.setString(1, word);
			rs = ps.executeQuery();
			if(rs.next()) {
				return false;
			}
			// Register the class
			ps = conn.prepareStatement("INSERT INTO banned_words (word) VALUES (?)");
			ps.setString(1, word);
			int success = ps.executeUpdate();
			return success > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false; 
	}
	// =========================================== Emotes ===========================================
	
	/**
	 * Saves an emote to the database. If an emote with the same name exists, it will be updated.
	 * @param emoteName The name of the emote (unique identifier)
	 * @param emoteData The binary data of the processed image
	 * @return 0 if new emote was saved successfully
	 *         1 if existing emote was updated successfully
	 *         2 if there was an error
	 */
	public int saveEmote(String emoteName, byte[] emoteData) {
		try {
			// Check if emote exists
			ps = conn.prepareStatement("SELECT emoteName FROM emotes WHERE emoteName = ?");
			ps.setString(1, emoteName);
			rs = ps.executeQuery();
			
			if (rs.next()) {
				// Update existing emote
				ps = conn.prepareStatement("UPDATE emotes SET emoteBin = ? WHERE emoteName = ?");
				ps.setBytes(1, emoteData);
				ps.setString(2, emoteName);
				return ps.executeUpdate() > 0 ? 1 : 2;
			} else {
				// Insert new emote
				ps = conn.prepareStatement("INSERT INTO emotes (emoteName, emoteBin) VALUES (?, ?)");
				ps.setString(1, emoteName);
				ps.setBytes(2, emoteData);
				return ps.executeUpdate() > 0 ? 0 : 2;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return 2;
		}
	}

	/**
	 * Retrieves an emote by name
	 * @param emoteName The name of the emote to retrieve
	 * @return byte array of the emote data, or null if not found
	 */
	public byte[] getEmote(String emoteName) {
		try {
			ps = conn.prepareStatement("SELECT emoteBin FROM emotes WHERE emoteName = ?");
			ps.setString(1, emoteName);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getBytes("emoteBin");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets all emote names
	 * @return List of emote names
	 */
	public List<String> getAllEmoteNames() {
		List<String> emoteNames = new ArrayList<>();
		try {
			ps = conn.prepareStatement("SELECT emoteName FROM emotes ORDER BY emoteName");
			rs = ps.executeQuery();
			while (rs.next()) {
				emoteNames.add(rs.getString("emoteName"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return emoteNames;
	}

	/**
	 * Deletes an emote by name
	 * @param emoteName The name of the emote to delete
	 * @return true if deleted successfully, false otherwise
	 */
	public boolean deleteEmote(String emoteName) {
		try {
			ps = conn.prepareStatement("DELETE FROM emotes WHERE emoteName = ?");
			ps.setString(1, emoteName);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	// ======================================= DB Utilities =========================================
	public int getUserID(String username) {
		try {
			// Get userID if username exists, otherwise return -1
			ps = conn.prepareStatement("SELECT userID FROM users WHERE username=?");
			ps.setString(1, username);
			rs = ps.executeQuery();
			if(!rs.next()) {
				return -1;
			}
			return rs.getInt("userID");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public int getClassID(String classCode) {
		try {
			// Get classID if classCode exists, otherwise return -1
			ps = conn.prepareStatement("SELECT classID FROM classes WHERE classCode=?");
			ps.setString(1, classCode);
			rs = ps.executeQuery();
			if(!rs.next()) {
				return -1;
			}
			return rs.getInt("classID");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

}
