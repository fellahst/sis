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
package org.apache.sis.metadata.iso.constraint;

import java.util.Collection;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.util.InternationalString;
import org.opengis.metadata.constraint.Restriction;
import org.opengis.metadata.constraint.LegalConstraints;


/**
 * Restrictions and legal prerequisites for accessing and using the resource.
 * The {@linkplain #getOtherConstraints() other constraint} element shall be non-empty only if
 * {@linkplain #getAccessConstraints() access constraints} and/or {@linkplain #getUseConstraints()
 * use constraints} elements have a value of {@link Restriction#OTHER_RESTRICTIONS}.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @author  Touraïvane (IRD)
 * @author  Cédric Briançon (Geomatys)
 * @since   0.3 (derived from geotk-2.1)
 * @version 0.3
 * @module
 */
@XmlType(name = "MD_LegalConstraints_Type", propOrder = {
    "accessConstraints",
    "useConstraints",
    "otherConstraints"
})
@XmlRootElement(name = "MD_LegalConstraints")
public class DefaultLegalConstraints extends DefaultConstraints implements LegalConstraints {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = -228007779747439839L;

    /**
     * Access constraints applied to assure the protection of privacy or intellectual property,
     * and any special restrictions or limitations on obtaining the resource.
     */
    private Collection<Restriction> accessConstraints;

    /**
     * Constraints applied to assure the protection of privacy or intellectual property,
     * and any special restrictions or limitations or warnings on using the resource.
     */
    private Collection<Restriction> useConstraints;

    /**
     * Other restrictions and legal prerequisites for accessing and using the resource.
     * Should be a non-empty value only if {@linkplain #getAccessConstraints() access constraints}
     * or {@linkplain #getUseConstraints() use constraints} declares
     * {@linkplain Restriction#OTHER_RESTRICTIONS other restrictions}.
     */
    private Collection<InternationalString> otherConstraints;

    /**
     * Constructs an initially empty constraints.
     */
    public DefaultLegalConstraints() {
    }

    /**
     * Constructs a new instance initialized with the values from the specified metadata object.
     * This is a <cite>shallow</cite> copy constructor, since the other metadata contained in the
     * given object are not recursively copied.
     *
     * @param object The metadata to copy values from.
     *
     * @see #castOrCopy(LegalConstraints)
     */
    public DefaultLegalConstraints(final LegalConstraints object) {
        super(object);
        accessConstraints = copyCollection(object.getAccessConstraints(), Restriction.class);
        useConstraints    = copyCollection(object.getUseConstraints(), Restriction.class);
        otherConstraints  = copyCollection(object.getOtherConstraints(), InternationalString.class);
    }

    /**
     * Returns a SIS metadata implementation with the values of the given arbitrary implementation.
     * This method performs the first applicable actions in the following choices:
     *
     * <ul>
     *   <li>If the given object is {@code null}, then this method returns {@code null}.</li>
     *   <li>Otherwise if the given object is already an instance of
     *       {@code DefaultLegalConstraints}, then it is returned unchanged.</li>
     *   <li>Otherwise a new {@code DefaultLegalConstraints} instance is created using the
     *       {@linkplain #DefaultLegalConstraints(LegalConstraints) copy constructor}
     *       and returned. Note that this is a <cite>shallow</cite> copy operation, since the other
     *       metadata contained in the given object are not recursively copied.</li>
     * </ul>
     *
     * @param  object The object to get as a SIS implementation, or {@code null} if none.
     * @return A SIS implementation containing the values of the given object (may be the
     *         given object itself), or {@code null} if the argument was null.
     */
    public static DefaultLegalConstraints castOrCopy(final LegalConstraints object) {
        if (object == null || object instanceof DefaultLegalConstraints) {
            return (DefaultLegalConstraints) object;
        }
        return new DefaultLegalConstraints(object);
    }

    /**
     * Returns the access constraints applied to assure the protection of privacy or intellectual property,
     * and any special restrictions or limitations on obtaining the resource.
     */
    @Override
    @XmlElement(name = "accessConstraints")
    public synchronized Collection<Restriction> getAccessConstraints() {
        return accessConstraints = nonNullCollection(accessConstraints, Restriction.class);
    }

    /**
     * Sets the access constraints applied to assure the protection of privacy or intellectual property,
     * and any special restrictions or limitations on obtaining the resource.
     *
     * @param newValues The new access constraints.
     */
    public synchronized void setAccessConstraints(final Collection<? extends Restriction> newValues) {
        accessConstraints = writeCollection(newValues, accessConstraints, Restriction.class);
    }

    /**
     * Returns the constraints applied to assure the protection of privacy or intellectual property,
     * and any special restrictions or limitations or warnings on using the resource.
     */
    @Override
    @XmlElement(name = "useConstraints")
    public synchronized Collection<Restriction> getUseConstraints() {
        return useConstraints = nonNullCollection(useConstraints, Restriction.class);
    }

    /**
     * Sets the constraints applied to assure the protection of privacy or intellectual property,
     * and any special restrictions or limitations or warnings on using the resource.
     *
     * @param newValues The new use constraints.
     */
    public synchronized void setUseConstraints(final Collection<? extends Restriction> newValues) {
        useConstraints = writeCollection(newValues, useConstraints, Restriction.class);
    }

    /**
     * Returns the other restrictions and legal prerequisites for accessing and using the resource.
     * Should be a non-empty value only if {@linkplain #getAccessConstraints() access constraints}
     * or {@linkplain #getUseConstraints() use constraints} declares
     * {@linkplain Restriction#OTHER_RESTRICTIONS other restrictions}.
     */
    @Override
    @XmlElement(name = "otherConstraints")
    public synchronized Collection<InternationalString> getOtherConstraints() {
        return otherConstraints = nonNullCollection(otherConstraints, InternationalString.class);
    }

    /**
     * Sets the other restrictions and legal prerequisites for accessing and using the resource.
     *
     * @param newValues Other constraints.
     */
    public synchronized void setOtherConstraints(final Collection<? extends InternationalString> newValues) {
        otherConstraints = writeCollection(newValues, otherConstraints, InternationalString.class);
    }
}
