package android.hgd;

public class ServerDetails {
	private String hostname;
	private String port;
	private String user;
	
	public ServerDetails(String hostname, String port, String user) {
		this.hostname = hostname;
		this.port = port;
		this.user = user;
	}
	
	public String getHostname() {
		return this.hostname;
	}
	
	public String getPort() {
		return this.port;
	}
	
	public String getUser() {
		return this.user;
	}
	
	public String toString() {
		return getUser() + "@" + getHostname() + ":" + getPort();
	}
}
