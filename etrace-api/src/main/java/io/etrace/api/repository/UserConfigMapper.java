package io.etrace.api.repository;

import io.etrace.api.model.po.user.UserConfig;
import org.springframework.data.repository.CrudRepository;

public interface UserConfigMapper extends CrudRepository<UserConfig, Long> {

    UserConfig findByUserEmail(String email);
}
