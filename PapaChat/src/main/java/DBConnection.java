import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DBConnection {
	private Connection conn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;

	public DBConnection() {
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/WeatherConditions?user=root&password=sql@csc1&useSSL-false");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generic query function that expects a rs, ie: select but not insert
	 *  
	 * @param query	custom SELECT query, eg: <i>"SELECT * FROM users;"</i>
	 * @return a 2D array of the query result
	 */
	public ArrayList<ArrayList<String>> query(String query) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while(rs.next()) {
				ArrayList<String> row = new ArrayList<String>();
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
	 * Checks if the username already exits
	 *  
	 * @param username			user's username, eg: <i>"root"</i>
	 * @param password			user's password, eg: <i>"password"</i>
	 * @return 0 if user does not already exists and is successfully registered
	 * 		   1 if user already exists
	 *		   2 if there was an error in registration
	 */
	public int registerUser(String username, String password) {
		try {
			ps = conn.prepareStatement("SELECT COUNT(username) FROM users WHERE username=?");
			ps.setString(1, username);
			rs = ps.executeQuery();
			if(rs.next() && rs.getInt(1) > 0) {
				return 1;
			}
			ps = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
			ps.setString(1, username);
			ps.setString(2, password);
			int success = ps.executeUpdate();
			if(success > 0) return 0;
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
	
	public HashMap<String, String> getUserInfo(){
		HashMap<String, String> userInfo = new HashMap<String, String>();
		
		
		return userInfo;
	}
	
	/**
	 * Registers a class if one does not already exist
	 *  
	 * @param className			The name of the class, eg: <i>"Software Development"</i>
	 * @param classCode			The code of the class, eg: <i>"CSCI201"</i>
	 * @param classDuration		The duration of the class in minutes, eg: <i>80</i> to indicate 1hr and 20 mins
	 * @param classStartTime	The start time of the class using the 24-hour clock, eg: <i>1430</i> for 2:30pm
	 * @param classDays			The days of the class, eg: <i>"M, T, W, Th, F, S, Su"</i> for all the days of the week
	 * @return 0 if class does not already exists and is successfully registered<br>
	 * 		   1 if class already exists<br>
	 * 		   2 if there was an error in registration
	 */
	public int registerClass(String className, String classCode, int classDuration, int classStartTime, String classDays) {
		try {
			// Check if the class already exists
			ps = conn.prepareStatement("SELECT COUNT(classCode) FROM classes WHERE classCode=?");
			ps.setString(1, className);
			rs = ps.executeQuery();
			if(rs.next() && rs.getInt(1) > 0) {
				return 1;
			}
			// Register the class
			ps = conn.prepareStatement("INSERT INTO classes (className, classCode, classDuration, classStartTime, classDays) VALUES (?, ?, ? ,?, ?)");
			ps.setString(1, className);
			ps.setString(2, classCode);
			ps.setInt(1, classDuration);
			ps.setInt(2, classStartTime);
			ps.setString(2, classDays);
			int success = ps.executeUpdate();
			if(success > 0) return 0;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 2; 
	}
	
	
	/**
	 * Saves the chat history of a class. This function handles the time stamp automatically.
	 *  
	 * @param classCode				The code of the class, eg: <i>"Software Development"</i>
	 * @param chathistoryFileName	The name of the file where the chatHistory was saved to eg: <i> "CSCI20120241205.csv"</i>
	 * @return 0 if chat history was saved successfully<br>
	 * 		   1 if the className does not exist<br>
	 * 		   2 if there was an error in saving
	 */
	public int saveSearch(String classCode, String chatHistoryFileName) {
		try {
			// Check if className exists
			ps = conn.prepareStatement("SELECT classID FROM classes WHERE className=?");
			ps.setString(1, classCode);
			rs = ps.executeQuery();
			if (rs.next()) {
				// Extract className's start time
                int classID = rs.getInt("classID");
                ps = conn.prepareStatement("SELECT classStartTime FROM classes WHERE classID=?");
                ps.setInt(1, classID);
                rs = ps.executeQuery();
                int startTime = rs.getInt("classStartTime");
                // Save chat history
                LocalDateTime dateStamp = LocalDateTime.now().withHour(startTime/100).withMinute(startTime%100).withSecond(0).withNano(0);
                ps = conn.prepareStatement("INSERT INTO chat_history (classID, date, chatHistoryFileName) VALUES (?, ?, ?)");
    			ps.setInt(1, classID);
    			ps.setTimestamp(2, Timestamp.valueOf(dateStamp));
    			ps.setString(3, chatHistoryFileName);
    			int success = ps.executeUpdate();
    			if(success > 0) return 0;
            } else {
                return 1;
            }
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 2;
	}
	
	/**
	 * Saves the chat history of a class. This function handles the time stamp automatically.
	 *  
	 * @param classCode				The code of the class, eg: <i>"Software Development"</i>
	 * @return List<string> of file names of this class code's chat history starting from the most recent<br>
	 * 		   null if className does not exist or other errors
	 */
	public List<String> getChatHistory(String classCode) {
		List<String> history = new ArrayList<String>();
		try {
			// Check if className exists
			ps = conn.prepareStatement("SELECT classID FROM classes WHERE className=?");
			ps.setString(1, classCode);
			rs = ps.executeQuery();
			if (rs.next()) {
				// Extract chatHistories start time
				ps = conn.prepareStatement("SELECT chatHistoryFileName FROM chat_history WHERE classCode = (SELECT classID FROM classes WHERE classCode=?) ORDER BY date DESC");
				ps.setString(1, classCode);
				rs = ps.executeQuery();
				while(rs.next()) {
					history.add(rs.getString("chatHistoryFileName"));
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

	/**
	 * Correctly closes all connections to the DB with this instance.
	 */
	public void closeDBConnection() {
		try {
			if(rs != null) rs.close();
			if(ps != null) ps.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
