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

import org.opengis.filter.Filter;

import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.StylePropertyValue;

import org.polymap.model2.Concerns;
import org.polymap.model2.Nullable;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * @author Steffen Stundzig
 */
public class PropertyMatchingNumberFilter
        extends StylePropertyValue<Filter> {

    public static ValueInitializer<PropertyMatchingNumberFilter> defaults() {
        return defaults( "", RelationalOperator.eq, "" );
    }


    public static ValueInitializer<PropertyMatchingNumberFilter> defaults( final String leftProperty,
            final RelationalOperator operator, final String rightLiteral ) {
        return new ValueInitializer<PropertyMatchingNumberFilter>() {

            @Override
            public PropertyMatchingNumberFilter initialize( PropertyMatchingNumberFilter proto ) throws Exception {
                proto.leftProperty.set( leftProperty );
                proto.relationalNumberOperator.set( operator );
                proto.rightLiteral.set( rightLiteral );
                return proto;
            }
        };
    }

    /**
     * The name of the property.
     */
    @Nullable
    @Concerns(StylePropertyChange.Concern.class)
    public Property<String> leftProperty;

    /**
     * The type of the expression.
     */
    @Nullable
    @Concerns(StylePropertyChange.Concern.class)
    public Property<RelationalOperator> relationalNumberOperator;

    /**
     * The content of the literal.
     */
    @Nullable
    @Concerns(StylePropertyChange.Concern.class)
    public Property<String> rightLiteral;

}
