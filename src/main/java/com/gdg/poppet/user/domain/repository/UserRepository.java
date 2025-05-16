package com.gdg.poppet.user.domain.repository;

import com.gdg.poppet.user.domain.enums.Provider;
import com.gdg.poppet.user.domain.model.User;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u " +
            "FROM User u " +
            "WHERE u.userId = :userId ")
    Optional<User> findByUserId(@Param(value = "userId") String userId);

    @Query("SELECT u " +
            "FROM User u " +
            "WHERE u.userId = :userId " +
            "  AND u.provider = :provider")
    Optional<User> findByUserIdAndProvider(@Param("userId") String userId, @Param("provider") Provider provider);

    @Query("SELECT u " +
            "FROM User u " +
            "JOIN FETCH u.emails el ")
    List<User> findAll();
}
