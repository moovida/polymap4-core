/* 
 * polymap.org
 * Copyright 2011, Polymap GmbH. All rights reserved.
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
package org.polymap.service;

import org.osgi.service.http.HttpService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.preferences.ScopedPreferenceStore;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;

import org.polymap.core.model.event.IModelHandleable;
import org.polymap.core.model.event.IModelStoreListener;
import org.polymap.core.model.event.ModelChangeTracker;
import org.polymap.core.model.event.ModelStoreEvent;
import org.polymap.core.model.event.ModelStoreEvent.EventType;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.DefaultSessionContextProvider;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.SessionContext;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class ServiceContext
        implements IModelStoreListener, IPropertyChangeListener {

    private static Log log = LogFactory.getLog( ServiceContext.class );
    
    private static final DefaultSessionContextProvider contextProvider;
    
    
    static {
        contextProvider = new DefaultSessionContextProvider();
        SessionContext.addProvider( contextProvider );
    }
    
    public static void mapContext( String sessionKey ) {
        contextProvider.mapContext( sessionKey, false );    
    }

    public static void unmapContext() {
        contextProvider.unmapContext();
    }
    
    
    // instance *******************************************
    
    private String                  serviceId;

    private String                  sessionKey;

    private HttpService             httpService;


    ServiceContext( String serviceId, HttpService httpService ) {
        this.serviceId = serviceId;
        this.sessionKey = "service-" + serviceId;
        this.httpService = httpService;
        try {
            contextProvider.mapContext( sessionKey, true );
            Polymap.instance().addPrincipal( new ServicesPlugin.AdminPrincipal() );
        }
        finally {
            contextProvider.unmapContext();
        }
        startService();   
    }
    

    protected IProvidedService findService() {
        for (IProvidedService service : ServiceRepository.instance().allServices()) {
            if (service.id().equals( serviceId )) {
                return service;
            }
        }
        return null;
    }
    
    
    public void startService() {
        boolean needsUnmap = false;
        try {
            needsUnmap = contextProvider.mapContext( sessionKey, true );
            Polymap.instance().addPrincipal( new ServicesPlugin.AdminPrincipal() );
            
            // start service
            IProvidedService service = findService();
            if (service.isEnabled()) {
                try {
                    service.start();
                }
                catch (Exception e) {
                    log.error( "Error while starting services: " + service.getPathSpec(), e );
                }
            }
            
            // listen to global change events of the map and layers
            ModelChangeTracker.instance().addListener( this );

            // listen to preference changes
            final ScopedPreferenceStore prefStore = new ScopedPreferenceStore( 
                    new InstanceScope(), ServicesPlugin.getDefault().getBundle().getSymbolicName() );
            prefStore.addPropertyChangeListener( this );
        }
        finally {
            if (needsUnmap) {
                contextProvider.unmapContext();
            }
        }
    }


    public void stopService() {
        boolean needsUnmap = false;
        try {
            needsUnmap = contextProvider.mapContext( sessionKey, false );
            
            // stop service
            IProvidedService service = findService();
            try {
                service.stop();
            }
            catch (Exception e) {
                log.error( "Error while starting services: " + service.getPathSpec(), e );
            }
            // unregister listener
            ModelChangeTracker.instance().removeListener( this );
        }
        finally {
            if (needsUnmap) {
                contextProvider.unmapContext();
            }
        }
        // destroy session
        contextProvider.destroyContext( sessionKey );
    }

    
    // event handling *************************************

    public void modelChanged( final ModelStoreEvent ev ) {
        if (ev.getEventType() == EventType.COMMIT) {
            
            // the event comes within a Job but with RAP session context (in most cases)
            // so we nee a "clean" Job to be able to map a new session context
            new Job( "Restart Service" ) {
                protected IStatus run( IProgressMonitor monitor ) {
                    boolean needsRestart = false;
                    try {
                        boolean needsUnmap = contextProvider.mapContext( sessionKey, true );
                        assert needsUnmap;

                        IProvidedService service = findService();
                        IMap map = service.getMap();
                        
                        if (ev.hasChanged( (IModelHandleable)service )) {
                            needsRestart = true;
                        }
                        if (ev.hasChanged( (IModelHandleable)map )) {
                            needsRestart = true;
                        }
                        for (ILayer layer : map.getLayers()) {
                            if (ev.hasChanged( (IModelHandleable)layer )) {
                                needsRestart = true;
                            }
                        }
                    }
                    finally {
                        contextProvider.unmapContext();
                    }

                    if (needsRestart) {
                        stopService();
                        startService();
                    }
                    return Status.OK_STATUS;
                }
            }.schedule();
        }
    }
    
    public boolean isValid() {
        return true;
    }
    

    public void propertyChange( PropertyChangeEvent ev ) {
        log.debug( "Preferences changed: " + ev.getProperty() );
        if (ev.getProperty().equals( ServicesPlugin.PREF_PROXY_URL )) {

            // the event comes within a Job but with RAP session context (in most cases)
            // so we nee a "clean" Job to be able to map a new session context
            new Job( "Restart Service" ) {
                protected IStatus run( IProgressMonitor monitor ) {
                    try {
                        boolean needsUnmap = contextProvider.mapContext( sessionKey, true );
                        assert needsUnmap;
                        
                        stopService();
                        startService();
                        return Status.OK_STATUS;
                    }
                    finally {
                        contextProvider.unmapContext();
                    }
                }
            }.schedule();
        }
    }

}
