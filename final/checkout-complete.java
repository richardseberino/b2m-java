package application.rsapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns="/checkout")

public class checkout extends HttpServlet {
    
	private static final long serialVersionUID = 1L;
	Logger logger = Logger.getLogger("rsapp.checkout");
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        int delay = (int)(Math.random() * 200); // simulated transaction delay 
        boolean errorState = (Math.random() * 100) > 5 ? true : false;
        String msg;

        try {
		    Thread.sleep((long)(delay));
        } catch (InterruptedException e) {
        }

        if ( errorState ) {
          msg = "RSAP0001I: Transaction OK.";
          logger.info(msg);
          response.setContentType("application/json");
		
          PrintWriter pw = response.getWriter();
          pw.print("{ \"status\": \"" + msg +"\", \"transactionTime\": \"" + delay + "ms\" }");
          pw.flush();

        } else {
          msg = "Severe problem detected.";
          msg = "RSAP0010E: Severe problem detected.";
          logger.severe(msg);
          response.sendError(500, msg);
        }
	}

}
