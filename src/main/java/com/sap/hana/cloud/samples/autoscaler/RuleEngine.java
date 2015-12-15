package com.sap.hana.cloud.samples.autoscaler;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

public class RuleEngine {
	public static Map<String, Integer> upscaleThresholds = new HashMap<>();
	public static Map<String, Integer> downscaleThresholds = new HashMap<>();
	public static String DEFAULT_METRIC = "CPU Load";
	public static String DEFAULT_METRIC_VALUE = "25,50";
	
	private static String DEFAULT_THRESHOLDS_FILE_LOCATION = "/WEB-INF/lib/params.properties";
	private static enum ruleNames { UPSCALE, DOWNSCALE };
	
	/**
	 * Helper private method that runs a rule and returns true or false, in case it is fulfilled or not, respectively.
	 * @param params a list of parameters that should be checked against the rule
	 * @param ruleName the name of the rule - can be one of {@link ruleNames} enums
	 * @return boolean true if the rule was fulfilled or false otherwise
	 */
	private static boolean runRule(Map<String, Integer> params, ruleNames ruleName) {
		if (ruleName == ruleNames.UPSCALE) {
			for (String paramName : upscaleThresholds.keySet()) {
				if (params.containsKey(paramName)) {
					if (params.get(paramName) == null || params.get(paramName) < upscaleThresholds.get(paramName)) {
						return false;
					}
				} else {
					return false;
				}
			}
			return true;
		} else if (ruleName == ruleNames.DOWNSCALE) {
			for (String paramName : downscaleThresholds.keySet()) {
				if (params.containsKey(paramName)) {
					if (params.get(paramName).equals(null) || params.get(paramName) > downscaleThresholds.get(paramName)) {
						return false;
					}
				} else {
					return false;
				}
				
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Public method that triggers the run of the upscale rules for all the monitored metrics
	 * @param pidMetrics a list of metrics for each instance of the current application
	 * @return boolean true if the rule was fulfilled or false otherwise
	 */
	public static boolean upScaleRuleFulfilled(Map<String, Map<String, Integer>> pidMetrics) {
		for (String pid : pidMetrics.keySet()) {
			if (!runRule(pidMetrics.get(pid), ruleNames.UPSCALE)) {
//				System.out.println("Upscale rule NOT fulfilled for pid: " + pid);
				return false;
			}
			System.out.println("Upscale rule fulfilled for pid: " + pid);
		}
		
		return true;
	}
	
	/**
	 * Public method that triggers the run of the downscale rules for all the monitored metrics
	 * @param pidMetrics a list of metrics for each instance of the current application
	 * @return boolean true if the rule was fulfilled or false otherwise
	 */
	public static boolean downScaleRuleFulfilled(Map<String, Map<String, Integer>> pidMetrics) {
		for (String pid : pidMetrics.keySet()) {
			if (!runRule(pidMetrics.get(pid), ruleNames.DOWNSCALE)) {
//				System.out.println("Downscale rule NOT fulfilled for pid: " + pid);
				return false;
			}
			System.out.println("Downscale rule fulfilled for pid: " + pid);
		}
		return true;
	}
	
	/**
	 * Method that should be used only to load the rule thresholds from the default location
	 * as specified in the DEFAULT_THRESHOLDS_FILE_LOCATION (should be "/WEB-INF/lib/params.properties")
	 * @param servletContext the servlet context used for opening the resource file
	 */
	public static void loadRuleThresholds(ServletContext servletContext) {
		loadRuleThresholds(servletContext, DEFAULT_THRESHOLDS_FILE_LOCATION);
	}
	
	/**
	 * Method that loads the rules thresholds from a specified resource file.
	 * @param servletContext the servlet context used for opening the resource file where the thresholds are found
	 * @param thresholdsFileLocation the classpath location url of the resource file containing the thresholds 
	 */
	public static void loadRuleThresholds(ServletContext servletContext, String thresholdsFileLocation) {
		Properties fallback = new Properties();
		fallback.put(DEFAULT_METRIC, DEFAULT_METRIC_VALUE);
		
		Properties prop = new Properties(fallback);
		try {
			InputStream stream = servletContext.getResourceAsStream(thresholdsFileLocation);
			try {
				prop.load(stream);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				stream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (Object metricObj : prop.keySet()) {
			String metricName= (String) metricObj;
			String thresholds = prop.getProperty(metricName);
			downscaleThresholds.put(metricName, Integer.valueOf(thresholds.substring(0, thresholds.indexOf(','))));
			upscaleThresholds.put(metricName, Integer.valueOf(thresholds.substring(thresholds.indexOf(',') + 1)));
		}
	}
}
