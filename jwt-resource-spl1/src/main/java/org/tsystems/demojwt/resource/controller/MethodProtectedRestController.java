package org.tsystems.demojwt.resource.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@RestController
public class MethodProtectedRestController {
	
	private RestOperations restTemplate;
	@Value("${rest.remote.protected}")
	private String path;

	public MethodProtectedRestController() {
		restTemplate = new RestTemplate();
	}
	
    /**
     * Example of granular restriction for endpoints. It's possible to use SPEL expressions
     * in @PreAuthorize such as 'hasRole()' to determine if a user has access. hasRole expression assumes a
     * 'ROLE_' prefix on all role names. So 'ADMIN' here is actually stored as 'ROLE_ADMIN' in database!
     **/
    @RequestMapping(path = "/protected-local", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getProtectedGreetingLocal() {
        return ResponseEntity.ok("Greetings from admin protected method  in sample 1!");
    }

    @RequestMapping(path = "/protected-remote", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getProtectedGreetingRemote() {
    	HttpEntity<String> httpEntity = new HttpEntity<>(createHeaders());
		ResponseEntity<String> responseEntity = restTemplate.exchange(path, HttpMethod.GET, httpEntity, String.class);	
		return responseEntity;			
    }

	private HttpHeaders createHeaders() {
		UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", (String) authentication.getCredentials());
		return headers;
	}
}