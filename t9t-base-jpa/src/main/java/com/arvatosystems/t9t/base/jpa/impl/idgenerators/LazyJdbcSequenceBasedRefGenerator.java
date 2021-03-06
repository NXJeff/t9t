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
package com.arvatosystems.t9t.base.jpa.impl.idgenerators;

import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.base.services.IRefGenerator;
import com.arvatosystems.t9t.cfg.be.DatabaseBrandType;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;

/**
 * Provides generators for technical Ids (database table primary keys). Standard JPA auto generated keys cannot be used for several reasons:
 * <ul>
 * <li>for geographical redundancy, we want a location specific offset on the key</li>
 * <li>we want run time type information (RTTI) to be baked into the generated value</li>
 * <li>in order not to loose too many valid values, caching should extend more than just the current request, therefore the key generation should span across
 * all sessions</li>
 * <li>some change of the computing strategy (sequence, table, etc) should be centralized to a single class,</li>
 * </ul>
 * It is enforced that this class is used for all entities requiring an auto generated keys.
 */
@Named("lazySequenceJDBC")  // only acquires an ID once the first request has been seen
@Singleton
public class LazyJdbcSequenceBasedRefGenerator implements IRefGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(LazyJdbcSequenceBasedRefGenerator.class);


    private static final int NUM_SEQUENCES = 100; // how many sequences we use to obtain the IDs
    private static final int NUM_SEQUENCES_UNSCALED = 10; // how many sequences we use to obtain unscaled IDs
    private final LazyJdbcSequenceBasedSingleRefGenerator[] generatorTab = new LazyJdbcSequenceBasedSingleRefGenerator[NUM_SEQUENCES];
    private final LazyJdbcSequenceBasedSingleRefGenerator[] generatorTab50xx = new LazyJdbcSequenceBasedSingleRefGenerator[NUM_SEQUENCES_UNSCALED];
    private final LazyJdbcSequenceBasedSingleRefGenerator[] generatorTab60xx = new LazyJdbcSequenceBasedSingleRefGenerator[NUM_SEQUENCES_UNSCALED];
    private final LazyJdbcSequenceBasedSingleRefGenerator[] generatorTab70xx = new LazyJdbcSequenceBasedSingleRefGenerator[NUM_SEQUENCES_UNSCALED];
    private final long scaledOffsetForLocation;

    // @Inject
    private final T9tServerConfiguration configuration = Jdp.getRequired(T9tServerConfiguration.class);

    public LazyJdbcSequenceBasedRefGenerator() {
        DatabaseBrandType dialect = configuration.databaseConfiguration.databaseBrand;
        LOGGER.info("Creating object references by SQL SEQUENCES via JDBC for database {}", dialect.name());

        scaledOffsetForLocation = (long) configuration.keyPrefetchConfiguration.locationOffset * LazyJpaSequenceBasedRefGenerator.OFFSET_BACKUP_LOCATION;
        for (int i = 0; i < NUM_SEQUENCES; ++i) {
            generatorTab[i] = new LazyJdbcSequenceBasedSingleRefGenerator(i, dialect, 500);
        }
        for (int i = 0; i < NUM_SEQUENCES_UNSCALED; ++i) {
            generatorTab50xx[i] = new LazyJdbcSequenceBasedSingleRefGenerator(5000 + i, dialect, 10);
            generatorTab60xx[i] = new LazyJdbcSequenceBasedSingleRefGenerator(6000 + i, dialect, 10);
            generatorTab70xx[i] = new LazyJdbcSequenceBasedSingleRefGenerator(7000 + i, dialect, 10);
        }
    }

    @Override
    public long generateRef(int rttiOffset) {
        if ((rttiOffset < 0) || (rttiOffset >= OFFSET_BACKUP_LOCATION)) {
            throw new InvalidParameterException("Bad rtti offset: " + rttiOffset);
        }
        return (generatorTab[rttiOffset % NUM_SEQUENCES].getnextId() * LazyJpaSequenceBasedRefGenerator.KEY_FACTOR) + scaledOffsetForLocation + rttiOffset;
    }

    @Override
    public long generateUnscaledRef(int rttiOffset) {
        LazyJdbcSequenceBasedSingleRefGenerator g = null;
        if ((rttiOffset >= 5000) && (rttiOffset < (5000 + NUM_SEQUENCES_UNSCALED))) {
            g = generatorTab50xx[rttiOffset - 5000];
        } else if ((rttiOffset >= 6000) && (rttiOffset < (6000 + NUM_SEQUENCES_UNSCALED))) {
            g = generatorTab60xx[rttiOffset - 6000];
        } else if ((rttiOffset >= 7000) && (rttiOffset < (7000 + NUM_SEQUENCES_UNSCALED))) {
            g = generatorTab70xx[rttiOffset - 7000];
        } else {
            throw new InvalidParameterException("Bad rtti offset: " + rttiOffset);
        }
        return (g.getnextId() * 2L) + (scaledOffsetForLocation > 0 ? 1 : 0);
    }
}
