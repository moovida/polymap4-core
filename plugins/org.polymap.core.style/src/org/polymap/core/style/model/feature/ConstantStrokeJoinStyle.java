/*
 * polymap.org Copyright (C) 2016, the @authors. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 3.0 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.core.style.model.feature;

import org.polymap.core.style.model.StylePropertyChange;
import org.polymap.core.style.model.StylePropertyValue;

import org.polymap.model2.Concerns;
import org.polymap.model2.Property;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * 
 *
 * @author Steffen Stundzig
 */
public class ConstantStrokeJoinStyle
        extends StylePropertyValue<StrokeJoinStyle> {

    /**
     * Initializes a newly created instance with default values.
     */
    public static ValueInitializer<ConstantStrokeJoinStyle> defaults() {
        return defaults( StrokeJoinStyle.round );
    }


    public static ValueInitializer<ConstantStrokeJoinStyle> defaults( final StrokeJoinStyle style ) {
        return new ValueInitializer<ConstantStrokeJoinStyle>() {

            @Override
            public ConstantStrokeJoinStyle initialize( ConstantStrokeJoinStyle proto ) throws Exception {
                proto.value.set( style );
                return proto;
            }
        };
    }

    @Concerns(StylePropertyChange.Concern.class)
    public Property<StrokeJoinStyle> value;
}
