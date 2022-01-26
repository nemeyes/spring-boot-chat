package com.seerstech.chat.server.config;

import com.seerstech.chat.server.jwt.JWTAuthEntryPoint;
import com.seerstech.chat.server.jwt.JWTAuthTokenFilter;
import com.seerstech.chat.server.service.ChatUserDetailsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;


/**
 * Web Security 설정
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
		// securedEnabled = true,
		// jsr250Enabled = true,
		prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	ChatUserDetailsService mChatUserDetailsService;
	
	@Autowired
	private JWTAuthEntryPoint mUnauthorizedHandler;
	
	@Bean
	public JWTAuthTokenFilter authTokenFilter() {
		return new JWTAuthTokenFilter();
	}
	
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        return bCryptPasswordEncoder;
    }
    
    @Bean
    @Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	
    	http.httpBasic().disable()
    		.cors().configurationSource(corsConfigurationSource())
    		.and()
    		.csrf().disable()
			.exceptionHandling().authenticationEntryPoint(mUnauthorizedHandler).and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.authorizeRequests()
			.antMatchers(HttpMethod.POST, "/user/login").permitAll()
			.antMatchers(HttpMethod.POST, "/user/logout").authenticated()
			.antMatchers(HttpMethod.POST, "/user/reissue").permitAll()
			.antMatchers(HttpMethod.POST, "/user/signup").authenticated()
			//.antMatchers("/user/**").authenticated()
			.antMatchers("/chat/**").authenticated()
			.and()
			.addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    	
    	//http.addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    	
    	/*
        http
            .csrf().disable() // 기본값이 on인 csrf 취약점 보안을 해제한다. on으로 설정해도 되나 설정할경우 웹페이지에서 추가처리가 필요함.
            .headers()
                .frameOptions().sameOrigin() // SockJS는 기본적으로 HTML iframe 요소를 통한 전송을 허용하지 않도록 설정되는데 해당 내용을 해제한다.
            .and()
                .formLogin().loginPage("/user/signin") // 권한없이 페이지 접근하면 로그인 페이지로 이동한다. .loginPage("/user/signin")
                .usernameParameter("user_id")
                .passwordParameter("user_password")
            .and()
                .authorizeRequests()
                	.antMatchers("/user/**").permitAll()
                    .antMatchers("/chat/**").hasRole("USER") // chat으로 시작하는 리소스에 대한 접근 권한 설정
                    .anyRequest().permitAll(); // 나머지 리소스에 대한 접근 설정
    	 */
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    	auth.userDetailsService(mChatUserDetailsService).passwordEncoder(passwordEncoder());
    }
}