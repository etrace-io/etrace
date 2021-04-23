package io.etrace.api.repository;

import io.etrace.api.model.po.user.UserConfigPO;
import org.springframework.data.repository.CrudRepository;

public interface UserConfigMapper extends CrudRepository<UserConfigPO, Long> {

    UserConfigPO findByUserEmail(String email);
}
