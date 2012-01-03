package android.hgd;

public class ServerDetails {
	private String hostname;
	private String port;
	private String user;
	private String password;
	
	public ServerDetails(String hostname, String port, String user, String password) {
		this.hostname = hostname;
		this.port = port;
		this.user = user;
		this.password = password;
	}
	
	public ServerDetails(String hostname, String port, String user) {
		this.hostname = hostname;
		this.port = port;
		this.user = user;
		this.password = "";
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public static ServerDetails toServerDetails(String serialisedServerDetails) {
		String user = serialisedServerDetails.split("@")[0];
		//String user = serialisedServerDetails.split("@")[0].split(":")[0];
		//String password = serialisedServerDetails.split("@")[0].split(":")[1];
		String hostname = serialisedServerDetails.split("@")[1].split(":")[0];
		String port = serialisedServerDetails.split("@")[1].split(":")[1];
		
		return new ServerDetails(hostname, port, user);//, password);
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
	
	public String getPassword() {
		return this.password;
	}
	
	public String toString() {
		//return getUser() + ":" + getPassword() + "@" + getHostname() + ":" + getPort();
		return getUser() + "@" + getHostname() + ":" + getPort();
	}
}
