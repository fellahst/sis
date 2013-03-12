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
package org.apache.sis.internal.converter;

import java.util.Set;
import java.util.EnumSet;
import net.jcip.annotations.Immutable;
import org.apache.sis.util.Numbers;
import org.apache.sis.util.ObjectConverter;
import org.apache.sis.math.FunctionProperty;


/**
 * Handles conversions from {@link java.lang.Number} to other numbers.
 * This class supports only the type supported by {@link Numbers}.
 *
 * {@section Performance note}
 * We provide a single class for all supported kinds of {@code Number} and delegate the actual
 * work to the {@code Numbers} static methods. This is not a very efficient way to do the work.
 * For example it may be more efficient to provide specialized subclasses for each target class,
 * so we don't have to execute the {@code switch} inside the {@code Numbers} class every time a
 * value is converted. However performance is not the primary concern here, since those converters
 * will typically be used by code doing more costly work (e.g. the {@code sis-metadata} module
 * providing {@code Map} views using Java reflection). So we rather try to be more compact.
 * If nevertheless performance appear to be a problem, consider reverting to revision 1455255,
 * which was using one subclass per target type as described above.
 *
 * @param <S> The source number type.
 * @param <T> The target number type.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-2.4)
 * @version 0.3
 * @module
 */
@Immutable
final class NumberConverter<S extends Number, T extends Number> extends SystemConverter<S,T> {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = -8715054480508622025L;

    /**
     * Creates a new converter for the given source and target classes.
     * This constructor does not verify the validity of parameter values.
     * It is caller's responsibility to ensure that the given class are
     * supported by the {@link Numbers} methods.
     */
    NumberConverter(final Class<S> sourceClass, final Class<T> targetClass) {
        super(sourceClass, targetClass);
    }

    /**
     * Declares this converter as a injective or surjective function,
     * depending on whether conversions loose information or not.
     */
    @Override
    public Set<FunctionProperty> properties() {
        return EnumSet.of(Numbers.widestClass(sourceClass, targetClass) == targetClass
                              ? FunctionProperty.INJECTIVE : FunctionProperty.SURJECTIVE,
                          FunctionProperty.ORDER_PRESERVING, FunctionProperty.INVERTIBLE);
    }

    /**
     * Converts the given number to the target type if that type is different.
     */
    @Override
    public T convert(final S source) {
        return Numbers.cast(source, targetClass);
    }

    /**
     * Returns the inverse of this converter.
     */
    @Override
    public ObjectConverter<T,S> inverse() {
        return new NumberConverter<>(targetClass, sourceClass).unique();
    }

    /**
     * Converter from numbers to comparables. This special case exists because {@link Number}
     * does not implement {@link java.lang.Comparable} directly, but all known subclasses do.
     */
    @Immutable
    static final class Comparable<S extends Number> extends SystemConverter<S, java.lang.Comparable<?>> {
        /**
         * For cross-version compatibility.
         */
        private static final long serialVersionUID = 3716134638218072176L;

        /**
         * Creates a new converter from the given type of numbers to {@code Comparable} instances.
         */
        @SuppressWarnings({"rawtypes","unchecked"})
        Comparable(final Class<S> sourceClass) {
            super(sourceClass, (Class) java.lang.Comparable.class);
        }

        /**
         * If the source class implements {@code Comparable}, then this converter is bijective.
         * Otherwise there is no known property for this converter.
         */
        @Override
        public Set<FunctionProperty> properties() {
            if (Comparable.class.isAssignableFrom(sourceClass)) {
                return EnumSet.of(FunctionProperty.INJECTIVE, FunctionProperty.SURJECTIVE,
                            FunctionProperty.ORDER_PRESERVING);
            }
            return EnumSet.noneOf(FunctionProperty.class);
        }

        /**
         * Converts the given number to a {@code Comparable} if its type is different.
         */
        @Override
        public java.lang.Comparable<?> convert(final Number source) {
            if (source == null || source instanceof java.lang.Comparable<?>) {
                return (java.lang.Comparable<?>) source;
            }
            return (java.lang.Comparable<?>) Numbers.narrowestNumber(source);
        }
    }
}
