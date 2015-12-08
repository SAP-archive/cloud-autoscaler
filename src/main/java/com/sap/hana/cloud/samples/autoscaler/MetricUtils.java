package com.sap.hana.cloud.samples.autoscaler;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class MetricUtils {

	protected static String DEFAULT_METRICS = "default-metrics";
	protected static String USER_DEFINED_METRICS = "user-defined-metrics";
	
	/**
	 * This method returns the default metrics returned by the monitoring API 
	 * (this needs to be adapted for user-defined metrics)
	 * @param stringObj a stringified JSON
	 * @return JSONArray a JSON array of the metrics
	 */
	public static JSONArray getDefaultMetrics(String stringObj) {
		return (new JSONObject(stringObj.toString())).getJSONArray(DEFAULT_METRICS);
	}
	
	/**
	 * This method returns the user-defined metrics returned by the monitoring API 
	 * @param stringObj - a stringified JSON containing the metrics
	 * @return JSONArray - a JSON array of the metrics
	 */
	public static JSONArray getUserDefinedMetrics(String stringObj) {
		return (new JSONObject(stringObj.toString())).getJSONArray(USER_DEFINED_METRICS);
	}
	
	/**
	 * Method that returns a (integer) value of a specified metric 
	 * @param metricsArray - a JSON array of metrics
	 * @param metricName - a String containing the name of the metric whose value is sought
	 * @return int - the value of the metric
	 */
	public static int getMetricValue(JSONArray metricsArray, String metricName) {
		int i = 0;
		while (!metricsArray.isNull(i)) {
			JSONObject metric = metricsArray.getJSONObject(i++);
			if (metric.getString("name").equals(metricName)) {
				return Integer.parseInt(metric.getString("value"));
			}
		}
		return 0;
	}
	
	/**
	 * Method that returns a (integer) value of a specified metric 
	 * @param metricsObj - a stringified JSON containing the metrics
	 * @param metricName - a String containing the name of the metric whose value is sought
	 * @return int - the value of the metric
	 */
	public static int getMetricValue(String metricsObj, String metricName) {
		return getMetricValue(getAllMetrics(metricsObj), metricName);
	}

	/**
	 * Method that returns values
	 * @param parsedMetrics - a Map of Strings to a Map<String, Integer> containing the parsed metrics for each process
	 * @param metricName - the filter metric name whose values are sought
	 * @return Map<String, Integer> - the map whose keys are the processes and the values are the values of the metric
	 */
	public static Map<String, Integer> getMetricValues(Map<String, Map<String, Integer>> parsedMetrics, String metricName) {
		Map<String, Integer> returnMap = new HashMap<>();
		for (String pid : parsedMetrics.keySet()) {
			returnMap.put(pid, parsedMetrics.get(pid).get(metricName));
		}
		return returnMap;
	}
	
	/**
	 * Method that transforms a stringified JSON of metrics into a Map
	 * @param metricsObj - a stringified JSON containing the metrics
	 * @return Map<String, Integer> - the corresponding map transformed from the string 
	 */
	public static Map<String, Integer> parseMetrics(String metricsObj) {
		return parseMetrics(getAllMetrics(metricsObj));
	}

	/**
	 * Helper private function to concatenate and return an array containing both the custom and the default metrics
	 * retrieved from the monitoring API
	 * @param metricsObj - a stringified JSON
	 * @return JSONArray - a concatenated JSON array of the metrics
	 */
	private static JSONArray getAllMetrics(String metricsObj) {
		JSONArray allMetrics = getDefaultMetrics(metricsObj);
		JSONArray customMetrics = getUserDefinedMetrics(metricsObj);
		for (int i = 0; i < customMetrics.length(); i++) {
			allMetrics.put(customMetrics.get(i));
		}
		return allMetrics;
	}

	/**
	 * Method that transforms a JSONArray of metrics into a Map
	 * @param metricsArray - a JSONArray of metrics
	 * @return Map<String, Integer> - the corresponding map transformed from the string 
	 */
	private static Map<String, Integer> parseMetrics(JSONArray metricsArray) {
		Map<String, Integer> returnMap = new HashMap<>();
		int i = 0;
		while (!metricsArray.isNull(i)) {
			JSONObject metric = metricsArray.getJSONObject(i++);
			returnMap.put(metric.getString("name"), Integer.parseInt(metric.getString("value")));
		}
		return returnMap;
	}
}
