package com.yunhwan.cloudsimlab.userarchitecture.application;

public class UserArchitectureNotFoundException extends RuntimeException {

	public UserArchitectureNotFoundException(String architectureId) {
		super("User architecture not found: " + architectureId);
	}
}
