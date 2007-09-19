package org.jboss.ejb3.servicelocator;

public class Ejb3NotFoundException extends RuntimeException {
	// Class Members
	private static final long serialVersionUID = 6533428942404073608L;

	// Constructors
	public Ejb3NotFoundException(String arg0) {
		super(arg0);
	}

	public Ejb3NotFoundException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public Ejb3NotFoundException(Throwable arg0) {
		super(arg0);
	}
}
