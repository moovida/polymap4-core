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
package org.polymap.core.style.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.polymap.core.style.serialize.sld.SLDSerializer.ff;

import java.util.List;

import java.awt.Color;

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Style;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.style.model.FeatureStyle;
import org.polymap.core.style.model.StyleRepository;
import org.polymap.core.style.model.feature.ConstantColor;
import org.polymap.core.style.model.feature.ConstantFilter;
import org.polymap.core.style.model.feature.ConstantNumber;
import org.polymap.core.style.model.feature.ConstantString;
import org.polymap.core.style.model.feature.ConstantStrokeCapStyle;
import org.polymap.core.style.model.feature.ConstantStrokeDashStyle;
import org.polymap.core.style.model.feature.ConstantStrokeJoinStyle;
import org.polymap.core.style.model.feature.FilterMappedColors;
import org.polymap.core.style.model.feature.FilterMappedNumbers;
import org.polymap.core.style.model.feature.LineStyle;
import org.polymap.core.style.model.feature.PointStyle;
import org.polymap.core.style.model.feature.PolygonStyle;
import org.polymap.core.style.model.feature.PropertyNumber;
import org.polymap.core.style.model.feature.PropertyString;
import org.polymap.core.style.model.feature.ScaleMappedNumbers;
import org.polymap.core.style.model.feature.StrokeDashStyle;
import org.polymap.core.style.model.feature.TextStyle;
import org.polymap.core.style.serialize.FeatureStyleSerializer.OutputFormat;
import org.polymap.core.style.serialize.sld.SLDSerializer;

import org.polymap.model2.runtime.ValueInitializer;

/**
 * @author Falko Br�utigam
 * @author Steffen Stundzig
 */
public class StyleModelTest {

    private static Log log = LogFactory.getLog( StyleModelTest.class );

