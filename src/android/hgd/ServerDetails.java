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
	
	public static ServerDetails toServerDetails(String serialisedServerDetails) {
		String user = serialisedServerDetails.split("@")[0];
		String hostname = serialisedServerDetails.split("@")[1].split(":")[0];
		String port = serialisedServerDetails.split("@")[1].split(":")[1];
		
		return new ServerDetails(hostname, port, user);
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
