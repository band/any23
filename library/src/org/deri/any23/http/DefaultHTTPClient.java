package org.deri.any23.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

/**
 * Opens an {@link InputStream} on an HTTP URI. Is configured
 * with sane values for timeouts, default headers and so on.
 * 
 * @author Paolo Capriotti
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class DefaultHTTPClient implements HTTPClient {
	private static final int DEFAULT_TIMEOUT = 5000;
	private static final int DEFAULT_TOTAL_CONNECTIONS = 5;

	private final MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
	private final String userAgent;
	private final String accept;
	private HttpClient client = null;
	
	public DefaultHTTPClient(String userAgent) {
		this(userAgent, null);
	}
	
	public DefaultHTTPClient(String userAgent, String accept) {
		this.userAgent = userAgent;
		this.accept = accept;
		System.out.println(accept);
	}

	private void ensureClientInitialized() {
		if (client != null) return;
		client = new HttpClient(manager);
		HttpConnectionManager connectionManager = client.getHttpConnectionManager();
		HttpConnectionManagerParams params = connectionManager.getParams();
		params.setConnectionTimeout(DEFAULT_TIMEOUT);
		params.setSoTimeout(DEFAULT_TIMEOUT);
		params.setMaxTotalConnections(DEFAULT_TOTAL_CONNECTIONS);

		HostConfiguration hostConf = client.getHostConfiguration();
		List<Header> headers = new ArrayList<Header>();
		headers.add(new Header("User-Agent", userAgent));
		if (accept != null) {
			headers.add(new Header("Accept", accept));
		}
		headers.add(new Header("Accept-Language", "en-us,en-gb,en,*;q=0.3"));
		headers.add(new Header("Accept-Charset", "utf-8,iso-8859-1;q=0.7,*;q=0.5"));
		// headers.add(new Header("Accept-Encoding", "x-gzip, gzip"));
		hostConf.getParams().setParameter("http.default-headers", headers);
	}

	// Will follow redirects
	/* (non-Javadoc)
	 * @see org.deri.any23.http.HTTPClient#openInputStream(java.lang.String)
	 */
	public InputStream openInputStream(String uri) throws IOException {
		ensureClientInitialized();
		GetMethod method = new GetMethod(uri);
		method.setFollowRedirects(true);
		client.executeMethod(method);
		return method.getResponseBodyAsStream();
	}

	/* (non-Javadoc)
	 * @see org.deri.any23.http.HTTPClient#close()
	 */
	public void close() {
		manager.shutdown();
	}
}