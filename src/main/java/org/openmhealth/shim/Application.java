package org.openmhealth.shim;

import org.openmhealth.shim.jawbone.JawboneShim;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

@Configuration
@EnableAutoConfiguration
@EnableWebSecurity
@RestController
public class Application extends WebSecurityConfigurerAdapter {

    private Shim jawboneShim = new JawboneShim();

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("user").password("password").roles("USER");
    }

    /**
     * Endpoint for triggering domain approval.
     *
     * @param shim - The shim registry key of the shim we're approving
     * @return - AuthorizationRequest parameters, including a boolean
     * flag if already authorized.
     */
    @RequestMapping("/authorize/{shim}")
    public ResponseEntity<AuthorizationRequestParameters> authorize(@PathVariable("shim") String shim) {
        return new ResponseEntity<AuthorizationRequestParameters>(
            jawboneShim.getAuthorizationRequestParameters(Collections.<String, String>emptyMap())
            , HttpStatus.OK);
    }

    /**
     * Endpoint for handling approvals from external data providers
     *
     * @param servletRequest - Request posted by the external data provider.
     * @return - AuthorizationResponse object with details
     * and result: authorize, error, or denied.
     */
    @RequestMapping(value = "/authorize/{shim}/callback",
        method = {RequestMethod.POST, RequestMethod.GET})
    public ResponseEntity<AuthorizationResponse> approve(HttpServletRequest servletRequest) {
        return new ResponseEntity<AuthorizationResponse>(
            jawboneShim.handleAuthorizationResponse(servletRequest), HttpStatus.OK);
    }

    /**
     * Endpoint for retrieving data from shims.
     *
     * @return - The shim data response wrapper with data from the shim.
     */
    @RequestMapping(value = "/data/{shim}/{dataType}", produces = "application/json")
    public ResponseEntity<ShimDataResponse> data(HttpServletRequest servletRequest) {
        ShimDataRequest shimDataRequest =
            ShimDataRequest.fromHttpRequest(servletRequest);
        return new ResponseEntity<ShimDataResponse>(
            jawboneShim.getData(shimDataRequest), HttpStatus.OK);
    }
}