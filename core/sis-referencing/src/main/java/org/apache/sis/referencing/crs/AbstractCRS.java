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
package org.apache.sis.referencing.crs;

import java.util.Map;
import javax.measure.unit.Unit;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.apache.sis.internal.referencing.ReferencingUtilities;
import org.apache.sis.referencing.AbstractReferenceSystem;
import org.apache.sis.referencing.cs.AbstractCS;
import org.apache.sis.util.ComparisonMode;
import org.apache.sis.io.wkt.Formatter;

import static org.apache.sis.util.Utilities.deepEquals;
import static org.apache.sis.util.ArgumentChecks.ensureNonNull;


/**
 * Abstract coordinate reference system, usually defined by a coordinate system and a datum.
 * {@code AbstractCRS} can have an arbitrary number of dimensions. The actual dimension of a
 * given instance can be determined as below:
 *
 * {@preformat java
 *   int dimension = crs.getCoordinateSystem().getDimension();
 * }
 *
 * However most subclasses restrict the allowed number of dimensions.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @since   0.4 (derived from geotk-1.2)
 * @version 0.4
 * @module
 *
 * @see AbstractCS
 * @see org.apache.sis.referencing.datum.AbstractDatum
 */
@XmlType(name="AbstractCRSType")
@XmlRootElement(name = "AbstractCRS")
public class AbstractCRS extends AbstractReferenceSystem implements CoordinateReferenceSystem {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = -7433284548909530047L;

    /**
     * The coordinate system.
     *
     * <p><b>Consider this field as final!</b>
     * This field is modified only at unmarshalling time by {@link #setCoordinateSystem(CoordinateSystem)}</p>
     */
    private CoordinateSystem coordinateSystem;

    /**
     * Constructs a new object in which every attributes are set to a null value.
     * <strong>This is not a valid object.</strong> This constructor is strictly
     * reserved to JAXB, which will assign values to the fields using reflexion.
     */
    AbstractCRS() {
        super(org.apache.sis.internal.referencing.NilReferencingObject.INSTANCE);
    }

    /**
     * Creates a coordinate reference system from the given properties and coordinate system.
     * The properties given in argument follow the same rules than for the
     * {@linkplain AbstractReferenceSystem#AbstractReferenceSystem(Map) super-class constructor}.
     * The following table is a reminder of main (not all) properties:
     *
     * <table class="sis">
     *   <tr>
     *     <th>Property name</th>
     *     <th>Value type</th>
     *     <th>Returned by</th>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#NAME_KEY}</td>
     *     <td>{@link org.opengis.referencing.ReferenceIdentifier} or {@link String}</td>
     *     <td>{@link #getName()}</td>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#ALIAS_KEY}</td>
     *     <td>{@link org.opengis.util.GenericName} or {@link CharSequence} (optionally as array)</td>
     *     <td>{@link #getAlias()}</td>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#IDENTIFIERS_KEY}</td>
     *     <td>{@link org.opengis.referencing.ReferenceIdentifier} (optionally as array)</td>
     *     <td>{@link #getIdentifiers()}</td>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.IdentifiedObject#REMARKS_KEY}</td>
     *     <td>{@link org.opengis.util.InternationalString} or {@link String}</td>
     *     <td>{@link #getRemarks()}</td>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.datum.Datum#DOMAIN_OF_VALIDITY_KEY}</td>
     *     <td>{@link org.opengis.metadata.extent.Extent}</td>
     *     <td>{@link #getDomainOfValidity()}</td>
     *   </tr>
     *   <tr>
     *     <td>{@value org.opengis.referencing.datum.Datum#SCOPE_KEY}</td>
     *     <td>{@link InternationalString} or {@link String}</td>
     *     <td>{@link #getScope()}</td>
     *   </tr>
     * </table>
     *
     * @param properties The properties to be given to the reference system.
     * @param cs The coordinate system.
     */
    public AbstractCRS(final Map<String,?> properties, final CoordinateSystem cs) {
        super(properties);
        ensureNonNull("cs", cs);
        coordinateSystem = cs;
    }

    /**
     * Constructs a new coordinate reference system with the same values than the specified one.
     * This copy constructor provides a way to convert an arbitrary implementation into a SIS one
     * or a user-defined one (as a subclass), usually in order to leverage some implementation-specific API.
     *
     * <p>This constructor performs a shallow copy, i.e. the properties are not cloned.</p>
     *
     * @param crs The coordinate reference system to copy.
     */
    protected AbstractCRS(final CoordinateReferenceSystem crs) {
        super(crs);
        coordinateSystem = crs.getCoordinateSystem();
    }

