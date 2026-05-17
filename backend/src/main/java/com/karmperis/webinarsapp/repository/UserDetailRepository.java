package com.karmperis.webinarsapp.repository;

import com.karmperis.webinarsapp.model.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link UserDetail} entities.
 * Management of details is primarily handled via cascade from the associated User.
 */
public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
}