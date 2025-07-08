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

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private static final String ROLE_ADMIN = "ADMIN";
	private static final String ROLE_USER = "USER";
	private static final String ROLE_MANAGER = "MANAGER";
	private static final String ROLE_VENDOR = "VENDOR";
	private static final String ROLE_ER = "ER";
	private static final String ROLE_TRAINER = "TRAINER";
	private static final String ROLE_MIS = "MIS";

	private OurUserDetailsServiceImpl ourUserDetailsServiceImpl;
	private VendorUserDetailsServiceImpl vendorUserDetailsService;
	private JWTAuthFilter jwtAuthFilter;

	public SecurityConfig(OurUserDetailsServiceImpl ourUserDetailsServiceImpl, JWTAuthFilter jwtAuthFilter,
			VendorUserDetailsServiceImpl vendorUserDetailsService) {
		super();
		this.ourUserDetailsServiceImpl = ourUserDetailsServiceImpl;
		this.jwtAuthFilter = jwtAuthFilter;
		this.vendorUserDetailsService = vendorUserDetailsService;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(AbstractHttpConfigurer::disable).cors(Customizer.withDefaults())
				.authorizeHttpRequests(Request -> Request
						.requestMatchers("/auth/**", "/public/**", "/auth/vendor/**", "/images/**" ,"/api/vendorInfo/company-names-for-users").permitAll()
						.requestMatchers("/api/employees/createEmployee").permitAll()
						.requestMatchers("/api/employees/vendor").permitAll()
						.requestMatchers("/api/email/send-interview-email").permitAll().requestMatchers("/admin/**")
						.hasAnyAuthority(ROLE_ADMIN).requestMatchers("/user/**")
						.hasAnyAuthority(ROLE_USER, ROLE_MANAGER).requestMatchers("/api/employees/**")
						.hasAnyAuthority(ROLE_USER, ROLE_ADMIN, ROLE_MANAGER).requestMatchers("/api/loi/**")
						.hasAnyAuthority(ROLE_USER, ROLE_ADMIN, ROLE_MANAGER).requestMatchers("/adminuser/**")
						.hasAnyAuthority(ROLE_USER, ROLE_ADMIN, ROLE_MANAGER).requestMatchers("/api/candi/**")
						.hasAnyAuthority(ROLE_VENDOR, ROLE_ADMIN)
						.requestMatchers("/api/vendorInfo/**").hasAnyAuthority(ROLE_VENDOR, ROLE_ADMIN, ROLE_MIS)
						.requestMatchers("/api/vendor/dashboard/status").hasAnyAuthority(ROLE_VENDOR)
						.requestMatchers("/api/attendance/**").hasAnyAuthority(ROLE_ER, ROLE_TRAINER)
						.requestMatchers("/api/training-attendance/**").hasAnyAuthority(ROLE_TRAINER)
						.requestMatchers("/api/process/dropdown").permitAll()
						.requestMatchers("/api/process/**").hasAnyAuthority(ROLE_ADMIN, ROLE_TRAINER, ROLE_ER)
						.requestMatchers("/api/training-scores/**").hasAnyAuthority(ROLE_TRAINER)
                        .requestMatchers("/api/passwordRest/**").permitAll()
//						.requestMatchers("/api/schemes/**").permitAll()
                        .requestMatchers("/api/schemes/**").hasAnyAuthority(ROLE_MIS,ROLE_VENDOR)
						.requestMatchers("/api/insert/training/**").permitAll()
//						.requestMatchers("/api/vendorInfo/company-names").permitAll()
						.requestMatchers("/api/insert/training/batchcodes").permitAll()
						.requestMatchers("/api/pincode/**").permitAll().anyRequest().authenticated())
//		.sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//		.authenticationProvider(authenticationProvider())
//		.authenticationProvider(vendorAuthenticationProvider())
//		.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//		return httpSecurity.build();
				.exceptionHandling(ex -> ex.authenticationEntryPoint((req, res, ex1) -> {
					System.out.println(">>> EntryPoint triggered: " + ex1.getMessage());
					res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					res.getWriter().write("Unauthorized");
				})).formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
				.sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationProvider(compositeAuthenticationProvider())
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
				.logout(logout -> logout.logoutUrl("/auth/vendor/logout").logoutSuccessHandler((req, res, auth) -> {
					res.setStatus(HttpServletResponse.SC_OK);
					res.setContentType("application/json");
					res.getWriter().write("{\"message\":\"Vendor logged out successfully\"}");
				}).invalidateHttpSession(true).deleteCookies("JSESSIONID"));

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
	public AuthenticationProvider compositeAuthenticationProvider() {
		return new CompositeAuthenticationProvider(authenticationProvider(), vendorAuthenticationProvider());
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

//	@Bean
//	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception{
//		return authenticationConfiguration.getAuthenticationManager();
//	}
//	@Bean
//	@Primary
//	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
//		return http.getSharedObject(AuthenticationManagerBuilder.class).authenticationProvider(authenticationProvider())
//				.authenticationProvider(vendorAuthenticationProvider()).build();
//	}

	@Bean
	@Primary
	public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
		return new ProviderManager(compositeAuthenticationProvider());
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
