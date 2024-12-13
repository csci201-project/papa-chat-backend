import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/loginVerification")
public class LoginServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private DBConnection DBConnection;
	private Gson gson;

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		DBConnection = new DBConnection();
		gson = new Gson();
	
		
		User user = gson.fromJson(request.getReader(), User.class);
		String username = user.getUsername();
		String password = user.getPassword();
		
		JsonObject preparedResponse = new JsonObject();
		if(!DBConnection.authenticateUser(username, null)){
			System.out.println("User " + username + " does not exist");
			preparedResponse.addProperty("verified", false);
			preparedResponse.addProperty("message", "User does not exist.");
		} else if(!DBConnection.authenticateUser(username, password)) {
			System.out.println("User: " + username + " exists, but password: " + password + " is incorrect");
			preparedResponse.addProperty("verified", false);
			preparedResponse.addProperty("message", "Password is incorrect.");
		} else {
			System.out.println("User " + username + ", with password: " + password + " Found");
			preparedResponse.addProperty("verified", true);
			preparedResponse.addProperty("message", "Login successful.");
		}
		
		DBConnection.closeDBConnection();
		try (var writer = response.getWriter()) {
            gson.toJson(preparedResponse, writer);
        }
		
		
	}

}
