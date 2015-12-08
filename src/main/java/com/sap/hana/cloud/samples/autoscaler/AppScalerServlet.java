package com.sap.hana.cloud.samples.autoscaler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/backend")
public class AppScalerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ScaleCentral sc;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AppScalerServlet() {
        super();
    }

    public void init() throws ServletException {
    	sc = ScaleCentral.getInstance();
    	RuleEngine.loadRuleThresholds(getServletContext());
    }
    
	/**
	 * This is the GET entry point. It takes a parameter "action", whose value that defines what happens next:
	 * 1. "start" - the monitoring of the application starts.
	 * 2. "stop" - the monitoring of the application stops.
	 * 3. "query" - get info about the current application. This call needs to be accompanied by two more parameters,
	 * "accountName" and "applicationName". In fact, this should be the first call, that sets up the coordinates of the
	 * monitored application. If not restricted from the UI, then all the other calls should be made after this one!
	 * 4. "startApp" - start a new instance of the current application.
	 * 5. "stopApp" - stops an instance of the current application.
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		String actionParam = request.getParameter("action");
		
		if (actionParam.equals("start")) {
			try {
				sc.startMonitor();
				out.println("Started to monitor the application.<br>");
				out.println(sc.getParsedAppMetrics().toString());
			} catch (Exception e) {
				out.println("Failed to start monitoring the application. " + e.getStackTrace());
			}
		} else if (actionParam.equals("stop")) {
			try {
				sc.stopMonitor();
				out.println("Stopped monitoring the application.");
			} catch (Exception e) {
				out.println("Failed to stop monitoring the application. " + e.getStackTrace());
			}
		} else if (actionParam.equals("query")) {
			  String accountName = request.getParameter("accountName");
			  String applicationName = request.getParameter("applicationName");
			  sc.updateApp2Scale(accountName, applicationName);
		      out.println(sc.getUpdatedAppInfo());
		} else if (actionParam.equals("startApp")) {
			out.println(sc.startApplication());
			out.println("Min Processes: " + sc.appData.getAppInfo().getMinProcesses());
			out.println("Max Processes: " + sc.appData.getAppInfo().getMaxProcesses());
		} else if (actionParam.equals("stopApp")) {
			out.println(sc.stopApplicationProcess());
			out.println("Min Processes: " + sc.appData.getAppInfo().getMinProcesses());
			out.println("Max Processes: " + sc.appData.getAppInfo().getMaxProcesses());
		}
	}

}
