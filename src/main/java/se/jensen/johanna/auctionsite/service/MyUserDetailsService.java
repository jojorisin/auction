package se.jensen.johanna.auctionsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import se.jensen.johanna.auctionsite.repository.UserRepository;
import se.jensen.johanna.auctionsite.security.MyUserDetails;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findUserByEmail(email)
                             .map(MyUserDetails::new)
                             .orElseThrow(() -> new UsernameNotFoundException("User with email %s not found".formatted(
                                     email)));
    }

}
