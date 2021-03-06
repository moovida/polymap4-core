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

/**
 * Commonly used web save fonts from
 * http://www.w3schools.com/cssref/css_websafe_fonts.asp
 *
 * @author Steffen Stundzig
 */
public enum FontFamily {
    monospaced("Monospaced"), sansSerif("SansSerif"), serif("Serif");

    private String value;


    FontFamily( final String value ) {
        this.value = value;
    }


    public String value() {
        return value;
    }


    public String[] families() {
        return value.split( "," );
    }


    public static FontFamily forValue( String currentValue ) {
        for (FontFamily family : values()) {
            if (family.value.equals( currentValue )) {
                return family;
            }
        }
        return null;
    }

}
