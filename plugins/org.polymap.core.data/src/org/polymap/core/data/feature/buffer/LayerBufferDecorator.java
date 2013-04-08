/* 
 * polymap.org
 * Copyright 2011-2013, Polymap GmbH. All rights reserved.
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
package org.polymap.core.data.feature.buffer;

import java.util.EventObject;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;

import com.google.common.collect.MapMaker;

import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;

import org.polymap.core.data.DataPlugin;
import org.polymap.core.data.FeatureChangeEvent;
import org.polymap.core.data.FeatureChangeEvent.Type;
import org.polymap.core.data.FeatureStateTracker;
import org.polymap.core.project.ILayer;
import org.polymap.core.runtime.entity.EntityHandle;
import org.polymap.core.runtime.entity.EntityStateEvent;
import org.polymap.core.runtime.entity.EntityStateEvent.EventType;
import org.polymap.core.runtime.event.Event;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

/**
 * Decorates {@link ILayer} according their buffer state.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class LayerBufferDecorator
        extends BaseLabelProvider
        implements ILightweightLabelDecorator {

    private static Log log = LogFactory.getLog( LayerBufferDecorator.class );

    public static final Point       DEFAULT_SIZE = new Point( 17, 16 );

    public static final int         TOP_LEFT = 0;
    public static final int         TOP_RIGHT = 1;
    public static final int         BOTTOM_LEFT = 2;
    public static final int         BOTTOM_RIGHT = 3;

    private static final String     OUTGOING = "icons/ovr16/outgo_synch3.gif";    
    private static final String     INCOMING = "icons/ovr16/incom_synch.gif";    
    private static final String     CONFLICT = "icons/ovr16/conf_synch.gif";    

    /** The ids of the decorated layers. */
    private Map<String,ILayer>      decorated;

    private Map<String,ILayer>      modified;
    
    public LayerBufferDecorator() {
        decorated = new MapMaker().weakValues().initialCapacity( 128 ).makeMap();
        modified = new MapMaker().weakValues().initialCapacity( 128 ).makeMap();
        
        EventManager.instance().subscribe( this, new EventFilter<EventObject>() {
            public boolean apply( EventObject ev ) {
                if (ev instanceof FeatureChangeEvent) {
                    FeatureChangeEvent fev = (FeatureChangeEvent)ev;                    
                    return decorated.containsKey( fev.getSource().id() );
                }
                else if (ev instanceof EntityStateEvent) {
                    return ((EntityStateEvent)ev).getEventType() == EventType.COMMIT;
                }
                return false;
            }
        });
    }

    
    public void dispose() {
        EventManager.instance().unsubscribe( this );
        FeatureStateTracker.instance().removeFeatureListener( this );
        decorated.clear();
        modified.clear();
    }

    
    @EventHandler(delay=2000,display=true)
    protected void featureChanges( List<FeatureChangeEvent> events ) {
        for (FeatureChangeEvent ev : events) {
            if (ev.getType() == Type.FLUSHED) {
                modified.remove( ev.getSource().id() );
            }
            else {
                modified.put( ev.getSource().id(), ev.getSource() );
            }
        }
        fireLabelProviderChanged( new LabelProviderChangedEvent( LayerBufferDecorator.this ) );
    }

    
    @EventHandler(delay=2000,display=true,scope=Event.Scope.JVM)
    protected void featureStored( List<EntityStateEvent> events ) {
        for (EntityStateEvent ev : events ) {
            if (ev.isMySession()) {
                modified.clear();
            }
        }
        fireLabelProviderChanged( new LabelProviderChangedEvent( LayerBufferDecorator.this ) );
    }

    
    public void decorate( Object elm, IDecoration decoration ) {
        if (elm instanceof ILayer) {
            ILayer layer = (ILayer)elm;

            try {
                layer.id();
            }
            catch (NoSuchEntityException e) {
                // handled by EntityModificationDecorator
                return;
            }

            try {
                boolean outgoing = modified.containsKey( layer.id() );
                
                EntityHandle layerHandle = FeatureStateTracker.layerHandle( layer );
                boolean incoming = false;
//                boolean incoming = EntityStateTracker.instance().isConflicting( 
//                        layerHandle, layerBuffer.getLayerTimestamp() );
                
                if (outgoing && incoming) {
                    decoration.addPrefix( "# " );                    
                }
                else if (incoming) {
                    decoration.addPrefix( "< " );                    
                }
                else if (outgoing) {
                    ImageDescriptor ovr = DataPlugin.imageDescriptorFromPlugin( DataPlugin.PLUGIN_ID, OUTGOING );
                    decoration.addOverlay( ovr, BOTTOM_RIGHT );
                    decoration.addPrefix( "> " );
                }
            }
            catch (Exception e) {
                log.warn( "", e );
                // XXX add question mark overlay
            }

            decorated.put( layer.id(), layer );
        }
    }

}
