package io.etrace.api.service;

import com.google.common.cache.Cache;
import io.etrace.api.consts.RoleType;
import io.etrace.api.model.po.user.UserRole;
import io.etrace.api.model.vo.SearchResult;
import io.etrace.api.repository.UserRoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.google.common.cache.CacheBuilder.newBuilder;
import static com.google.common.collect.Sets.newHashSet;
import static io.etrace.api.service.UserService.VISITOR_PSNCODE;

@Service
public class UserRoleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRoleService.class);

    //user code=>roles
    private final Cache<String, Set<String>> cache;

    @Autowired
    private UserRoleMapper userRoleMapper;

    public UserRoleService() {
        cache = newBuilder().maximumSize(500)
            .expireAfterWrite(5, TimeUnit.MINUTES).build();
    }

    public UserRole create(UserRole userRole) {
        return userRoleMapper.save(userRole);
    }

    public UserRole update(UserRole userRole) {
        return userRoleMapper.save(userRole);
    }

    public Iterable<UserRole> findAllRoles() {
        return userRoleMapper.findAll();
    }

    public Set<String> findRolesByUser(String user) {
        Set<String> roles = cache.getIfPresent(user);
        if (roles != null) {
            return roles;
        }
        UserRole userRole = findRoleByUser(user);
        //set user default role
        if (VISITOR_PSNCODE.equalsIgnoreCase(user)) {
            roles = newHashSet(RoleType.VISITOR.name());
        } else {
            roles = newHashSet(RoleType.USER.name());
            if (userRole != null && userRole.getRoles() != null) {
                roles.addAll(userRole.getRoles());
            }
        }

        cache.put(user, roles);
        return roles;
    }

    protected Set<String> findRolesByUserWithoutCache(String user) {
        UserRole userRole = findRoleByUser(user);
        Set<String> roles = newHashSet(RoleType.USER.name());
        if (userRole != null && userRole.getRoles() != null) {
            roles.addAll(userRole.getRoles());
        }
        return roles;
    }

    //todo :  这里代码是模糊搜索，业务意义是什么
    @Deprecated
    protected UserRole findRoleByUser(String user) {
        List<UserRole> roles = userRoleMapper.findAllByUserEmailContaining(user, Pageable.unpaged());
        if (roles != null && !roles.isEmpty()) {
            return roles.get(0);
        }
        return null;
    }

    public SearchResult<UserRole> findRoleByParam(String userEmail, int pageNum, int pageSize) {
        SearchResult<UserRole> searchResult = new SearchResult<>();
        long count;
        if (userEmail == null) {
            count = userRoleMapper.count();
        } else {
            count = userRoleMapper.countByUserEmailContaining(userEmail);
        }
        searchResult.setTotal(count);
        if (count > 0) {
            PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
            List<UserRole> roles = userRoleMapper.findAllByUserEmailContaining(userEmail, pageRequest);
            searchResult.setResults(roles);
        }
        return searchResult;
    }

}
