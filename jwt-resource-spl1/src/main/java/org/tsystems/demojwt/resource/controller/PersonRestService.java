package org.tsystems.demojwt.resource.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@RestController
public class PersonRestService {

	private RestOperations restTemplate;
	private static final List<Person> persons;
	@Value("${rest.remote.persons}")
	private String path;

	static {
		persons = new ArrayList<>();
		persons.add(new Person("Hello", "World in sample spl1"));
		persons.add(new Person("Foo", "Bar in sample spl1"));
	}

	public PersonRestService() {
		restTemplate = new RestTemplate();
	}

	@RequestMapping(path = "/persons-local", method = RequestMethod.GET)
	public List<Person> getPersonsLocal() {
		return persons;
	}

	@RequestMapping(path = "/persons-local/{name}", method = RequestMethod.GET)
	public Person getPersonLocal(@PathVariable("name") String name) {
		return persons.stream().filter(person -> name.equalsIgnoreCase(person.getName())).findAny().orElse(null);
	}

	@RequestMapping(path = "/persons-remote", method = RequestMethod.GET)
	public List<Person> getPersonsRemote() {
		HttpEntity<String> httpEntity = new HttpEntity<>(createHeaders());
		ResponseEntity<List<Person>> responseEntity = restTemplate.exchange(path, HttpMethod.GET, httpEntity,
				new ParameterizedTypeReference<List<Person>>() {
				});
		return responseEntity.getBody();
	}

	@RequestMapping(path = "/persons-remote/{name}", method = RequestMethod.GET)
	public Person getPersonRemote(@PathVariable("name") String name) {
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<String, String>();
		formData.add("name", name);
		HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(formData, createHeaders());
		ResponseEntity<Person> responseEntity = restTemplate.exchange(path, HttpMethod.GET, httpEntity,
				new ParameterizedTypeReference<Person>() {
				});
		return responseEntity.getBody();
	}

	private HttpHeaders createHeaders() {
		UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", (String) authentication.getCredentials());
		return headers;
	}
}