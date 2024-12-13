import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/registerVerification")
public class RegistrationServlet extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private DBConnection DBConnection;
	private Gson gson;

	// TODO check for email uniqueness
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		DBConnection = new DBConnection();
		gson = new Gson();
		
		User user = gson.fromJson(request.getReader(), User.class);

		String fname = user.getFname();
		String lname = user.getLname();
		String email = user.getEmail();
		String username = user.getUsername();
		String password = user.getPassword();
		
		JsonObject preparedResponse = new JsonObject();
		if(DBConnection.authenticateUser(username, null)){
			System.out.println("User " + username + " already exist");
			preparedResponse.addProperty("verified", false);
			preparedResponse.addProperty("message", "This username is already taken.");
		} else {
			int success = DBConnection.registerUser(fname, lname, email, username, password);
			if(success == 0) {
				System.out.println("User " + username + ", with password: " + password + " Registered");
				preparedResponse.addProperty("verified", true);
				preparedResponse.addProperty("message", "Registration successful.");
			} else {
				System.out.println("Registration Failed");
				preparedResponse.addProperty("verified", false);
				preparedResponse.addProperty("message", "Registration unsuccessful.");
			}
		}
		
		DBConnection.closeDBConnection();
		try (var writer = response.getWriter()) {
            gson.toJson(preparedResponse, writer);
        }
	}	
}
