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
package org.polymap.core.style.serialize.sld.feature;

import org.opengis.filter.expression.Expression;

import org.polymap.core.runtime.config.Config;
import org.polymap.core.style.model.feature.Halo;
import org.polymap.core.style.serialize.FeatureStyleSerializer.Context;
import org.polymap.core.style.serialize.sld.StyleCompositeSerializer;
import org.polymap.core.style.serialize.sld.SymbolizerDescriptor;

import org.polymap.model2.Immutable;

/**
 * 
 * @author Steffen Stundzig
 */
public class HaloDescriptor
        extends SymbolizerDescriptor {

    @Immutable
    // @DefaultDouble( 2 )
    // @Check(value = NumberRangeValidator.class, args = { "0", "100" })
    // Double
    public Config<Expression> width;

    @Immutable
    // Color
    public Config<Expression> color;

    @Immutable
    // @DefaultDouble( 1 )
    // @Check(value = NumberRangeValidator.class, args = { "0", "1" })
    // Double
    public Config<Expression> opacity;

    @Override
    public HaloDescriptor clone() {
        return (HaloDescriptor)super.clone();
    }
    
    
    /**
     * Serializes {@link Halo} into {@link HaloDescriptor}.
     */
    public static class Serializer
            extends StyleCompositeSerializer<Halo,HaloDescriptor> {

        public Serializer( Context context ) {
            super( context );
        }

        @Override
        protected HaloDescriptor createDescriptor() {
            return new HaloDescriptor();
        }

        @Override
        public void doSerialize( Halo style ) {
            setValue( style.width.get(), (HaloDescriptor sd, Expression value) -> sd.width.set( value ) );
            setValue( style.color.get(), (HaloDescriptor sd, Expression value) -> sd.color.set( value ) );
            setValue( style.opacity.get(), (HaloDescriptor sd, Expression value) -> sd.opacity.set( value ) );
        }
    }

}
