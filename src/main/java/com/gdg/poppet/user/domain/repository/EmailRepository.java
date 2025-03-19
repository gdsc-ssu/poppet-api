package com.gdg.poppet.user.domain.repository;

import com.gdg.poppet.user.domain.model.Email;
import com.gdg.poppet.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {

    @Query("SELECT e " +
            "FROM Email e " +
            "WHERE e.user = :user " +
            "ORDER BY e.createdAt")
    List<Email> findByUser(@Param(value = "user") User user);
}
