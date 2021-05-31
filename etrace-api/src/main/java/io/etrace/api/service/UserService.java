package io.etrace.api.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.etrace.api.consts.RoleType;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.ETraceUserPO;
import io.etrace.api.repository.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.etrace.api.config.SimpleWebSecurityConfig.MOCK_PASSWORD;

@Service
public class UserService implements UserDetailsService {
    public static final String VISITOR_PSNCODE = "MA000001";
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRoleService userRoleService;

    @Override
    public UserDetails loadUserByUsername(String username) {
        org.springframework.security.core.userdetails.User.UserBuilder builder;
        builder = org.springframework.security.core.userdetails.User.withUsername(username);
        //must set password
        builder.password(MOCK_PASSWORD);

        //load user roles info
        Set<String> roles = userRoleService.findRolesByUser(username);
        builder.roles(roles.toArray(new String[roles.size()]));

        return builder.build();
    }

    //public void saveUser(ETraceUser ETraceUser) {
    //    ETraceUser ETraceUserFromDB = userMapper.findByEmail(ETraceUser.getEmail());
    //    if (ETraceUserFromDB != null) {
    //        ETraceUser.setId(ETraceUserFromDB.getId());
    //        // todo: 这个 update 应该多余了
    //        updateUser(ETraceUser);
    //    } else {
    //        try {
    //            createUser(ETraceUser);
    //        } catch (DuplicateKeyException e) {
    //            // 如果用户已经存在，
    //            ETraceUser ETraceUserFromDB2 = userMapper.findByEmail(ETraceUser.getEmail());
    //            if (null != ETraceUserFromDB2) {
    //                LOGGER.info("find user in db。return ok");
    //                ETraceUser.setId(ETraceUserFromDB2.getId());
    //            } else {
    //                LOGGER.warn("not find user in db，maybe mysql synchronization is too slow");
    //            }
    //        }
    //    }
    //}

    public void createUser(ETraceUser ETraceUser) {
        userMapper.save(ETraceUser.toPO());
    }

    public void updateUser(ETraceUser ETraceUser) {
        userMapper.save(ETraceUser.toPO());
    }

    public List<ETraceUserPO> findByKeyword(String keyword) {
        List<ETraceUserPO> list = userMapper.findAllByEmailContainingOrUserNameContaining(keyword, keyword);
        return list;
    }

    /**
     * 完全的信息
     */
    @Cacheable(value = "user-email-cache", key = "#email")
    public ETraceUser findByUserEmail(String email) {
        return ETraceUser.toVO(userMapper.findByEmail(email));
    }

    public List<ETraceUser> findAllUser(String keyword, String userRole, int pageNum, int pageSize) {
        List<ETraceUser> ETraceUsers =
            userMapper.findAllByEmailContainingOrUserNameContaining(keyword, keyword,
                PageRequest.of(pageNum - 1, pageSize))
                .stream().map(ETraceUser::toVO).collect(Collectors.toList());
        List<ETraceUser> ETraceUserList = Lists.newLinkedList();
        if (CollectionUtils.isEmpty(ETraceUsers)) {
            return ETraceUserList;
        }
        for (ETraceUser ETraceUser : ETraceUsers) {
            Set<String> roles = userRoleService.findRolesByUserWithoutCache(ETraceUser.getEmail());
            if (userRole != null) {
                if (roles != null && roles.contains(userRole)) {
                    roles.add(RoleType.USER.name());
                    ETraceUser.setRoles(roles);
                    ETraceUserList.add(ETraceUser);
                }
            } else {
                if (roles != null) {
                    roles.add(RoleType.USER.name());
                } else {
                    roles = Sets.newHashSet(RoleType.USER.name());
                }
                ETraceUser.setRoles(roles);
                ETraceUserList.add(ETraceUser);
            }
        }
        return ETraceUserList;
    }
}

