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

import static org.polymap.core.ui.FormDataFactory.on;

import java.util.Collection;
import java.util.List;

import java.io.IOException;
import java.text.DecimalFormat;

import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

import org.polymap.core.runtime.i18n.IMessages;
import org.polymap.core.style.Messages;
import org.polymap.core.style.model.feature.NumberRange;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.StatusDispatcher;

/**
 * Chooser which loads all values for a selected property and add a lower and upper
 * bound spinner, and also high and low values for the mapped numbers.
 *
 * @author Steffen Stundzig
 */
public class FeaturePropertyRangeMappedNumbersChooser {

    private static final IMessages i18n = Messages.forPrefix( "FeaturePropertyRangeMappedNumbersChooser" );

    // private final ExpressionMappedColors property;

    private final FeatureStore featureStore;

    private final FeatureType featureType;

    private String propertyName;

    private NumberRange range;

    private Double mappedMinimum;

    private Double mappedMaximum;

    private Number lowerBound;

    private Number upperBound;

    private Integer steps;

    private Spinner stepsSpinner;

    private Spinner mappedMaximumSpinner;

    private Spinner mappedMinimumSpinner;

    private Spinner upperBoundSpinner;

    private Spinner lowerBoundSpinner;

    private boolean isInteger = false;


    public FeaturePropertyRangeMappedNumbersChooser( String propertyName, Number lowerBound, Number upperBound,
            Double mappedMinimum, Double mappedMaximum, Integer steps, NumberRange range, FeatureStore featureStore,
            FeatureType featureType ) {
        this.propertyName = propertyName;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.mappedMinimum = mappedMinimum != null ? mappedMinimum.doubleValue() : range.from();
        this.mappedMaximum = mappedMaximum != null ? mappedMaximum.doubleValue() : range.to();

        this.steps = steps;
        this.range = range;
        this.featureStore = featureStore;
        this.featureType = featureType;
    }


    public String title() {
        return i18n.get( "title" );
    }

    private static Log log = LogFactory.getLog( FeaturePropertyRangeMappedNumbersChooser.class );


