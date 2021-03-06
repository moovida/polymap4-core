/* 
 * polymap.org
 * Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.style.model.feature;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.geotools.filter.v1_1.OGCConfiguration;
import org.geotools.xml.Configuration;
import org.geotools.xml.Encoder;
import org.geotools.xml.Parser;
import org.opengis.filter.Filter;
import org.xml.sax.SAXException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Timer;
import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.StylePropertyValue;

import org.polymap.model2.Concerns;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Provides a constant filter as style property value.
 *
 * @author Falko Br�utigam
 */
public class ConstantFilter
        extends StylePropertyValue<Filter> {

    private static Log log = LogFactory.getLog( ConstantFilter.class );

    private final static QName name = org.geotools.filter.v1_1.OGC.Filter;

    private static Parser parser;

    private static final Configuration ENCODE_CONFIG = new org.geotools.filter.v1_1.OGCConfiguration();

    private static final Configuration CONFIGURATION = new OGCConfiguration();

    public static final Charset ENCODE_CHARSET = Charset.forName( "UTF-8" );

    /**
     * 
     */
    public static final ValueInitializer<ConstantFilter> defaultTrue = new ValueInitializer<ConstantFilter>() {

        @Override
        public ConstantFilter initialize( ConstantFilter proto ) throws Exception {
            return proto;
        }
    };

    // instance *******************************************

    /** Null specifies {@link Filter#INCLUDE}. */
    @Nullable
    @Concerns(StylePropertyChange.Concern.class)
    protected Property<String> encoded;


    public ConstantFilter setFilter( Filter filter ) throws IOException {
        encoded.set( encode( filter ) );
        return this;
    }


    public Filter filter() throws IOException, SAXException, ParserConfigurationException {
        return encoded.get() != null ? decode( encoded.get() ) : Filter.INCLUDE;
    }


    public static Filter decode( String encoded ) throws IOException, SAXException, ParserConfigurationException {
        Timer t = new Timer().start();
        Object result = getParser().parse( new ByteArrayInputStream( encoded.getBytes( ENCODE_CHARSET ) ) );
        log.info( "Filter decoded (" + t.elapsedTime() + "ms): " );
        if (result instanceof Filter) {
            return (Filter)result;
        }
        if (result instanceof String && StringUtils.isBlank( (String)result )) {
            return Filter.INCLUDE;
        }
        throw new IOException( "unknown parser result " + result );
    }


    private static synchronized Parser getParser() {
        if (parser == null) {
            parser = new Parser( ENCODE_CONFIG );
        }
        return parser;
    }


    public static String encode( Filter filter ) throws IOException {
        Timer t = new Timer().start();

        String encoded = getEncoder().encodeAsString( filter, name );
        log.info( "Filter encoded (" + t.elapsedTime() + "ms): " );
        return encoded;
    }


    private static Encoder getEncoder() {
        // if (encoder == null) {
        Encoder encoder = new Encoder( CONFIGURATION );
        encoder.setIndenting( true );
        encoder.setEncoding( ENCODE_CHARSET );
        encoder.setNamespaceAware( false );
        encoder.setOmitXMLDeclaration( true );
        // }
        return encoder;
    }

}
