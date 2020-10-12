package io.etrace.api.repository;

import io.etrace.api.model.po.user.UserAction;
import org.springframework.data.repository.CrudRepository;

public interface UserActionMapper extends CrudRepository<UserAction, Long> {

    UserAction findByUserEmail(String email);
}
