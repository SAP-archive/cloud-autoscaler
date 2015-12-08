package com.sap.hana.cloud.samples.autoscaler;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *	Class that holds the application properties and utilities for string / JSON manipulation.
 */
public class AppProps {
	private String rawProps;
	private JSONObject props;
	
	private int minProcesses, maxProcesses;
	private Set<String> pids = new HashSet<String>(); 
	
	public AppProps() {
		minProcesses = 1;
		maxProcesses = 1;
	}

	public synchronized void update(String rawProps) {
		setRawProps(rawProps);
		setProps(new JSONObject(rawProps));
		setPids(getPidsFromProps(props));
		setMinProcesses(getMinProcessesFromProps(props));
		setMaxProcesses(getMaxProcessesFromProps(props));
	}

	private void setProps(JSONObject props) {
		this.props = props;
	}
	
	public synchronized JSONObject getProps() {
		return props;
	}

	@Override
	public synchronized String toString() {
		return rawProps;
	}

	private void setRawProps(String rawProps) {
		this.rawProps = rawProps;
	}
	
	private void setPids(HashSet<String> pids) {
		this.pids = pids;
	}
	
	public synchronized Set<String> getPids() {
		return pids;
	}
	
	public synchronized int getMinProcesses() {
		return minProcesses;
	}

	private void setMinProcesses(int minProcesses) {
		this.minProcesses = minProcesses;
	}

	public synchronized int getMaxProcesses() {
		return maxProcesses;
	}

	private void setMaxProcesses(int maxProcesses) {
		this.maxProcesses = maxProcesses;
	}
	
	public static HashSet<String> getPidsFromProps(JSONObject props) {
		HashSet<String> pids = new HashSet<>();
		JSONArray pidArray = props.getJSONObject("entity").getJSONObject("state").getJSONArray("processes");
		int i = 0;
		while (!pidArray.isNull(i)) {
			pids.add(pidArray.getJSONObject(i++).getString("processId"));
		}
		return pids;
	}
	
	public static int getMinProcessesFromProps(JSONObject props) {
		int minP = 1;
		try {
			minP = props.getJSONObject("entity").getInt("minProcesses");
		} catch (Exception e) {
			//just ignore
		}
		
		return minP;
	}
	
	public static int getMaxProcessesFromProps(JSONObject props) {
		int maxP = 1;
		try {
			maxP = props.getJSONObject("entity").getInt("maxProcesses");
		} catch (Exception e) {
			//just ignore
		}
		
		return maxP;
	}
}