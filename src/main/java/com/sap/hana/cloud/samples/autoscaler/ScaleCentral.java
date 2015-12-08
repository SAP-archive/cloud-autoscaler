package com.sap.hana.cloud.samples.autoscaler;

import java.util.Map;

/**
 * This is the central singleton class that controls the scaling.
 */
public class ScaleCentral {
	private static ScaleCentral instance = null;
	private Monitor monitor = null;
	private Thread monitorThread = null;
	
	protected AppData appData;
	
	protected String monitoredAccount;
	protected String monitoredApp;
	
	private ScaleCentral() {
	}
	
	public static ScaleCentral getInstance() {
		if (instance == null) {
			instance = new ScaleCentral();
		}
		return instance;
	}
	
	/**
	 * This method takes as input an HCP account and application name and fires up an update on the application info,
	 * i.e. sets the new coordinates (accountName, applicationName) and then issues a call to the Lifecycle Mgmt. REST API
	 * in order to get the application information.
	 * NOTE: It might happen that the application / account are non-accessible due to security restrictions. Please be aware
	 * of that when trying to monitor / scale an application.
	 * @param accountName the account name
	 * @param applicationName the application name
	 */
	public void updateApp2Scale(String accountName, String applicationName) {
		monitoredAccount = accountName;
		monitoredApp = applicationName;
		appData = new AppData(monitoredAccount, monitoredApp);
	}
	
	/**
	 * Method that returns the cached application information. 
	 * @return application properties as an {@link AppProps} object
	 */
	public AppProps getAppInfo() {
		return appData.getAppInfo();
	}
	
	/**
	 * Method that returns the updated application information by triggering a new REST call to the lifecycle mgmt API.
	 * Refreshes the cache as well. 
	 * @return application properties as an {@link AppProps} object
	 */
	public AppProps getUpdatedAppInfo() {
		return appData.getAppInfo(true);
	}
	
	/**
	 * Method that returns a map of the current monitored application metrics.
	 * @return
	 */
	public Map<String, String> getAppMetrics() {
		return appData.getMetrics();
	}
	
	/**
	 * Method that returns a map of the current monitored application metrics for each instance of the application.
	 * @return
	 */
	public Map<String, Map<String, Integer>> getParsedAppMetrics() {
		return appData.getParsedMetrics();
	}
	
	public void startMonitor() {
		if (monitor == null) {
			monitor = new Monitor(appData, monitoredAccount, monitoredApp);
			monitorThread = new Thread(monitor);
			monitorThread.start();
		}
	}
	
	public void stopMonitor() {
		try {
			if (monitorThread != null) {
				monitor.stop();
				monitorThread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			monitor = null;
		}
	}
	
	public String startApplication() {
		return appData.startApplication();
	}
	
	public String stopApplicationProcess() {
		return appData.stopApplicationProcess();
	}
}
