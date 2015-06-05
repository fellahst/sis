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
package org.apache.sis.io.wkt;

import java.util.Map;
import java.util.List;
import java.util.Date;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.opengis.util.FactoryException;
import org.apache.sis.util.Workaround;
import org.apache.sis.internal.metadata.WKTParser;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

// Branch-specific imports
import java.util.Calendar;
import java.util.TimeZone;


/**
 * Parses <cite>Well Known Text</cite> (WKT). Parsers are the converse of {@link Formatter}.
 * Like the later, a parser is constructed with a given set of {@linkplain Symbols symbols}.
 * Parsers also need a set of factories to be used for instantiating the parsed objects.
 *
 * <p>In current version, parsers are usually not intended to be subclassed outside this package.</p>
 *
 * <p>Parsers are not synchronized. It is recommended to create separate parser instances for each thread.
 * If multiple threads access a parser concurrently, it must be synchronized externally.</p>
 *
 * @author  Rémi Eve (IRD)
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @since   0.6
 * @version 0.6
 * @module
 */
abstract class Parser implements WKTParser {
    /**
     * Set to {@code true} if parsing of number in scientific notation is allowed.
     * The way to achieve that is currently a hack, because {@link NumberFormat}
     * has no API for managing that as of JDK 1.8.
     *
     * @todo See if a future version of JDK allows us to get ride of this ugly hack.
     */
    @Workaround(library = "JDK", version = "1.8")
    static final boolean SCIENTIFIC_NOTATION = true;

    /**
     * The locale for error messages (not for number parsing), or {@code null} for the system default.
     */
    final Locale errorLocale;

    /**
     * The symbols to use for parsing WKT.
     */
    final Symbols symbols;

    /**
     * The symbol for scientific notation, or {@code null} if none.
     * This is usually {@code "E"} (note the upper case), but could also be something like {@code "×10^"}.
     */
    private final String exponentSymbol;

    /**
     * The object to use for parsing numbers.
     */
    private final NumberFormat numberFormat;

    /**
     * The object to use for parsing dates, created when first needed.
     */
    private DateFormat dateFormat;

    /**
     * Keyword of unknown elements. The ISO 19162 specification requires that we ignore unknown elements,
     * but we will nevertheless report them as warnings.
     */
    final Map<String, List<String>> ignoredElements;

    /**
     * Constructs a parser using the specified set of symbols.
     *
     * @param symbols     The set of symbols to use.
     * @param errorLocale The locale for error messages (not for parsing), or {@code null} for the system default.
     */
    Parser(final Symbols symbols, final Locale errorLocale) {
        this.errorLocale = errorLocale;
        ensureNonNull("symbols", symbols);
        this.symbols = symbols;
        numberFormat = symbols.createNumberFormat();
        if (SCIENTIFIC_NOTATION && numberFormat instanceof DecimalFormat) {
            final DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
            exponentSymbol = decimalFormat.getDecimalFormatSymbols().getExponentSeparator();
            String pattern = decimalFormat.toPattern();
            if (!pattern.contains("E0")) {
                final StringBuilder buffer = new StringBuilder(pattern);
                final int split = pattern.indexOf(';');
                if (split >= 0) {
                    buffer.insert(split, "E0");
                }
                buffer.append("E0");
                decimalFormat.applyPattern(buffer.toString());
            }
        } else {
            exponentSymbol = null;
        }
        ignoredElements = new LinkedHashMap<String, List<String>>();
    }

    /**
     * Creates the object from a string. This method is for implementation of {@code createFromWKT(String)}
     * method is SIS factories only.
     *
     * @param  text Coordinate system encoded in Well-Known Text format (version 1 or 2).
     * @return The result of parsing the given text.
     * @throws FactoryException if the object creation failed.
     *
     * @see org.apache.sis.referencing.factory.GeodeticObjectFactory#createFromWKT(String)
     * @see org.apache.sis.referencing.operation.transform.DefaultMathTransformFactory#createFromWKT(String)
     */
    @Override
    public final Object createFromWKT(final String text) throws FactoryException {
        try {
            return parseObject(text, new ParsePosition(0));
        } catch (ParseException exception) {
            final Throwable cause = exception.getCause();
            if (cause instanceof FactoryException) {
                throw (FactoryException) cause;
            }
            throw new FactoryException(exception);
        }
    }

    /**
     * Parses a <cite>Well Know Text</cite> (WKT).
     *
     * @param  text The text to be parsed.
     * @param  position The position to start parsing from.
     * @return The parsed object.
     * @throws ParseException if the string can not be parsed.
     */
    public Object parseObject(final String text, final ParsePosition position) throws ParseException {
        ignoredElements.clear();
        final Element element = new Element(new Element(this, text, position));
        final Object object = parseObject(element);
        element.close(ignoredElements);
        return object;
    }

    /**
     * Parses the next element in the specified <cite>Well Know Text</cite> (WKT) tree.
     *
     * @param  element The element to be parsed.
     * @return The parsed object.
     * @throws ParseException if the element can not be parsed.
     */
    abstract Object parseObject(final Element element) throws ParseException;

    /**
     * Parses the number at the given position.
     * This is a helper method for {@link Element} only.
     */
    final Number parseNumber(String text, final ParsePosition position) {
        final int base = position.getIndex();
        Number number = numberFormat.parse(text, position);
        if (number != null && exponentSymbol != null) {
            /*
             * HACK: DecimalFormat.parse(…) does not understand lower case 'e' for scientific notation.
             *       It understands upper case 'E' only, so we may need to perform a replacement here.
             */
            int i = position.getIndex();
            if (text.regionMatches(true, i, exponentSymbol, 0, exponentSymbol.length())) {
                text = new StringBuilder(text).replace(i, i + exponentSymbol.length(), exponentSymbol).toString();
                position.setIndex(base);
                number = numberFormat.parse(text, position);
            }
        }
        return number;
    }

    /**
     * Parses the date at the given position.
     * This is a helper method for {@link Element} only.
     */
    final Date parseDate(final String text, final ParsePosition position) {
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat(WKTFormat.DATE_PATTERN, symbols.getLocale());
        }
        /*
         * Following is a workaround specific to the JDK6 branch. Since JDK6 does not understand the 'Z' suffix,
         * we handle it in this method. The Apache SIS branch for JDK7 does not need this hack since JDK7 supports
         * parsing the 'Z' suffix.
         */
        final Date date = dateFormat.parse(text, position);
        if (date != null) {
            final int p = position.getIndex();
            if (p < text.length() && text.charAt(p) == 'Z') {
                position.setIndex(p + 1);
                final Calendar cal = dateFormat.getCalendar();
                final int year   = cal.get(Calendar.YEAR);
                final int month  = cal.get(Calendar.MONTH);
                final int day    = cal.get(Calendar.DAY_OF_MONTH);
                final int hour   = cal.get(Calendar.HOUR_OF_DAY);
                final int minute = cal.get(Calendar.MINUTE);
                final int second = cal.get(Calendar.SECOND);
                final int millis = cal.get(Calendar.MILLISECOND);
                final TimeZone timezone = cal.getTimeZone();
                final long time;
                try {
                    cal.setTimeZone(TimeZone.getTimeZone("UTC"));
                    cal.set(year, month, day, hour, minute, second);
                    cal.set(Calendar.MILLISECOND, millis);
                    time = cal.getTimeInMillis();
                } finally {
                    cal.setTimeZone(timezone);
                }
                date.setTime(time);
            }
        }
        return date;
    }
}
