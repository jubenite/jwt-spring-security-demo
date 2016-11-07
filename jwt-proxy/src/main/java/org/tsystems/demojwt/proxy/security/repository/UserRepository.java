package org.tsystems.demojwt.proxy.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tsystems.demojwt.proxy.model.security.User;

/**
 * Created by t-systems
 */
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
