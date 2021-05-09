/*
 * Copyright 2020 etrace.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.etrace.api.service;

import com.google.common.collect.Sets;
import io.etrace.api.model.po.user.ETraceUser;
import io.etrace.api.model.po.user.ETraceUserPO;
import io.etrace.api.model.po.user.UserRole;
import io.etrace.api.repository.UserMapper;
import io.etrace.api.repository.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    UserMapper userMapper;
    @Autowired
    UserRoleMapper userRoleMapper;

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        Iterable<ETraceUserPO> all = userMapper.findAll();
        /*
        这里之后改成从DB取密码信息
        取的password 会自动和用户的输入进行比较，进而进行 authentication
         */
        Optional<ETraceUserPO> op = userMapper.findByUserName(userEmail);
        if (op.isPresent()) {
            ETraceUser user = ETraceUser.toVO(op.get());
            Optional<UserRole> op2 = userRoleMapper.findByUserId(user.getId());
            if (op2.isPresent()) {
                user.setRoles(Sets.newHashSet(op2.get().getRoles()));
            } else {
                user.setRoles(Sets.newHashSet("ROLE_USER"));
            }

            return user;
        } else {
            throw new UsernameNotFoundException(userEmail + " not found");
        }
    }
}
