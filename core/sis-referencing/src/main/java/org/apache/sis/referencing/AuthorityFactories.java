/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.referencing;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.sql.SQLTransientException;
import org.opengis.util.FactoryException;
import org.opengis.referencing.AuthorityFactory;
import org.opengis.referencing.cs.CSAuthorityFactory;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.datum.DatumAuthorityFactory;
import org.opengis.referencing.operation.CoordinateOperationAuthorityFactory;
import org.apache.sis.internal.util.LazySet;
import org.apache.sis.internal.system.Loggers;
import org.apache.sis.internal.system.Modules;
import org.apache.sis.internal.system.SystemListener;
import org.apache.sis.referencing.factory.MultiAuthoritiesFactory;
import org.apache.sis.referencing.factory.UnavailableFactoryException;
import org.apache.sis.referencing.factory.sql.EPSGFactory;
import org.apache.sis.util.logging.Logging;


/**
 * Provides the CRS, CS, datum and coordinate operation authority factories.
 * Provides also the system-wide {@link MultiAuthoritiesFactory} instance used by {@link CRS#forCode(String)}.
 * Current version handles the EPSG factory in a special way, but we may try to avoid doing special cases in a
 * future SIS version (this may require more help from {@link ServiceLoader}).
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.7
 * @version 0.7
 * @module
 */
final class AuthorityFactories<T extends AuthorityFactory> extends LazySet<T> {
    /**
     * An array containing only the EPSG factory. Content of this array is initially null.
     * The EPSG factory will be created when first needed by {@link #initialValues()}.
     */
    private static final AuthorityFactory[] EPSG = new AuthorityFactory[1];

    /**
     * The unique system-wide authority factory instance that contains all factories found on the classpath,
     * plus the EPSG factory.
     */
    static final MultiAuthoritiesFactory ALL = new MultiAuthoritiesFactory(
            new AuthorityFactories<>(CRSAuthorityFactory.class),
            new AuthorityFactories<>(CSAuthorityFactory.class),
            new AuthorityFactories<>(DatumAuthorityFactory.class),
            new AuthorityFactories<>(CoordinateOperationAuthorityFactory.class))
    {
        /** Anonymous constructor */ {
            setLenient(true);
        }

        @Override
        public void reload() {
            EPSG(null);
            super.reload();
        }
    };

    /**
     * Registers a hook for forcing {@code ALL} to reload all CRS, CS, datum and coordinate operation factories
     * when the classpath changed.
     */
    static {
        SystemListener.add(new SystemListener(Modules.REFERENCING) {
            @Override protected void classpathChanged() {ALL.reload();}
        });
    }

    /**
     * Creates a new provider for factories of the given type.
     */
    private AuthorityFactories(final Class<T> type) {
        super(ServiceLoader.load(type));
    }

    /**
     * Sets the EPSG factory to the given value.
     */
    static void EPSG(final AuthorityFactory factory) {
        synchronized (EPSG) {
            EPSG[0] = factory;
        }
    }

    /**
     * Returns the EPSG factory.
     */
    static AuthorityFactory EPSG() {
        synchronized (EPSG) {
            AuthorityFactory factory = EPSG[0];
            if (factory == null) try {
                factory = new EPSGFactory(null);
            } catch (FactoryException e) {
                log(Level.CONFIG, e);
                factory = EPSGFactoryFallback.INSTANCE;
            }
            EPSG[0] = factory;
            return factory;
        }
    }

    /**
     * Returns the fallback to use if the authority factory is not available. Unless the problem may be temporary,
     * this method replaces the {@link EPSGFactory} instance by {@link EPSGFactoryFallback} in order to prevent
     * the same exception to be thrown and logged on every calls to {@link CRS#forCode(String)}.
     */
    static CRSAuthorityFactory fallback(final UnavailableFactoryException e) throws UnavailableFactoryException {
        final boolean isTransient = (e.getCause() instanceof SQLTransientException);
        final AuthorityFactory unavailable = e.getUnavailableFactory();
        final CRSAuthorityFactory factory;
        synchronized (EPSG) {
            if (unavailable != EPSG[0]) {
                throw e;                                // Exception did not come from a factory that we control.
            }
            factory = EPSGFactoryFallback.INSTANCE;
            if (!isTransient) {
                ALL.reload();
                EPSG[0] = factory;
            }
        }
        log(Level.WARNING, e);
        return factory;
    }

    /**
     * Logs the given exception at the given level. This method pretends that the logging come from
     * {@link CRS#getAuthorityFactory(String)}, which is the public facade for {@link #EPSG()}.
     */
    private static void log(final Level level, final Exception e) {
        final LogRecord record = new LogRecord(level, e.getLocalizedMessage());
        record.setLoggerName(Loggers.CRS_FACTORY);
        Logging.log(CRS.class, "getAuthorityFactory", record);
    }

    /**
     * Invoked by {@link LazySet} for adding the EPSG factory before any other factory fetched by {@code ServiceLoader}.
     * We put the EPSG factory first because it is often used anyway even for {@code CRS} and {@code AUTO} namespaces.
     *
     * <p>This method tries to instantiate an {@link EPSGFactory} if possible,
     * or an {@link EPSGFactoryFallback} otherwise.</p>
     */
    @Override
    @SuppressWarnings("unchecked")
    protected T[] initialValues() {
        EPSG();                         // Force EPSGFactory instantiation if not already done.
        return (T[]) EPSG;
    }
}