    /**
     * Returns the GeoAPI interface implemented by this class.
     * The default implementation returns {@code CoordinateReferenceSystem.class}.
     * Subclasses implementing a more specific GeoAPI interface shall override this method.
     *
     * @return The coordinate reference system interface implemented by this class.
     */
    @Override
    public Class<? extends CoordinateReferenceSystem> getInterface() {
        return CoordinateReferenceSystem.class;
    }

    /**
     * Returns the coordinate system.
     *
     * @return The coordinate system.
     */
    @Override
    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Sets the coordinate system to the given value. This method is invoked only by JAXB at
     * unmarshalling time and can be invoked only if the coordinate system has never been set.
     *
     * @param  name The property name, used only in case of error message to format.
     * @throws IllegalStateException If the coordinate system has already been set.
     */
    final void setCoordinateSystem(final String name, final CoordinateSystem cs) {
        if (cs != null && ReferencingUtilities.canSetProperty(name, coordinateSystem != null)) {
            coordinateSystem = cs;
        }
    }

    /**
     * Returns the unit used for all axis, or {@code null} if not all axis uses the same unit.
     * This method is often used for formatting according  Well Know Text (WKT) version 1.
     */
    final Unit<?> getUnit() {
        return ReferencingUtilities.getUnit(coordinateSystem);
    }

    /**
     * Compares this coordinate reference system with the specified object for equality.
     * If the {@code mode} argument value is {@link ComparisonMode#STRICT STRICT} or
     * {@link ComparisonMode#BY_CONTRACT BY_CONTRACT}, then all available properties are
     * compared including the {@linkplain #getDomainOfValidity() domain of validity} and
     * the {@linkplain #getScope() scope}.
     *
     * @param  object The object to compare to {@code this}.
     * @param  mode {@link ComparisonMode#STRICT STRICT} for performing a strict comparison, or
     *         {@link ComparisonMode#IGNORE_METADATA IGNORE_METADATA} for comparing only properties
     *         relevant to coordinate transformations.
     * @return {@code true} if both objects are equal.
     */
    @Override
    public boolean equals(final Object object, final ComparisonMode mode) {
        if (super.equals(object, mode)) {
            switch (mode) {
                case STRICT: {
                    return coordinateSystem.equals(((AbstractCRS) object).coordinateSystem);
                }
                default: {
                    final CoordinateReferenceSystem that = (CoordinateReferenceSystem) object;
                    return deepEquals(getCoordinateSystem(), that.getCoordinateSystem(), mode);
                }
            }
        }
        return false;
    }

    /**
     * Invoked by {@link #hashCode()} for computing the hash code when first needed.
     * See {@link org.apache.sis.referencing.AbstractIdentifiedObject#computeHashCode()}
     * for more information.
     *
     * @return The hash code value. This value may change in any future Apache SIS version.
     */
    @Override
    protected long computeHashCode() {
        return super.computeHashCode() + 31*coordinateSystem.hashCode();
    }

    /**
     * Formats the inner part of a <cite>Well Known Text</cite> (WKT)</a> element.
     * The default implementation writes the following elements:
     *
     * <ul>
     *   <li>The {@linkplain AbstractSingleCRS#getDatum() datum}, if any.</li>
     *   <li>The unit if all axes use the same unit. Otherwise the unit is omitted and the WKT format
     *       is {@linkplain Formatter#setInvalidWKT(IdentifiedObject) flagged as invalid}.</li>
     *   <li>All {@linkplain #getCoordinateSystem() coordinate system}'s axis.</li>
     * </ul>
     *
     * @param  formatter The formatter to use.
     * @return The name of the WKT element type (e.g. {@code "GEOGCS"}).
     */
    @Override
    protected String formatTo(final Formatter formatter) {
        formatDefaultWKT(formatter);
        // Will declares the WKT as invalid.
        return super.formatTo(formatter);
    }

    /**
     * Default implementation of {@link #formatTo(Formatter)}.
     * For {@link DefaultEngineeringCRS} and {@link DefaultVerticalCRS} use only.
     */
    void formatDefaultWKT(final Formatter formatter) {
        final Unit<?> unit = getUnit();
        formatter.append(unit);
        final int dimension = coordinateSystem.getDimension();
        for (int i=0; i<dimension; i++) {
            formatter.append(coordinateSystem.getAxis(i));
        }
        if (unit == null) {
            formatter.setInvalidWKT(coordinateSystem);
        }
    }
}