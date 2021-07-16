package com.cm.pdfextractor.pdfextractor.repository;

import com.cm.pdfextractor.pdfextractor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = "SELECT * FROM user WHERE user.user_email=:#{#email}", nativeQuery = true)
    User findByEmail(@Param("email") String email);
}
