package org.nthdimenzion.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author: Samir
 * @since 1.0 13/02/2015
 */
@Setter
public class UserLoginDetailDto implements UserDetails {


    private List<String> permissions;

    private Collection<SimpleGrantedAuthority> authorities;

    private String username;

    private String password;

    UserLoginDetailDto() {

    }

    private UserLoginDetailDto(String userName, String password) {
        this.username = userName;
        this.password = password;
    }

    public static UserLoginDetailDto createUserLoginDetailDto(String userName, String password) {
        UserLoginDetailDto userLoginDetailDto = new UserLoginDetailDto(userName, password);
        return userLoginDetailDto;
    }

    public UserLoginDetailDto populateAuthorities(List<String> authorities) {
        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String authority : authorities) {
            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(authority);
            grantedAuthorities.add(simpleGrantedAuthority);
        }
        this.authorities = grantedAuthorities;
        return this;
    }

    @JsonIgnore
    @Override
    public Collection<SimpleGrantedAuthority> getAuthorities() {
        return authorities;
    }


    public List<String> getPermissions() {
        return permissions;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }
}
