package com.arvatosystems.t9t.auth.jpa.mapping.impl

import com.arvatosystems.t9t.annotations.jpa.AutoMap42
import com.arvatosystems.t9t.annotations.jpa.NeedMapping
import com.arvatosystems.t9t.auth.UserDTO
import com.arvatosystems.t9t.auth.UserDescription
import com.arvatosystems.t9t.auth.jpa.entities.UserEntity
import com.arvatosystems.t9t.auth.jpa.mapping.IRoleDTOMapper
import com.arvatosystems.t9t.auth.jpa.persistence.IRoleEntityResolver
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver

@AutoMap42
public class UserMappers {
    IUserEntityResolver userResolver
    IRoleEntityResolver roleResolver
    IRoleDTOMapper roleMapper

//    @AutoHandler("com.arvatosystems.t9t.core.smutr.request.CARS")  // NPE
//    @AutoHandler("CAS")
//    @AutoHandler("S42")   uses the sapi BE search
    @NeedMapping  // required because the DTO is final
    def void d2eUserDTO(UserEntity entity, UserDTO dto, boolean onlyActive) {
        entity.roleRef = roleResolver.getRef(dto.roleRef, onlyActive)
    }

    @NeedMapping  // required because the DTO is final
    def void e2dUserDTO(UserEntity entity, UserDTO dto) {
        dto.roleRef = roleMapper.mapToDto(entity.roleRef)
    }

    def void e2dUserDescription(UserEntity entity, UserDescription dto) {
    }
}