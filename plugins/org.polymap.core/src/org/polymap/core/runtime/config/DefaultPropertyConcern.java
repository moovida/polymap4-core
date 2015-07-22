/* 
 * polymap.org
 * Copyright (C) 2015, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.runtime.config;

/**
 * Provides default empty implementations for all methods. 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class DefaultPropertyConcern<T>
        implements PropertyConcern<T> {

    @Override
    public T doInit( Object obj, Config<T> prop, T value ) {
        return value;
    }

    @Override
    public T doGet( Object obj, Config<T> prop, T value ) {
        return value;
    }

    @Override
    public T doSet( Object obj, Config<T> prop, T value ) {
        return value;
    }


}
