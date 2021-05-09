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

package io.etrace.api.model.po.user;

import io.etrace.api.consts.RoleType;
import io.etrace.api.model.po.BaseObject;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import javax.persistence.Column;
import javax.persistence.Transient;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ETraceUser extends BaseObject implements Cloneable, UserDetails {

    /**
     * 可重复（不过应尽量避免）
     */
    private String userName;
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Transient
    private Set<String> roles;

    @Transient
    private UserConfigPO userConfig;
    /**
     * 默认是非api访问
     */
    @Transient
    private Boolean isApiUser = Boolean.FALSE;

    public static ETraceUser toVO(ETraceUserPO po) {
        ETraceUser vo = new ETraceUser();
        BeanUtils.copyProperties(po, vo);
        return vo;
    }

    public ETraceUserPO toPO() {
        ETraceUserPO po = new ETraceUserPO();
        BeanUtils.copyProperties(this, po);
        return po;
    }

    @Override
    public ETraceUser clone() {
        ETraceUser clone = new ETraceUser();
        clone.setId(getId());
        clone.setUpdatedAt(getUpdatedAt());
        clone.setCreatedAt(getCreatedAt());
        clone.email = email;
        clone.roles = roles;
        clone.userConfig = userConfig;
        return clone;
    }

    @Transient
    public boolean isAdmin() {
        return !CollectionUtils.isEmpty(roles) && roles.contains(RoleType.ADMIN.name());
    }

    @Transient
    public boolean isAnonymousUser() {
        return roles.size() == 0 || (roles.size() == 1 && roles.contains(RoleType.VISITOR.name()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
