package com.sap.hana.cloud.samples.autoscaler;


public class Monitor implements Runnable {
	protected AppData appData;
	protected String monitoredAccount;
	protected String monitoredApp;
	
	private volatile boolean runFlag = true;
	
	public Monitor(AppData appData, String monitoredAccount, String monitoredApp) {
		this.monitoredAccount = monitoredAccount;
		this.monitoredApp = monitoredApp;
		this.appData = appData;
	}
	
	public void stop() {
		runFlag = false;
	}
	
	/**
	 * This is the monitoring trigger. It runs each 2 seconds and updates the application info, afterwards 
	 * issuing a REST call to the monitoring API for an update of the current metrics values for each
	 * application instance.
	 * After the information has been updated, the rules for scaling are being applied. 
	 */
	@Override
	public void run() {
		while (runFlag) {
			try {
				appData.getAppInfo(true);
				appData.updateCurrentMetrics();
				try2Scale();
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				runFlag = false;
			}
		}
	}

	/**
	 * Method that is called periodically and tries to do the scaling as follows:
	 * 1. If the number of running instances is lower than the number allowed, then check if the upscale rules are fulfilled
	 * 1.1. If yes, then start a new instance of the application.
	 * 2. If checks on #1 failed, then try to do something similar, but in the downscale direction. 
	 */
	private void try2Scale() {
		//try to scale up first
		if (appData.getAppInfo().getMaxProcesses() > appData.getAppInfo().getPids().size() && 
				RuleEngine.upScaleRuleFulfilled(appData.getParsedMetrics())) {
			appData.startApplication();
		} else if (appData.getAppInfo().getMinProcesses() < appData.getAppInfo().getPids().size() && 
				RuleEngine.downScaleRuleFulfilled(appData.getParsedMetrics())) {
			appData.stopApplicationProcess();
		}
	}

}
