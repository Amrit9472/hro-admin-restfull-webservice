package com.eos.admin.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.eos.admin.serviceImpl.OurUserDetailsServiceImpl;

import com.eos.admin.serviceImpl.VendorUserDetailsServiceImpl;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private OurUserDetailsServiceImpl ourUserDetailsServiceImpl;
	private VendorUserDetailsServiceImpl vendorUserDetailsService;
	private JWTAuthFilter jwtAuthFilter;
	
	
	public SecurityConfig(OurUserDetailsServiceImpl ourUserDetailsServiceImpl, JWTAuthFilter jwtAuthFilter,VendorUserDetailsServiceImpl vendorUserDetailsService) {
		super();
		this.ourUserDetailsServiceImpl = ourUserDetailsServiceImpl;
		this.jwtAuthFilter = jwtAuthFilter;
		this.vendorUserDetailsService = vendorUserDetailsService;
	}

	private static final String ROLE_ADMIN = "ADMIN";
	private static final String ROLE_USER = "USER";
	private static final String ROLE_MANAGER = "MANAGER";
	private static final String ROLE_VENDOR = "VENDOR";
	private static final String ROLE_ER= "ER";
	private static final String ROLE_TRAINER = "TRAINER";
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(AbstractHttpConfigurer::disable)
		.cors(Customizer.withDefaults())
		.authorizeHttpRequests(Request -> Request.requestMatchers("/auth/**", "/public/**", "/auth/vendor/**", "/images/**").permitAll().
				requestMatchers("/api/employees/createEmployee").permitAll()
				.requestMatchers("/api/employees/vendor").permitAll()
				.requestMatchers("/api/email/send-interview-email").permitAll()
				.requestMatchers("/admin/**").hasAnyAuthority(ROLE_ADMIN)
				.requestMatchers("/user/**").hasAnyAuthority(ROLE_USER,ROLE_MANAGER)
				.requestMatchers("/api/employees/**").hasAnyAuthority(ROLE_USER,ROLE_ADMIN,ROLE_MANAGER)
				.requestMatchers("/api/loi/**").hasAnyAuthority(ROLE_USER,ROLE_ADMIN,ROLE_MANAGER)
				.requestMatchers("/adminuser/**").hasAnyAuthority(ROLE_USER,ROLE_ADMIN,ROLE_MANAGER)
				.requestMatchers("/api/candi/**").hasAnyAuthority(ROLE_VENDOR,ROLE_ADMIN)			
				.requestMatchers("/api/candidates/**").hasAnyAuthority(ROLE_VENDOR,ROLE_ADMIN)
				.requestMatchers("/api/vendorInfo/**").hasAnyAuthority(ROLE_VENDOR,ROLE_ADMIN)
				.requestMatchers("/api/attendance/**").hasAnyAuthority(ROLE_ER,ROLE_TRAINER)
				.requestMatchers("/api/training-attendance/**").hasAnyAuthority(ROLE_TRAINER)
				.requestMatchers("/api/training-batch/**").hasAnyAuthority(ROLE_TRAINER, ROLE_ER)
				.requestMatchers("/api/training/**").hasAnyAuthority(ROLE_TRAINER, ROLE_ER)
				.requestMatchers("/api/training-scores/**").hasAnyAuthority(ROLE_TRAINER, ROLE_ER)
				.requestMatchers("/api/process/**").hasAnyAuthority(ROLE_ADMIN)
				.requestMatchers("/api/trainer-master/**").hasAnyAuthority(ROLE_TRAINER, ROLE_ADMIN, ROLE_USER)
				.anyRequest().authenticated())
		.sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		.authenticationProvider(authenticationProvider())
		.authenticationProvider(vendorAuthenticationProvider())
		.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
		return httpSecurity.build();
	
	}


	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(ourUserDetailsServiceImpl);
		daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
		return daoAuthenticationProvider;
	}

	@Bean
	public AuthenticationProvider vendorAuthenticationProvider() {
	    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
	    provider.setUserDetailsService(vendorUserDetailsService);
	    provider.setPasswordEncoder(passwordEncoder());
	    return provider;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	

//	@Bean
//	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
//		return authenticationConfiguration.getAuthenticationManager();
//	}
	 @Bean
	 @Primary 
	    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
	        return http.getSharedObject(AuthenticationManagerBuilder.class)
	            .authenticationProvider(authenticationProvider())
	            .authenticationProvider(vendorAuthenticationProvider())
	            .build();
	    }
	 @Bean
	    @Qualifier("vendorAuthenticationManager")
	    public AuthenticationManager vendorAuthenticationManager() {
	        return new ProviderManager(vendorAuthenticationProvider());
	    }
	 @Bean
	 public WebSecurityCustomizer webSecurityCustomizer() {
	     return (web) -> web.ignoring().requestMatchers("/images/**");
	 }

}
