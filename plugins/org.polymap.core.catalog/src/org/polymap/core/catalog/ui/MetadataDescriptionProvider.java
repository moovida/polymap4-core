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
package org.polymap.core.catalog.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import org.polymap.core.catalog.IMetadata;
import org.polymap.core.catalog.IMetadataCatalog;
import org.polymap.core.catalog.resolve.IResolvableInfo;
import org.polymap.core.catalog.resolve.IResourceInfo;
import org.polymap.core.catalog.resolve.IServiceInfo;
import org.polymap.core.ui.UIUtils;

/**
 * Default label provider for: {@link IMetadataCatalog}, {@link IMetadata},
 * {@link IServiceInfo} and {@link IResourceInfo} elements.
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class MetadataDescriptionProvider
        extends CellLabelProvider
        implements ILabelProvider {

    private static Log log = LogFactory.getLog( MetadataDescriptionProvider.class );

    @Override
    public void update( ViewerCell cell ) {
        Object elm = cell.getElement();
        cell.setText( getText( elm ) );
        cell.setForeground( UIUtils.getColor( 150, 150, 150 ) );
    }


    @Override
    public String getText( Object elm ) {
        if (elm instanceof IMetadataCatalog) {
            return ((IMetadataCatalog)elm).getDescription();
        }
        else if (elm instanceof IMetadata) {
            return ((IMetadata)elm).getDescription();
        }
        else if (elm instanceof IResolvableInfo) {
            return ((IResolvableInfo)elm).getDescription();
        }
        else {
            throw new RuntimeException( "Unknown element type: " +  elm );
        }
    }


    @Override
    public Image getImage( Object elm ) {
        return null;
    }
    
}
