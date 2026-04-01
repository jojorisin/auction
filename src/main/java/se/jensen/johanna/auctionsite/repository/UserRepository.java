package se.jensen.johanna.auctionsite.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.jensen.johanna.auctionsite.model.AppUser;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {

  Optional<AppUser> findUserByEmail(String email);

  boolean existsByEmail(String email);
}
