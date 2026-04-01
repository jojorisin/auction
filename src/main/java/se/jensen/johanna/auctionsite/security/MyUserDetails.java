package se.jensen.johanna.auctionsite.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import se.jensen.johanna.auctionsite.model.AppUser;
import se.jensen.johanna.auctionsite.model.enums.Role;

public class MyUserDetails implements UserDetails {

  private final AppUser appUser;

  public MyUserDetails(AppUser appUser) {
    this.appUser = appUser;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()));
  }

  @Override
  public String getPassword() {
    return appUser.getHashedPassword();
  }

  @Override
  public String getUsername() {
    return appUser.getEmail();
  }

  public Long getUserId() {
    return appUser.getId();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  public Role getRole() {
    return appUser.getRole();
  }
}
