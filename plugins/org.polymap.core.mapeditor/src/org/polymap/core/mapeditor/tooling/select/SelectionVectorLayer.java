/* 
 * polymap.org
 * Copyright 2012, Falko Br�utigam. All rights reserved.
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
package org.polymap.core.mapeditor.tooling.select;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.mapeditor.tooling.IEditorToolSite;
import org.polymap.core.mapeditor.tooling.edit.BaseVectorLayer;
import org.polymap.core.project.ILayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class SelectionVectorLayer
        extends BaseVectorLayer {

    private static Log log = LogFactory.getLog( SelectionVectorLayer.class );

    public SelectionVectorLayer( IEditorToolSite site, ILayer layer ) {
        super( site, layer );
    }

}
