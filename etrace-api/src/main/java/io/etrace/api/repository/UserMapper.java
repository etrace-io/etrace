package io.etrace.api.repository;

import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.ETraceUserPO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

public interface UserMapper extends PagingAndSortingRepository<ETraceUserPO, Long> {

    ETraceUserPO findByUserName(String username);

    List<ETraceUserPO> findAllByEmailContainingOrUserNameContaining(String keyword, String k2);

    ETraceUserPO findByEmail(String email);

    List<ETraceUserPO> findAllByEmailContainingOrUserNameContaining(String keyword, String k2, Pageable page);
}
