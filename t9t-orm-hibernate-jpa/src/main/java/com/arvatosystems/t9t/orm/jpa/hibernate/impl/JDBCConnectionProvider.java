/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.orm.jpa.hibernate.impl;

import java.sql.Connection;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;

import com.arvatosystems.t9t.base.jpa.ormspecific.IJpaJdbcConnectionProvider;

import de.jpaw.dp.Singleton;

@Singleton
public class JDBCConnectionProvider implements IJpaJdbcConnectionProvider {

    @Override
    public Connection get(EntityManager em) {
        return ((SessionImpl)em.unwrap(Session.class)).connection();
    }

}
