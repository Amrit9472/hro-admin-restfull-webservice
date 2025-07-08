package com.eos.admin.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CompositeAuthenticationProvider implements AuthenticationProvider {

	private final AuthenticationProvider ourProvider;
	private final AuthenticationProvider vendorProvider;

	public CompositeAuthenticationProvider(@Qualifier("authenticationProvider") AuthenticationProvider ourProvider,
			@Qualifier("vendorAuthenticationProvider") AuthenticationProvider vendorProvider) {
		this.ourProvider = ourProvider;
		this.vendorProvider = vendorProvider;
	}

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (ourProvider.supports(authentication.getClass())) {
			try {
				log.info("Trying authentication via EmployeeProvider...");
				return ourProvider.authenticate(authentication);
			} catch (AuthenticationException ex) {
				log.warn("Employee authentication failed: {}", ex.getMessage());
			}
		}

		if (vendorProvider.supports(authentication.getClass())) {
			try {
				log.info("Trying authentication via VendorProvider...");
				return vendorProvider.authenticate(authentication);
			} catch (AuthenticationException vendorEx) {
				log.warn("Vendor authentication failed: {}", vendorEx.getMessage());
			}
		}

		throw new BadCredentialsException("Authentication failed for both employee and vendor providers.");

	}

	@Override
	public boolean supports(Class<?> authentication) {
		return ourProvider.supports(authentication) || vendorProvider.supports(authentication);
	}

}
