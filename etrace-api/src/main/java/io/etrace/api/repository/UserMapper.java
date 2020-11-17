package io.etrace.api.repository;

import io.etrace.api.model.po.user.ETraceUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface UserMapper extends PagingAndSortingRepository<ETraceUser, Long> {

    Optional<ETraceUser> findByUserName(String username);

    List<ETraceUser> findAllByEmailContainingOrUserNameContaining(String keyword, String k2);

    ETraceUser findByEmail(String email);

    List<ETraceUser> findAllByEmailContainingOrUserNameContaining(String keyword, String k2, Pageable page);
}
