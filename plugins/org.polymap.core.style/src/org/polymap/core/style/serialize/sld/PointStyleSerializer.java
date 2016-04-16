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
package org.polymap.core.style.serialize.sld;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.PointStyle;
import org.polymap.core.style.serialize.sld.StylePropertyValueHandler.Setter;

/**
 * 
 *
 * @author Falko Br�utigam
 */
public class PointStyleSerializer
        extends StyleSerializer<PointStyle> {

    private static Log log = LogFactory.getLog( PointStyleSerializer.class );


    @Override
    public void serialize( PointStyle style ) {
        // XXX setter should be inline
        Setter<PointSymbolizerDescriptor,Integer> setter = (sd,value) -> sd.strokeWidth.set( value );
        setValue( style.strokeWidth.get(), setter );
    }
    
}