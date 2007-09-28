

import java.io.Serializable;

/**
 * Represents a binding between a service name, its location in the JNDI tree
 * and the JNP host serving the application
 * 
 * @author ALR
 */
public class ServiceHostProximityBinding implements Serializable {

	// Class Members
	private static final long serialVersionUID = 3251615680702644132L;

	// Instance Members
	private String name;

	private String jndiLocation;

	private String host;

	// Constructors
	public ServiceHostProximityBinding() {
	}

	public ServiceHostProximityBinding(String name, String jndiLocation,
			String host, boolean localToJvm) {
		this.setName(name);
		this.setJnpHost(host);
		this.setJndiLocation(jndiLocation);
	}

	// Accessors/Mutators
	public String host() {
		return host;
	}

	public void setJnpHost(String address) {
		this.host = address;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJndiLocation() {
		return jndiLocation;
	}

	public void setJndiLocation(String jndiLocation) {
		this.jndiLocation = jndiLocation;
	}

}
