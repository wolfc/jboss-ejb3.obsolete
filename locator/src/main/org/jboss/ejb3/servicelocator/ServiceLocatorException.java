package org.jboss.ejb3.servicelocator;

public class ServiceLocatorException extends RuntimeException {

	// Class Members
	private static final long serialVersionUID = -8470028232704428218L;

	// Constructors
	public ServiceLocatorException(String arg0) {
		super(arg0);
	}

	public ServiceLocatorException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public ServiceLocatorException(Throwable arg0) {
		super(arg0);
	}
}
