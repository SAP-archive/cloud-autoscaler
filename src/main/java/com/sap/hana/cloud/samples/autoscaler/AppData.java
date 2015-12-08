package com.sap.hana.cloud.samples.autoscaler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;

import com.sap.core.connectivity.api.DestinationException;

/**
 * Class containing the cached data for the monitored application plus some utilities.
 */
public class AppData {
	protected Map<String, String> monMap = new HashMap<>();
	protected Map<String, Map<String, Integer>> parsedMonMap = new HashMap<>();
	
	protected MyHttpClient httpClient;
	protected String account;
	protected String app;
	
	private AppProps props = new AppProps();
	
	public AppData(String account, String app) {
		this.account = account;
		this.app = app;
		httpClient = new MyHttpClient();
		updateAppInfo();
	}
	
	public MyHttpClient httpClient() {
		return httpClient;
	}
	
	/**
	 * Method that returns the application information.
	 * @param forceUpdate if true, then a REST call is issued to the lifecycle mgmt API to refresh the cache.
	 * @return application properties as an {@link AppProps} object
	 */
	public AppProps getAppInfo(boolean forceUpdate) {
		if (forceUpdate)
			updateAppInfo();
		return getAppInfo();
	}
	
	/**
	 * Getter for the cached application properties.
	 * @return application properties as an {@link AppProps} object
	 */
	public synchronized AppProps getAppInfo() {
		return props;
	}
	
	/**
	 * Method that updates the application info by firing a REST call to the HCP lifecycle mgmt API.
	 */
	private synchronized void updateAppInfo() {
		StringBuilder returnMess = new StringBuilder();
		try {
			//httpClient.updateCSRFToken();
			HttpResponse response = httpClient.makeGETCall(httpClient.lmDestination(), "/accounts/" + account + "/apps/" + app + "/");
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
	 
			String line = "";
			while ((line = rd.readLine()) != null) {
				returnMess.append(line);
			}
			props.update(returnMess.toString()); //new JSONObject(returnMess.toString());
		} catch (DestinationException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();;
		}
	}

	/**
	 * Setter for the list of application instances.
	 * @return
	 */
	public synchronized Set<String> getPids() {
		return props.getPids();
	}

	/**
	 * Method that updates the cached metrics from HCP monitoring API calls.
	 */
	public synchronized void updateCurrentMetrics() {
		monMap.clear();
		parsedMonMap.clear();
		for (String pid : props.getPids()) {
			monMap.put(pid, getCurrentMetricsForProcess(pid));
			parsedMonMap.put(pid, parseCurrentMetricsForProcess(pid));
		}
	}
	
	public synchronized Map<String, String> getMetrics() {
		return monMap;
	}
	
	public synchronized Map<String, Map<String, Integer>> getParsedMetrics() {
		return parsedMonMap;
	}
	
	private synchronized Map<String, Integer> parseCurrentMetricsForProcess(String pid) {
		return MetricUtils.parseMetrics(monMap.get(pid));
	}
	
	/**
	 * Get the current metrics for a specified instance of the application.
	 * @param pid the id of the process mapped on the current application instance.
	 * @return stringified JSON containing the current metrics for the specified instance.
	 */
	private synchronized String getCurrentMetricsForProcess(String pid) {
		String updatedMetrics = "";
		try {
			HttpResponse response = httpClient.makeGETCall(httpClient.monDestination(), "/metrics/application/" + account 
					+ "/" + app	+ "/_null/" + pid);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			
			StringBuilder returnMess = new StringBuilder();
			String line = "";
			while ((line = rd.readLine()) != null) {
				returnMess.append(line);
			}
			updatedMetrics = returnMess.toString();
		} catch (DestinationException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return updatedMetrics;
	}

	public String startApplication() {
		StringBuilder returnString = new StringBuilder();
		String body = "{\"applicationState\":\"STARTED\"}";
		try {
			Map<String, String> headers = new HashMap<>();
			headers.put("Content-type", "application/json");
			
			HttpResponse response = httpClient.makeCSRFPUTCall(httpClient.lmDestination(), "/accounts/" + account + "/apps/" + app + "/state", 
					headers, body);
			returnString.append(EntityUtils.toString(response.getEntity()));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			updateAppInfo();
		}
		
		return returnString.toString();
	}
	
	public synchronized String stopApplicationProcess() {
		String pid2Stop = "";
		Iterator<String> pidsIterator = props.getPids().iterator();
		if (pidsIterator.hasNext()) {
			pid2Stop = pidsIterator.next();
		}
		
		StringBuilder returnString = new StringBuilder();
		returnString.append("Stopping process with ID: " + pid2Stop);
		String body = "{\"processState\":\"STOPPED\"}";
		try {
			Map<String, String> headers = new HashMap<>();
			headers.put("Content-type", "application/json");
			
			HttpResponse response = httpClient.makeCSRFPUTCall(httpClient.lmDestination(), "/shortcuts/processes/" + pid2Stop + "/state", 
					headers, body);
			returnString.append(EntityUtils.toString(response.getEntity()));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			updateAppInfo();
		}
		
		return returnString.toString();
	}
}
