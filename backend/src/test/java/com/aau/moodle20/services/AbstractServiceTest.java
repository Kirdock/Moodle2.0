package com.aau.moodle20.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractServiceTest {

    protected String adminMatriculationNumber ="00000000";
    protected String normalMatriculationNumber = "12345678";

    protected void mockSecurityContext_WithUserDetails(UserDetailsImpl userDetails)
    {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(userDetails);
    }

    protected UserDetailsImpl getUserDetails_Admin()
    {
        UserDetailsImpl userDetails = new UserDetailsImpl("admin","admin", Boolean.TRUE, adminMatriculationNumber,"admin","admin" );

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("Admin"));
        userDetails.setAuthorities(authorities);
        return userDetails;
    }

    protected UserDetailsImpl getUserDetails_Not_Admin()
    {
        UserDetailsImpl userDetails = new UserDetailsImpl("normal","normal", Boolean.FALSE, normalMatriculationNumber,"normal","normal" );
        List<GrantedAuthority> authorities = new ArrayList<>();
        userDetails.setAuthorities(authorities);
        return userDetails;
    }
}