    private static StyleRepository repo;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        repo = new StyleRepository( null );
    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (repo != null) {
            repo.close();
        }
    }


    // instance *******************************************

    @Test
    public void testPoint() throws Exception {
        FeatureStyle fs = repo.newFeatureStyle();

        // point
        PointStyle point = fs.members().createElement( PointStyle.defaults );
        assertTrue( point.visibleIf.get() instanceof ConstantFilter );

        point.diameter.createValue( ConstantNumber.defaults( 10.0 ) );
        point.fill.get().color.createValue( ConstantColor.defaults( 0, 0, 0 ) );
        point.fill.get().opacity.createValue( ConstantNumber.defaults( 1.0 ) );
        point.stroke.get().color.createValue( ConstantColor.defaults( 100, 100, 100 ) );
        point.stroke.get().width.createValue( ConstantNumber.defaults( 5.0 ) );
        point.stroke.get().opacity.createValue( FilterMappedNumbers.defaults() )
                .add( ff.equals( ff.literal( 1 ), ff.literal( 1 ) ), 0.1 )
                .add( ff.equals( ff.literal( 2 ), ff.literal( 2 ) ), 0.2 );

        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );

        point.stroke.get().opacity.createValue( ConstantNumber.defaults( 1.0 ) );
        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
    }


    @Test
    public void testFeatureBasedPoint() throws Exception {
        FeatureStyle fs = repo.newFeatureStyle();

        // point
        PointStyle point = fs.members().createElement( PointStyle.defaults );

        assertTrue( point.visibleIf.get() instanceof ConstantFilter );

        point.diameter.createValue( ConstantNumber.defaults( 23.0 ) );
        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
        Style style = repo.serializedFeatureStyle( fs.id(), Style.class ).get();
        PointSymbolizer sym = (PointSymbolizer)style.featureTypeStyles().get( 0 ).rules().get( 0 ).symbolizers()
                .get( 0 );
        assertEquals( SLDSerializer.ff.literal( 23.0 ), sym.getGraphic().getSize() );

        point.diameter.createValue( PropertyNumber.defaults( "foo", null, null ) );
        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
        style = repo.serializedFeatureStyle( fs.id(), Style.class ).get();
        sym = (PointSymbolizer)style.featureTypeStyles().get( 0 ).rules().get( 0 ).getSymbolizers()[0];
        assertEquals( SLDSerializer.ff.property( "foo" ), sym.getGraphic().getSize() );

        point.diameter.createValue( ConstantNumber.defaults( 42.0 ) );
        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
        style = repo.serializedFeatureStyle( fs.id(), Style.class ).get();
        sym = (PointSymbolizer)style.featureTypeStyles().get( 0 ).rules().get( 0 ).symbolizers().get( 0 );
        assertEquals( SLDSerializer.ff.literal( 42.0 ), sym.getGraphic().getSize() );
    }


    @Test
    public void testPolygon() throws Exception {
        FeatureStyle fs = repo.newFeatureStyle();

        // point
        PolygonStyle polygon = fs.members().createElement( PolygonStyle.defaults );
        assertTrue( polygon.visibleIf.get() instanceof ConstantFilter );

        polygon.fill.get().color.createValue( ConstantColor.defaults( 1, 2, 3 ) );
        polygon.stroke.get().color.createValue( ConstantColor.defaults( 100, 100, 100 ) );
        polygon.stroke.get().width.createValue( ConstantNumber.defaults( 5.0 ) );
        polygon.stroke.get().opacity.createValue( FilterMappedNumbers.defaults() )
                .add( ff.equals( ff.literal( 1 ), ff.literal( 1 ) ), 0.1 )
                .add( ff.equals( ff.literal( 2 ), ff.literal( 2 ) ), 0.2 );
        polygon.stroke.get().strokeStyle.get().capStyle.createValue( ConstantStrokeCapStyle.defaults() );
        polygon.stroke.get().strokeStyle.get().dashStyle.createValue( ConstantStrokeDashStyle.defaults() );
        polygon.stroke.get().strokeStyle.get().joinStyle.createValue( ConstantStrokeJoinStyle.defaults() );

        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
    }


    @Test
    public void testText() throws Exception {
        FeatureStyle fs = repo.newFeatureStyle();

        // point
        TextStyle text = fs.members().createElement( TextStyle.defaults );

        text.property.createValue( ConstantString.defaults( "constant" ) );
        // text.halo.createValue( Halo.defaults );
        text.halo.get().color.createValue( ConstantColor.defaults( 1, 2, 3 ) );
        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );

        text.property.createValue( PropertyString.defaults( "featureproperty" ) );
        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
    }


    @Test
    public void simpleLine() throws Exception {
        FeatureStyle fs = repo.newFeatureStyle();

        // point
        LineStyle line = fs.members().createElement( LineStyle.defaults );

        line.fill.get().color.createValue( ConstantColor.defaults( 0, 0, 100 ) );
        line.fill.get().width.createValue( ConstantNumber.defaults( 15.0 ) );
        line.fill.get().strokeStyle.get().capStyle.createValue( ConstantStrokeCapStyle.defaults() );
        line.fill.get().strokeStyle.get().dashStyle
                .createValue( ConstantStrokeDashStyle.defaults( StrokeDashStyle.dashdot ) );
        line.fill.get().strokeStyle.get().joinStyle.createValue( ConstantStrokeJoinStyle.defaults() );

        line.stroke.get().color.createValue( ConstantColor.defaults( 100, 0, 0 ) );
        line.stroke.get().width.createValue( ConstantNumber.defaults( 2.0 ) );
        line.stroke.get().strokeStyle.get().capStyle.createValue( ConstantStrokeCapStyle.defaults() );
        line.stroke.get().strokeStyle.get().dashStyle.createValue( ConstantStrokeDashStyle.defaults() );
        line.stroke.get().strokeStyle.get().joinStyle.createValue( ConstantStrokeJoinStyle.defaults() );

        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
    }


    @Test
    public void propertyNumberWithMinimumMaximum() {
        FeatureStyle fs = repo.newFeatureStyle();

        // point
        PointStyle point = fs.members().createElement( PointStyle.defaults );

        point.diameter.createValue( PropertyNumber.defaults( "foo", new Double( 8 ), new Double( 23 ) ) );
        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
        Style style = repo.serializedFeatureStyle( fs.id(), Style.class ).get();
        PointSymbolizer sym = (PointSymbolizer)style.featureTypeStyles().get( 0 ).rules().get( 0 ).getSymbolizers()[0];
        assertEquals( "min([max([foo], [8.0])], [23.0])", sym.getGraphic().getSize().toString() );

        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class, OutputFormat.OGC ) );
        style = repo.serializedFeatureStyle( fs.id(), Style.class, OutputFormat.OGC ).get();
        List<FeatureTypeStyle> featureTypeStyles = style.featureTypeStyles();
        assertEquals( 1, featureTypeStyles.size() );
        assertEquals( "[ foo <= 8.0 ]", featureTypeStyles.get( 0 ).rules().get( 0 ).getFilter().toString() );
        assertEquals( "[ foo >= 23.0 ]", featureTypeStyles.get( 0 ).rules().get( 1 ).getFilter().toString() );
        assertEquals( "[[ foo > 8.0 ] AND [ foo < 23.0 ]]",
                featureTypeStyles.get( 0 ).rules().get( 2 ).getFilter().toString() );

    }


    @Test
    public void propertyMappedNumbers() {
        FeatureStyle fs = repo.newFeatureStyle();

        // point
        PointStyle point = fs.members().createElement( PointStyle.defaults );

        point.diameter.createValue( new ValueInitializer<FilterMappedNumbers<Double>>() {

            @Override
            public FilterMappedNumbers<Double> initialize( FilterMappedNumbers<Double> proto ) throws Exception {
                proto.add( ff.equals( ff.property( "foo" ), ff.literal( "big" ) ), new Double( 5 ) );
                proto.add( ff.equals( ff.property( "foo" ), ff.literal( "bigger" ) ), new Double( 15 ) );
                proto.add( ff.and( ff.notEqual( ff.property( "foo" ), ff.literal( "big" ) ),
                        ff.notEqual( ff.property( "foo" ), ff.literal( "bigger" ) ) ), new Double( 23 ) );
                return proto;
            }
        } );

        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class, OutputFormat.GEOSERVER ) );
        Style style = repo.serializedFeatureStyle( fs.id(), Style.class, OutputFormat.GEOSERVER ).get();
        List<FeatureTypeStyle> featureTypeStyles = style.featureTypeStyles();
        assertEquals( 1, featureTypeStyles.size() );
        assertEquals( Filter.INCLUDE, featureTypeStyles.get( 0 ).rules().get( 0 ).getFilter() );

        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class, OutputFormat.OGC ) );
        style = repo.serializedFeatureStyle( fs.id(), Style.class, OutputFormat.OGC ).get();
        featureTypeStyles = style.featureTypeStyles();
        assertEquals( 1, featureTypeStyles.size() );
        assertEquals( "[ foo = big ]", featureTypeStyles.get( 0 ).rules().get( 0 ).getFilter().toString() );
        assertEquals( "[ foo = bigger ]", featureTypeStyles.get( 0 ).rules().get( 1 ).getFilter().toString() );
        assertEquals( "[[ foo != big ] AND [ foo != bigger ]]",
                featureTypeStyles.get( 0 ).rules().get( 2 ).getFilter().toString() );
    }
    @Test
    public void propertyRangeMappedNumbers() {
        FeatureStyle fs = repo.newFeatureStyle();

        // point
        PointStyle point = fs.members().createElement( PointStyle.defaults );

        point.diameter.createValue( new ValueInitializer<FilterMappedNumbers<Double>>() {

            @Override
            public FilterMappedNumbers<Double> initialize( FilterMappedNumbers<Double> proto ) throws Exception {
                proto.add( ff.lessOrEqual( ff.property( "foo" ), ff.literal( "big" ) ), new Double( 5 ) );
                proto.add( ff.less( ff.property( "foo" ), ff.literal( "bigger" ) ), new Double( 15 ) );
                proto.add( ff.greaterOrEqual( ff.property( "foo" ), ff.literal( "bigger" ) ), new Double( 23 ) );
                return proto;
            }
        } );

        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class, OutputFormat.GEOSERVER ) );
        Style style = repo.serializedFeatureStyle( fs.id(), Style.class, OutputFormat.GEOSERVER ).get();
        List<FeatureTypeStyle> featureTypeStyles = style.featureTypeStyles();
        assertEquals( 1, featureTypeStyles.size() );
        assertEquals( Filter.INCLUDE, featureTypeStyles.get( 0 ).rules().get( 0 ).getFilter() );

        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class, OutputFormat.OGC ) );
        style = repo.serializedFeatureStyle( fs.id(), Style.class, OutputFormat.OGC ).get();
        featureTypeStyles = style.featureTypeStyles();
        assertEquals( 1, featureTypeStyles.size() );
        assertEquals( "[ foo <= big ]", featureTypeStyles.get( 0 ).rules().get( 0 ).getFilter().toString() );
        assertEquals( "[ foo < bigger ]", featureTypeStyles.get( 0 ).rules().get( 1 ).getFilter().toString() );
        assertEquals( "[ foo >= bigger ]",
                featureTypeStyles.get( 0 ).rules().get( 2 ).getFilter().toString() );
    }


    @Test
    public void propertyMappedColors() {
        FeatureStyle fs = repo.newFeatureStyle();

        // point
        PointStyle point = fs.members().createElement( PointStyle.defaults );

        point.diameter.createValue( ConstantNumber.defaults( 5.0 ) );
        point.fill.get().color.createValue( new ValueInitializer<FilterMappedColors>() {

            @Override
            public FilterMappedColors initialize( FilterMappedColors proto ) throws Exception {
                proto.add( ff.equals( ff.property( "foo" ), ff.literal( "big" ) ), new Color( 255, 0, 0 ) );
                proto.add( ff.equals( ff.property( "foo" ), ff.literal( "bigger" ) ), new Color( 0, 0, 255 ) );
                proto.add( ff.and( ff.notEqual( ff.property( "foo" ), ff.literal( "big" ) ),
                        ff.notEqual( ff.property( "foo" ), ff.literal( "bigger" ) ) ), new Color( 0, 0, 255 ) );
                return proto;
            }
        } );
        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
        Style style = repo.serializedFeatureStyle( fs.id(), Style.class, OutputFormat.GEOSERVER ).get();
        List<FeatureTypeStyle> featureTypeStyles = style.featureTypeStyles();
        assertEquals( 1, featureTypeStyles.size() );
        assertEquals( Filter.INCLUDE, featureTypeStyles.get( 0 ).rules().get( 0 ).getFilter() );

        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class, OutputFormat.OGC ) );
        style = repo.serializedFeatureStyle( fs.id(), Style.class, OutputFormat.OGC ).get();
        featureTypeStyles = style.featureTypeStyles();
        assertEquals( 1, featureTypeStyles.size() );
        assertEquals( "[ foo = big ]", featureTypeStyles.get( 0 ).rules().get( 0 ).getFilter().toString() );
        assertEquals( "[ foo = bigger ]", featureTypeStyles.get( 0 ).rules().get( 1 ).getFilter().toString() );
        assertEquals( "[[ foo != big ] AND [ foo != bigger ]]",
                featureTypeStyles.get( 0 ).rules().get( 2 ).getFilter().toString() );
    }


    @Test
    public void scaleMappedNumbers() {
        FeatureStyle fs = repo.newFeatureStyle();

        // point
        PointStyle point = fs.members().createElement( PointStyle.defaults );

        point.diameter.createValue( new ValueInitializer<ScaleMappedNumbers<Double>>() {

            @Override
            public ScaleMappedNumbers<Double> initialize( ScaleMappedNumbers<Double> proto ) throws Exception {
                proto.defaultNumberValue.set( new Double( 23 ) );
                proto.add( new Double( 5 ), new Double( 10000 ) );
                proto.add( new Double( 150 ), new Double( 500000 ) );
                return proto;
            }
        } );

        fs.store();
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class ) );
        log.info( "SLD: " + repo.serializedFeatureStyle( fs.id(), String.class, OutputFormat.OGC ) );
        // Style style = repo.serializedFeatureStyle( fs.id(), Style.class )
        // .get();
        // PointSymbolizer sym =
        // (PointSymbolizer)style.featureTypeStyles.get(0].rules().get(0].getSymbolizers()[0];
        // Map<String, Object> record = Maps.newHashMap();
        // record.put( "foo", 48 );
        // assertEquals( SLDSerializer.ff.literal( 23.0 ),
        // sym.getGraphic().getSize().evaluate( record ) );
    }
}
