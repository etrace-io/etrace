package io.etrace.api.repository;

import io.etrace.api.model.po.user.UserRole;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoleMapper extends PagingAndSortingRepository<UserRole, Long> {

    Optional<UserRole> findByUserId(Long userId);

    List<UserRole> findAllByUserEmailContaining(String email, Pageable page);

    int countByUserEmailContaining(String email);
}
