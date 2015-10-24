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
package org.apache.sis.internal.referencing.provider;

import javax.xml.bind.annotation.XmlTransient;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.apache.sis.parameter.Parameters;
import org.apache.sis.referencing.datum.BursaWolfParameters;


/**
 * The provider for <cite>"Geocentric translations (geocentric domain)"</cite> (EPSG:1031).
 * This is a special case of {@link PositionVector7Param} where only the translation terms
 * can be set to a non-null value.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @since   0.7
 * @version 0.7
 * @module
 */
@XmlTransient
public final class GeocentricTranslation extends GeocentricAffine {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = -7160250630666911608L;

    /**
     * The group of all parameters expected by this coordinate operation.
     */
    public static final ParameterDescriptorGroup PARAMETERS;
    static {
        PARAMETERS = builder()
            .addIdentifier("1031")
            .addName("Geocentric translations (geocentric domain)")
            .addName("Geocentric Translations")     // Ambiguous alias (does not specify the domain)
            .createGroup(TX, TY, TZ);
    }

    /**
     * Constructs the provider.
     */
    public GeocentricTranslation() {
        super(2, PARAMETERS);
    }

    /**
     * Fills the given Bursa-Wolf parameters with the specified values.
     * Only the translation terms are extracted from the given parameter values.
     */
    @Override
    void fill(final BursaWolfParameters parameters, final Parameters values) {
        parameters.tX = values.doubleValue(TX);
        parameters.tY = values.doubleValue(TY);
        parameters.tZ = values.doubleValue(TZ);
    }
}
