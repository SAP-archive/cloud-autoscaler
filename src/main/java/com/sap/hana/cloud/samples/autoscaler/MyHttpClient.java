package com.sap.hana.cloud.samples.autoscaler;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.sap.core.connectivity.api.DestinationException;
import com.sap.core.connectivity.api.DestinationFactory;
import com.sap.core.connectivity.api.DestinationNotFoundException;
import com.sap.core.connectivity.api.http.HttpDestination;

/**
 *	HTTP client class for making API calls to the HCP Lifecycle Mgmt API and HCP Monitoring API.
 *  It requires 2 destinations to be configured, one for each of the APIs ("hcprestapi" and "hcpmonitoringapi")
 */
public class MyHttpClient {
	public String CSRFToken;
	public String errorMessage = "";
	
	private final String USER_AGENT = "Mozilla/5.0";
	
	private HttpDestination lmDestination, monDestination;
	
	private final String LM_DESTINATION_NAME = "hcprestapi";
	private final String MONITOR_DESTINATION_NAME = "hcpmonitoringapi";
	
	private class ClientBundle {
		public HttpClient client;
		public HttpRequestBase request;
		public HttpContext context;
		public String CSRFToken;
		public ClientBundle(HttpClient client, HttpRequestBase request, HttpContext context) {
			this.client = client;
			this.request = request;
			this.context = context;
		}
	}
	
	public MyHttpClient() {
		setDestinations();
		updateCSRFToken();
	}
	
	
	private void setDestinations() {
		Context ctx;
		try {
			ctx = new InitialContext();
			DestinationFactory destinationFactory = (DestinationFactory)ctx.lookup(DestinationFactory.JNDI_NAME);
			this.lmDestination = (HttpDestination) destinationFactory.getDestination(LM_DESTINATION_NAME);
			this.monDestination = (HttpDestination) destinationFactory.getDestination(MONITOR_DESTINATION_NAME);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (DestinationNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void updateCSRFToken() {
		this.CSRFToken = getCSRFToken4App().CSRFToken;
	}
	
	private ClientBundle getCSRFToken4App() {
		String newCSRFToken = null;
		ClientBundle clientBundle = null;
		try {
			Map<String, String> headers = new HashMap<>();
			headers.put("User-Agent", USER_AGENT);
			headers.put("X-CSRF-Token", "Fetch");
			
			clientBundle = prepareGETCSRFCall(lmDestination, "/csrf", headers);//makeGETCall(lmDestination, "/csrf", headers);
			HttpResponse response = clientBundle.client.execute(clientBundle.request, clientBundle.context);//httpContextsMap.get(client));
			
			Header[] respH = response.getHeaders("X-CSRF-Token");
			for (Header h : respH) {
				newCSRFToken = h.getValue();
				break;
			}
			clientBundle.CSRFToken = newCSRFToken;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return clientBundle;
	}
	
	
	public HttpDestination lmDestination() {
		return lmDestination;
	}
	
	public HttpDestination monDestination() {
		return monDestination;
	}
	
	private HttpContext getContextForClient(HttpClient client) {
		CookieStore cookieStore = new BasicCookieStore();
		HttpContext httpContext = new BasicHttpContext();
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		return httpContext;
	}
	
	public HttpResponse makeGETCall(HttpDestination destination, String extraURL) throws URISyntaxException,
			DestinationException, IOException, ClientProtocolException {
		Map<String, String> headers = new HashMap<>();
		headers.put("X-CSRF-Token", this.CSRFToken);
		return makeGETCall(destination, extraURL, headers);
	}
	
	public HttpResponse makeGETCall(HttpDestination destination, String extraURL, Map<String, String> headers) throws URISyntaxException,
	DestinationException, IOException, ClientProtocolException {
		String url = destination.getURI() + extraURL;
		HttpClient client = destination.createHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
		
		HttpGet request = new HttpGet(url);
		
		for(String headerKey : headers.keySet()) {
			request.addHeader(headerKey, headers.get(headerKey));
		}
		HttpResponse response = client.execute(request, getContextForClient(client));
		
		return response;
	}
	
	public ClientBundle prepareGETCSRFCall(HttpDestination destination, String extraURL, Map<String, String> headers) throws URISyntaxException,
	DestinationException, IOException, ClientProtocolException {
		String url = destination.getURI() + extraURL;
		HttpClient client = destination.createHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
		HttpGet request = new HttpGet(url);
		HttpContext context = getContextForClient(client);
		
		for(String headerKey : headers.keySet()) {
			request.addHeader(headerKey, headers.get(headerKey));
		}
		
		return new ClientBundle(client, request, context);
	}
	
	public HttpResponse makePUTCall(HttpDestination destination, String extraURL, String body) throws URISyntaxException,
	DestinationException, IOException, ClientProtocolException {
		Map<String, String> headers = new HashMap<>();
		headers.put("X-CSRF-Token", this.CSRFToken);
		return makePUTCall(destination, extraURL, headers, body);
	}
		
	public HttpResponse makePUTCall(HttpDestination destination, String extraURL, Map<String, String> headers, String body) throws URISyntaxException,
		DestinationException, IOException, ClientProtocolException {
		String url = destination.getURI() + extraURL;
		HttpClient client = destination.createHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
		HttpPut request = new HttpPut(url);
		request.setEntity(new StringEntity(body));
		
		for(String headerKey : headers.keySet()) {
			request.addHeader(headerKey, headers.get(headerKey));
		}
		HttpResponse response = client.execute(request, getContextForClient(client));//httpContextsMap.get(client));
		return response;
	}
	
	public HttpResponse makeCSRFPUTCall(HttpDestination destination, String extraURL, Map<String, String> headers, String body) throws URISyntaxException,
	DestinationException, IOException, ClientProtocolException {
		
		ClientBundle clientBundle = getCSRFToken4App();
		
		headers.put("X-CSRF-Token", clientBundle.CSRFToken);
		
		String url = destination.getURI() + extraURL;
		HttpClient client = clientBundle.client;
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
		HttpPut request = new HttpPut(url);
		request.setEntity(new StringEntity(body));
		
		for(String headerKey : headers.keySet()) {
			request.addHeader(headerKey, headers.get(headerKey));
		}
		HttpResponse response = client.execute(request, clientBundle.context);//httpContextsMap.get(client));
		return response;
	}
}
