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
package org.polymap.core.style.ui;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.ConstantFontWeight;
import org.polymap.core.style.model.FontWeight;
import org.polymap.model2.runtime.ValueInitializer;

/**
 * Editor that creates one {@link ConstantFontWeight}.
 *
 * @author Steffen Stundzig
 */
public class ConstantFontWeightEditor
        extends StylePropertyEditor<ConstantFontWeight> {

    private static final IMessages i18n = Messages.forPrefix( "FontWeight" );

    private static Log log = LogFactory.getLog( ConstantFontWeightEditor.class );

    private final static List<FontWeight> content = Lists.newArrayList( FontWeight.values() );


    @Override
    public String label() {
        return i18n.get( "title" );
    }


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return FontWeight.class.isAssignableFrom( targetType( site ) ) ? super.init( site ) : false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( new ValueInitializer<ConstantFontWeight>() {

            @Override
            public ConstantFontWeight initialize( ConstantFontWeight proto ) throws Exception {
                proto.value.set( FontWeight.normal );
                return proto;
            }
        } );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        Combo combo = new Combo( contents, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN );

        combo.setItems( content.stream().map( FontWeight::name ).map( n -> i18n.get( n ) ).toArray( String[]::new ) );
        combo.select( content.indexOf( prop.get().value.get() ) );

        combo.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                prop.get().value.set( content.get( combo.getSelectionIndex() ) );
            }
        } );
        return contents;
    }
}