	package com.ecommerce.product.config;
	
	import org.springframework.context.annotation.Bean;
	import org.springframework.context.annotation.Configuration;
	import org.springframework.http.HttpMethod;
	import org.springframework.security.config.Customizer;
	import org.springframework.security.config.annotation.web.builders.HttpSecurity;
	import org.springframework.security.web.SecurityFilterChain;
	import org.springframework.security.core.userdetails.User;
	import org.springframework.security.core.userdetails.UserDetails;
	import org.springframework.security.provisioning.InMemoryUserDetailsManager;
	import org.springframework.security.core.userdetails.UserDetailsService;
	import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
	import org.springframework.security.crypto.password.PasswordEncoder;
	
	@Configuration
	public class SecurityConfig {
	
		@Bean
		SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		    http
		        .csrf(csrf -> csrf.disable())
		        .authorizeHttpRequests(auth -> auth
		            .requestMatchers("/swagger-ui.html","/swagger-ui/**","/v3/api-docs/**").permitAll()
		            .requestMatchers(HttpMethod.DELETE,"/api/v1/products/**").hasRole("ADMIN")
		            .anyRequest().authenticated()
		        )
		        .httpBasic(Customizer.withDefaults());

		    return http.build();
		}
//	    @Bean
//	    UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
//	
//	        UserDetails admin = User.builder()
//	                .username("admin")
//	                .password(passwordEncoder.encode("admin123"))
//	                .roles("ADMIN")
//	                .build();
//	
//	        UserDetails user = User.builder()
//	                .username("user")
//	                .password(passwordEncoder.encode("user123"))
//	                .roles("USER")
//	                .build();
//	
//	        return new InMemoryUserDetailsManager(admin, user);
//	    }
	
	    @Bean
	    PasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }
	}