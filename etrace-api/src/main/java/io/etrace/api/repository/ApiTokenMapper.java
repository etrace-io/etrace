package io.etrace.api.repository;

import io.etrace.api.model.po.ui.ApiToken;
import org.springframework.data.repository.CrudRepository;

public interface ApiTokenMapper extends CrudRepository<ApiToken, Long> {

    ApiToken findByCid(String cid);

    ApiToken findByUserEmail(String email);
}
