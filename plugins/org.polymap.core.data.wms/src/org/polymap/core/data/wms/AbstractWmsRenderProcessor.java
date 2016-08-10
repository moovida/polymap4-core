/* 
 * polymap.org
 * Copyright (C) 2009-2015, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.data.wms;

import static java.util.Collections.singletonList;

import java.awt.Color;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.ows.CRSEnvelope;
import org.geotools.data.ows.Layer;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.polymap.core.data.image.GetLayerTypesRequest;
import org.polymap.core.data.image.GetLayerTypesResponse;
import org.polymap.core.data.image.GetLegendGraphicRequest;
import org.polymap.core.data.image.GetMapRequest;
import org.polymap.core.data.image.LayerType;
import org.polymap.core.data.pipeline.DataSourceDescription;
import org.polymap.core.data.pipeline.PipelineExecutor.ProcessorContext;
import org.polymap.core.data.pipeline.PipelineProcessorSite;
import org.polymap.core.data.pipeline.ProcessorResponse;
import org.polymap.core.data.pipeline.TerminalPipelineProcessor;

import com.vividsolutions.jts.geom.Envelope;

/**
 * Abstract base class, which does the complete WMS request preparation.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 * @author Steffen Stundzig
 */
public abstract class AbstractWmsRenderProcessor
        implements TerminalPipelineProcessor {

    private static final Log log = LogFactory.getLog( AbstractWmsRenderProcessor.class );

    private static final ReferencedEnvelope NILL_BOX = 
            new ReferencedEnvelope( 0, 0, 0, 0, DefaultGeographicCRS.WGS84 );

    private WebMapServer            wms;
    
    private String                  layerName;

    private Layer                   layer;

    private PipelineProcessorSite   site;
    

    // instance *******************************************

    @Override
    public void init( @SuppressWarnings("hiding") PipelineProcessorSite site ) throws Exception {
        this.site = site;
        wms = (WebMapServer)site.dsd.get().service.get();
        layerName = site.dsd.get().resourceName.get();

        layer = wms.getCapabilities().getLayerList().stream()
                .filter( l -> layerName.equals( l.getName() ) )
                .findFirst().orElseThrow( () -> new RuntimeException( "No layer found for name: " + layerName ) );
    }


    @Override
    public boolean isCompatible( DataSourceDescription dsd ) {
        return dsd.service.get() instanceof WebMapServer;
    }


    public void getLegendGraphicRequest( GetLegendGraphicRequest request, ProcessorContext context ) throws Exception {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    public void getLayerTypesRequest( GetLayerTypesRequest request, ProcessorContext context ) throws Exception {
        List<ReferencedEnvelope> boundingBoxes = new ArrayList();
        for (CRSEnvelope env : layer.getBoundingBoxes().values()) {
            boundingBoxes.add( new ReferencedEnvelope( env ) );
        }
        LayerType layerType = new LayerType( layer.getTitle(), layer.getName(), null, boundingBoxes );
        context.sendResponse( new GetLayerTypesResponse( singletonList( layerType ) ) );
    }


    public void getMapRequest( GetMapRequest request, ProcessorContext context ) throws Exception {
        int width = request.getWidth();
        int height = request.getHeight();
        BoundingBox bbox = request.getBoundingBox();
        log.info( "bbox=" + bbox + ", imageSize=" + width + "x" + height );

        org.geotools.data.wms.request.GetMapRequest getMap = wms.createGetMapRequest();
        
        getMap.setFormat( request.getFormat() );
        getMap.setDimensions( width, height );
        getMap.setTransparent( true );
        Color color = request.getBgColor();
        if (color != null) {
            getMap.setBGColour( String.format( "#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue() ) );
        }
        //getMap.setBGColour( "0xFFFFFF" );
        
        BoundingBox requestBBox = bbox;
        getMap.setBBox( requestBBox.getMinX() + "," + requestBBox.getMinY() //$NON-NLS-1$
                + "," + requestBBox.getMaxX() //$NON-NLS-1$
                + "," + requestBBox.getMaxY() ); //$NON-NLS-1$
        String srs = request.getCRS();
        getMap.setSRS( srs );
        
        
//        Map sortedLayersMap = MultiValueMap.decorate( new TreeMap() );
//        for (Iterator it=visibleLayers.referencedLayers(); it.hasNext();) {
//            Layer layer = (Layer)availableLayers.get( it.next() );
//            Integer layerOrder = (Integer)layerOrderMap.get( layer.getName() );
//            // reverse order; request.addLayer() works that way
//            Integer key = Integer.valueOf( Integer.MAX_VALUE - layerOrder.intValue() );
//            sortedLayersMap.put( key, layer );
//        }
        
//        for (ILayer layer : layers) {
//            IGeoResource geores = layer.getGeoResource();
//            Layer wmsLayer = geores.resolve( org.geotools.data.ows.Layer.class, null );
//            getMap.addLayer( wmsLayer );
//            log.debug( "    request: layer: " + layer.getLabel() );
//        }

        
        getMap.addLayer( layer );
        log.debug( "    WMS URL:" + getMap.getFinalURL() );
        
        prepareRequest(getMap);
        
        InputStream in = null;
        try {
            long start = System.currentTimeMillis();
            GetMapResponse wmsResponse = wms.issueRequest( getMap );
            log.debug( "Got repsonse (" + (System.currentTimeMillis()-start) + "ms). providing data: " + wmsResponse.getContentType() );
            
            in = wmsResponse.getInputStream();
            handleResponse(in, context);
            context.sendResponse( ProcessorResponse.EOP );
            log.debug( "...all data send." );
        }
        finally {
            IOUtils.closeQuietly( in );
        }
    }

    /**
     * Create the correct pipeline response for the resulting WMS input stream
     * @param in
     * @param context
     */
    protected abstract void handleResponse( InputStream in, ProcessorContext context ) throws Exception;

    /**
     * Do some final changes to the WMS request
     * @param getMap
     */
    protected abstract void prepareRequest( org.geotools.data.wms.request.GetMapRequest getMap );


    /**
     * Using the viewport bounds and combined wms layer extents, determines an appropriate bounding
     * box by projecting the viewport into the request CRS, intersecting the bounds, and returning
     * the result.
     * 
     * @param wmsLayers all adjacent wms layers we are requesting
     * @param viewport map editor bounds and crs
     * @param requestCRS coordinate reference system supported by the server
     * @return the bbox to ask the server for
     * @throws MismatchedDimensionException
     * @throws TransformException
     * @throws FactoryException
     */
    public static ReferencedEnvelope calculateRequestBBox( List<Layer> wmsLayers,
            ReferencedEnvelope viewport, CoordinateReferenceSystem requestCRS )
            throws MismatchedDimensionException, TransformException, FactoryException {
        /* The bounds of all wms layers on this server combined */
        Envelope layersBBox = getLayersBoundingBox( requestCRS, wmsLayers );
        if (isEnvelopeNull( layersBBox )) {
            // the wms server has no bounds
            log.debug( "Zero width/height envelope: wmsLayers = " + layersBBox ); //$NON-NLS-1$
            layersBBox = null;
            // alternatively, we could impose a reprojected -180,180,-90,90
        }

        /* The viewport bounds projected to the request crs */
        ReferencedEnvelope reprojectedViewportBBox = viewport.transform( requestCRS, true );
        if (isEnvelopeNull( reprojectedViewportBBox )) {
            // viewport couldn't be reprojected
            log.debug( "Zero width/height envelope: reprojected viewport from " + viewport //$NON-NLS-1$
                    + " to " + requestCRS + " returned " + reprojectedViewportBBox ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // alternative for better accuracy: new
        // ReferencedEnvelope(JTS.transform(viewport, null,
        // CRS.findMathTransform(viewportCRS, crs, true), 4), crs);

        /* The intersection of the viewport and the combined wms layers */
        Envelope interestBBox;
        if (layersBBox == null) {
            interestBBox = reprojectedViewportBBox;
        }
        else {
            interestBBox = reprojectedViewportBBox.intersection( layersBBox );
        }
        if (isEnvelopeNull( interestBBox )) {
            // outside of bounds, do not draw
            log.debug( "Bounds of the data are outside the bounds of the viewscreen." ); //$NON-NLS-1$
            return NILL_BOX;
        }

        /* The bounds of the request we are going to make */
        ReferencedEnvelope requestBBox = new ReferencedEnvelope( interestBBox, requestCRS );
        return requestBBox;
    }


    public static Envelope getLayersBoundingBox( CoordinateReferenceSystem crs, List<Layer> layers ) {
        Envelope envelope = null;

        for (Layer layer : layers) {

            GeneralEnvelope temp = layer.getEnvelope( crs );

            if (temp != null) {
                Envelope jtsTemp = new Envelope( temp.getMinimum( 0 ), temp.getMaximum( 0 ), temp
                        .getMinimum( 1 ), temp.getMaximum( 1 ) );
                if (envelope == null) {
                    envelope = jtsTemp;
                }
                else {
                    envelope.expandToInclude( jtsTemp );
                }
            }
        }
        return envelope;
    }

    
    protected static boolean isEnvelopeNull( Envelope bbox ) {
        if (bbox.getWidth() <= 0 || bbox.getHeight() <= 0) {
            return true;
        }
        return false;
    }

}
