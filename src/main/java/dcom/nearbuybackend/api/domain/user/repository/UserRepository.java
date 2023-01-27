package dcom.nearbuybackend.api.domain.user.repository;

import dcom.nearbuybackend.api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
}