    public void createContents( Composite parent ) {
        parent.setLayout( FormLayoutFactory.defaults().spacing( 16 ).create() );
        // property chooser
        final Combo propertyCombo = new Combo( parent, SWT.SINGLE | SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY );

        final Label currentValues = new Label( parent, SWT.NONE );
        final int digits = range.digits();
        final double factorX = Math.pow( 10, digits );

        lowerBoundSpinner = new Spinner( parent, SWT.BORDER );
        upperBoundSpinner = new Spinner( parent, SWT.BORDER );

        final List<String> properties = properties();
        propertyCombo.setItems( properties.toArray( new String[properties.size()] ) );
        propertyCombo.select( properties.indexOf( propertyName ) );
        propertyCombo.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e ) {
                propertyName = properties.get( propertyCombo.getSelectionIndex() );
                try {
                    updateSpinner( currentValues, digits, factorX );
                }
                catch (IOException e1) {
                    StatusDispatcher.handleError( "error during load of property values", e1 );
                }
            }
        } );

        lowerBoundSpinner.setDigits( digits );
        lowerBoundSpinner.setMinimum( Integer.MIN_VALUE );
        lowerBoundSpinner.setMaximum( Integer.MAX_VALUE );
        // lowerBoundSpinner.setIncrement( (int)(range.increment() * factorX) );
        // lowerBoundSpinner.setPageIncrement( (int)(range.increment() * factorX *
        // 10) );
        double currentLowerBound = lowerBound != null ? lowerBound.doubleValue() : 0.0;
        lowerBoundSpinner.setSelection( (int)(currentLowerBound * factorX) );
        lowerBoundSpinner.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                int selection = lowerBoundSpinner.getSelection();
                if (lowerBoundSpinner.getDigits() > 0) {
                    lowerBound = selection / Math.pow( 10, digits );
                }
                else {
                    lowerBound = selection;
                }
                upperBoundSpinner.setMinimum( selection );
            }
        } );

        upperBoundSpinner.setMinimum( Integer.MIN_VALUE );
        upperBoundSpinner.setMaximum( Integer.MAX_VALUE );
        // upperBoundSpinner.setIncrement( (int)(range.increment() * factorX) );
        // upperBoundSpinner.setPageIncrement( (int)(range.increment() * factorX *
        // 10) );
        upperBoundSpinner.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                int selection = upperBoundSpinner.getSelection();
                if (upperBoundSpinner.getDigits() > 0) {
                    upperBound = selection / Math.pow( 10, digits );
                }
                else {
                    upperBound = selection;
                }
                lowerBoundSpinner.setMaximum( selection );
            }
        } );

        mappedMinimumSpinner = new Spinner( parent, SWT.BORDER );
        mappedMinimumSpinner.setDigits( digits );
        mappedMinimumSpinner.setMinimum( (int)(range.from() * factorX) );
        mappedMinimumSpinner.setMaximum( (int)(range.to() * factorX) );
        mappedMinimumSpinner.setIncrement( (int)(range.increment() * factorX) );
        mappedMinimumSpinner.setPageIncrement( (int)(range.increment() * factorX * 10) );
        mappedMinimumSpinner.setSelection( (int)(mappedMinimum * factorX) );
        mappedMinimumSpinner.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                int selection = mappedMinimumSpinner.getSelection();
                mappedMinimum = selection / Math.pow( 10, digits );
                // maximumMappedSpinner.setMinimum( selection );
            }
        } );

        mappedMaximumSpinner = new Spinner( parent, SWT.BORDER );
        mappedMaximumSpinner.setDigits( digits );
        mappedMaximumSpinner.setMinimum( (int)(range.from() * factorX) );
        mappedMaximumSpinner.setMaximum( (int)(range.to() * factorX) );
        mappedMaximumSpinner.setIncrement( (int)(range.increment() * factorX) );
        mappedMaximumSpinner.setPageIncrement( (int)(range.increment() * factorX * 10) );
        mappedMaximumSpinner.setSelection( (int)(mappedMaximum * factorX) );
        mappedMaximumSpinner.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                int selection = mappedMaximumSpinner.getSelection();
                mappedMaximum = selection / Math.pow( 10, digits );
                // minimumMappedSpinner.setMaximum( selection );
            }
        } );

        stepsSpinner = new Spinner( parent, SWT.BORDER );
        stepsSpinner.setDigits( 0 );
        stepsSpinner.setMinimum( 1 );
        stepsSpinner.setMaximum( 100 );
        stepsSpinner.setSelection( steps );
        stepsSpinner.addSelectionListener( new SelectionAdapter() {

            public void widgetSelected( SelectionEvent e ) {
                steps = stepsSpinner.getSelection();
            }
        } );

        Label l = new Label( parent, SWT.NONE | SWT.RIGHT );
        l.setText( i18n.get( "selectProperty" ) );
        on( l ).top( 3 ).left( 0 );
        on( propertyCombo ).top( 0 ).left( l ).right( 100 );
        on( currentValues ).top( propertyCombo ).left( 0 ).right( 100 ).width( 450 );

        l = new Label( parent, SWT.NONE | SWT.RIGHT );
        l.setText( i18n.get( "lowerBound" ) );
        on( l ).top( currentValues, 3 ).left( 0 );
        on( lowerBoundSpinner ).top( currentValues ).left( l );

        l = new Label( parent, SWT.NONE | SWT.RIGHT );
        l.setText( i18n.get( "mapsTo" ) );
        on( l ).top( currentValues, 3 ).left( lowerBoundSpinner );
        on( mappedMinimumSpinner ).top( currentValues ).left( l );

        l = new Label( parent, SWT.NONE | SWT.RIGHT );
        l.setText( i18n.get( "upperBound" ) );
        on( l ).top( lowerBoundSpinner, 3 ).left( 0 );
        on( upperBoundSpinner ).top( lowerBoundSpinner ).left( l );

        l = new Label( parent, SWT.NONE | SWT.RIGHT );
        l.setText( i18n.get( "mapsTo" ) );
        on( l ).top( lowerBoundSpinner, 3 ).left( upperBoundSpinner );
        on( mappedMaximumSpinner ).top( lowerBoundSpinner ).left( l );

        l = new Label( parent, SWT.NONE | SWT.RIGHT );
        l.setText( i18n.get( "steps" ) );
        on( l ).top( upperBoundSpinner, 3 ).left( 0 );
        on( stepsSpinner ).top( upperBoundSpinner ).left( l );

        try {
            updateSpinner( currentValues, digits, factorX );
        }
        catch (IOException e1) {
            StatusDispatcher.handleError( "error during load of property values", e1 );
        }
    }


    private void updateSpinner( Label label, int digits, double factorX ) throws IOException {
        if (!StringUtils.isBlank( propertyName )) {
            // load all values and count min max
            Number min = null;
            Number max = null;

            FeatureCollection featureCollection = featureStore.getFeatures();
            FeatureIterator iterator = featureCollection.features();
            Class<?> binding = featureStore.getSchema().getDescriptor( propertyName ).getType().getBinding();
            isInteger = binding == Integer.class || binding == Long.class;

            // color for empty or null is also the default color
            boolean valuesFound = false;
            while (iterator.hasNext()) {
                SimpleFeature feature = (SimpleFeature)iterator.next();
                Object rawValue = feature.getAttribute( propertyName );
                if (rawValue != null && rawValue instanceof Number) {
                    Number currentValue = (Number)rawValue;
                    valuesFound = true;
                    if (min == null) {
                        min = currentValue;
                    }
                    else {
                        if (isInteger) {
                            min = Math.min( currentValue.intValue(), min.intValue() );
                        }
                        else {
                            min = Math.min( currentValue.doubleValue(), min.doubleValue() );
                        }
                    }
                    if (max == null) {
                        max = currentValue;
                    }
                    else {
                        if (isInteger) {
                            max = Math.max( currentValue.intValue(), max.intValue() );
                        }
                        else {
                            max = Math.max( currentValue.doubleValue(), max.doubleValue() );
                        }
                    }
                }
            }
            enableSpinner( valuesFound );
            if (valuesFound) {
                DecimalFormat df = new DecimalFormat();
                if (isInteger) {
                    df.setMaximumFractionDigits( 0 );
                    df.setMinimumFractionDigits( 0 );
                }
                else {
                    df.setMaximumFractionDigits( digits );
                    df.setMinimumFractionDigits( digits );
                }
                label.setText( i18n.get( "currentValues", df.format( min ), df.format( max ) ) );
                if (lowerBound == null) {
                    lowerBound = min;
                }
                if (upperBound == null) {
                    upperBound = max;
                }
            }
            else {
                label.setText( i18n.get( "noValues" ) );
            }
        }
        else {
            enableSpinner( false );
            label.setText( "" );
        }

        if (isInteger) {
            lowerBoundSpinner.setDigits( 0 );
            int currentLowerBound = lowerBound != null ? lowerBound.intValue() : 0;
            lowerBoundSpinner.setSelection( currentLowerBound );

            upperBoundSpinner.setDigits( 0 );
            int currentUpperBound = upperBound != null ? upperBound.intValue() : 1000;
            upperBoundSpinner.setSelection( currentUpperBound );
        }
        else {
            lowerBoundSpinner.setDigits( digits );
            double currentLowerBound = lowerBound != null ? lowerBound.doubleValue() : 0.0;
            lowerBoundSpinner.setSelection( (int)(currentLowerBound * factorX) );

            upperBoundSpinner.setDigits( digits );
            double currentUpperBound = upperBound != null ? upperBound.doubleValue() : 10000.0;
            upperBoundSpinner.setSelection( (int)(currentUpperBound * factorX) );
        }

    }


    private void enableSpinner( boolean enabled ) {
        stepsSpinner.setEnabled( enabled );
        mappedMaximumSpinner.setEnabled( enabled );
        mappedMinimumSpinner.setEnabled( enabled );
        upperBoundSpinner.setEnabled( enabled );
        lowerBoundSpinner.setEnabled( enabled );
    }


    private List<String> properties() {
        Collection<PropertyDescriptor> schemaDescriptors = featureType.getDescriptors();
        GeometryDescriptor geometryDescriptor = featureType.getGeometryDescriptor();
        final List<String> allowedProperties = Lists.newArrayList();
        for (PropertyDescriptor descriptor : schemaDescriptors) {
            if (geometryDescriptor == null || !geometryDescriptor.equals( descriptor )) {
                if (Number.class.isAssignableFrom( descriptor.getType().getBinding() )) {
                    allowedProperties.add( descriptor.getName().getLocalPart() );
                }
            }
        }
        return allowedProperties;
    }


    public String propertyName() {
        return propertyName;
    }


    public Double mappedMinimum() {
        return mappedMinimum;
    }


    public Double mappedMaximum() {
        return mappedMaximum;
    }


    public Number lowerBound() {
        return lowerBound;
    }


    public Number upperBound() {
        return upperBound;
    }


    public Integer steps() {
        return steps;
    }


    public boolean isInteger() {
        return isInteger;
    }
}
