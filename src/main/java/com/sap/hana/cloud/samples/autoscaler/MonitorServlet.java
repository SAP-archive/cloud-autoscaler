package com.sap.hana.cloud.samples.autoscaler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is a servlet that returns to the frontend the current monitored metrics 
 * (actually just the "cpu_utilization" and "busy_threads", but can be configured to return what's needed).
 */
@WebServlet("/monitor")
public class MonitorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ScaleCentral sc;
    private final String DEFAULT_METRIC = "cpu_utilization";   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MonitorServlet() {
        super();
    }
    
    public void init() throws ServletException {
    	sc = ScaleCentral.getInstance();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		out.println(sc.getAppInfo().getPids().size() + " running processes:<br>");
		Set<String> metrics = new HashSet<String>();
		metrics.add("cpu_utilization"); 
		metrics.add("busy_threads");
		out.println(prettyPrintCPUMetrics(metrics));
	}

	private String prettyPrintCPUMetrics(Set<String> metricNames) {
		StringBuilder sb = new StringBuilder();
		Map<String, Map<String, Integer>> pidMetrics = new HashMap<String, Map<String, Integer>>();
		for(String metricName : metricNames) {
			pidMetrics.put(metricName, MetricUtils.getMetricValues(sc.getParsedAppMetrics(), metricName));
//			Map<String, Integer> pidMetricValues = MetricUtils.getMetricValues(sc.getParsedAppMetrics(), metricName);
		}
		int counter = 1;
		for (String pid : sc.getAppInfo().getPids())/*pidMetricValues.keySet())*/ {
			sb.append(counter++ + pid + " : ");
			if (pidMetrics.get(DEFAULT_METRIC).get(pid) == null) /*pidMetricValues.get(pid)*/
				sb.append("STARTING<br>");
			else {
				for (String metricName : metricNames) {
					sb.append(metricName + " = " + pidMetrics.get(metricName).get(pid) + "; ");
				}
				sb.append("<br>");
				//sb.append(pidMetricValues.get(pid) + "%" + "<br>");
			}
		}
		return sb.toString();
	}

}
