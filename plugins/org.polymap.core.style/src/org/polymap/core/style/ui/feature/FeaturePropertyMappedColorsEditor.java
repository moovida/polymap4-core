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
package org.polymap.core.style.ui.feature;

import java.util.List;
import java.util.Map;

import java.awt.Color;

import org.opengis.filter.And;
import org.opengis.filter.Filter;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.StylePlugin;
import org.polymap.core.style.model.feature.FilterMappedColors;
import org.polymap.core.style.ui.StylePropertyEditor;
import org.polymap.core.style.ui.StylePropertyFieldSite;
import org.polymap.core.style.ui.UIService;
import org.polymap.core.style.ui.feature.FeaturePropertyMappedColorsChooser.Triple;

import org.polymap.model2.runtime.ValueInitializer;

/**
 * Editor that creates colors based on feature attributes.
 *
 * @author Steffen Stundzig
 */
public class FeaturePropertyMappedColorsEditor
        extends StylePropertyEditor<FilterMappedColors> {

    private static final IMessages i18n = Messages.forPrefix( "FeaturePropertyMappedColorsEditor" );

    private String propertyName;

    private Color defaultColor;

    private Map<String,Color> initialColors;

    private org.eclipse.swt.graphics.Color defaultFg;


    @Override
    public String label() {
        return i18n.get( "title" );
    }


    @Override
    public boolean init( StylePropertyFieldSite site ) {
        return Color.class.isAssignableFrom( targetType( site ) ) && site.featureStore.isPresent()
                && site.featureType.isPresent() ? super.init( site ) : false;
    }


    @Override
    public void updateProperty() {
        prop.createValue( new ValueInitializer<FilterMappedColors>() {

            @Override
            public FilterMappedColors initialize( FilterMappedColors proto ) throws Exception {
                proto.encodedFilters.clear();
                proto.colorValues.clear();
                return proto;
            }
        } );
    }


    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = super.createContents( parent );
        Button button = new Button( parent, SWT.FLAT|SWT.LEFT );
        defaultFg = button.getForeground();

        initialize();

        button.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                FeaturePropertyMappedColorsChooser cc = new FeaturePropertyMappedColorsChooser( propertyName,
                        defaultColor, initialColors, featureStore(), featureType() );
                UIService.instance().openDialog( cc.title(), dialogParent -> {
                    cc.createContents( dialogParent );
                }, () -> {
                    if (cc.propertyName() != null && !cc.triples().isEmpty()) {
                        propertyName = cc.propertyName();
                        initialColors.clear();

                        prop.get().encodedFilters.clear();
                        prop.get().colorValues.clear();
                        List<Filter> opposite = Lists.newArrayList();
                        for (Triple triple : cc.triples()) {
                            if (!StringUtils.isBlank( triple.label() )) {
                                prop.get().add( ff.equals( ff.property( propertyName ), ff.literal( triple.label() ) ),
                                        triple.color() );
                                opposite.add(
                                        ff.notEqual( ff.property( propertyName ), ff.literal( triple.label() ) ) );
                                initialColors.put( triple.label(), triple.color() );
                            }
                            else {
                                // empty contains the default color
                                defaultColor = triple.color();
                            }
                        }
                        if (defaultColor != null) {
                            prop.get().add( ff.and( opposite ), defaultColor );
                        }
                        prop.get().fake.set( "" + System.currentTimeMillis() );
                        updateButtonColor( button, defaultColor );
                    }
                    return true;
                } );
            }
        } );
        updateButtonColor( button, defaultColor );
        return contents;
    }


    protected void initialize() {
        List<Filter> expressions = prop.get().filters();
        List<Color> values = prop.get().values();
        initialColors = Maps.newHashMap();
        for (int i = 0; i < expressions.size(); i++) {
            Filter filter = expressions.get( i );
            Color color = values.get( i );
            if (filter instanceof PropertyIsEqualTo) {
                PropertyIsEqualTo piet = (PropertyIsEqualTo)filter;
                if (propertyName == null) {
                    propertyName = ((PropertyName)piet.getExpression1()).getPropertyName();
                }
                String property = ((Literal)piet.getExpression2()).getValue().toString();
                initialColors.put( property, color );
            }
            else if (filter instanceof And) {
                defaultColor = color;
            }
        }
    }


    protected void updateButtonColor( Button button, Color color ) {
        if (!StringUtils.isBlank( propertyName ) && !initialColors.isEmpty()) {
            button.setText( i18n.get( "rechoose", propertyName, initialColors.size() ) );
            button.setBackground( StylePlugin.okColor() );
            button.setForeground( defaultFg );
        }
        else {
            button.setText( i18n.get( "choose" ) );
            button.setBackground( StylePlugin.okColor() );
            button.setForeground( StylePlugin.errorColor() );
        }

//        org.eclipse.swt.graphics.Color rgb = color != null
//                ? UIUtils.getColor( color.getRed(), color.getGreen(), color.getBlue() ) 
//                : StylePlugin.errorColor();
//                
//        button.setBackground( rgb );
//        if (rgb.getRed() * rgb.getBlue() * rgb.getGreen() > (255 * 255 * 255) / 2) {
//            button.setForeground( UIUtils.getColor( 0, 0, 0 ) );
//        }
//        else {
//            button.setForeground( UIUtils.getColor( 255, 255, 255 ) );
//        }
    }


    @Override
    public boolean isValid() {
        return !StringUtils.isBlank( propertyName ) && defaultColor != null;
    }

}
