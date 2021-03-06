package org.polymap.core.data.refine;

import static org.junit.Assert.assertEquals;

import java.util.List;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;
import com.google.refine.model.ColumnModel;
import com.google.refine.model.Row;

import org.polymap.core.data.refine.impl.CSVFormatAndOptions;
import org.polymap.core.data.refine.impl.ImportResponse;
import org.polymap.core.data.refine.impl.RefineServiceImpl;

public class OperationsTest {

    private RefineServiceImpl service;


    @Before
    public void setUp() throws IOException {
        service = RefineServiceImpl.INSTANCE( java.nio.file.Files.createTempDirectory( "refine" ) );
    }


    @Test
    public void testSSV() throws Exception {
        // ; separated file
        File wohngebiete = new File(
                this.getClass().getResource( "/data/wohngebiete_sachsen.csv" ).getFile() );
        File tmp = File.createTempFile( "foo", ".csv" );
        Files.copy( wohngebiete, tmp );
        ImportResponse<CSVFormatAndOptions> response = service.importFile( tmp,
                CSVFormatAndOptions.createDefault(), null );
        assertEquals( ";", response.options().separator() );
        assertEquals( "ISO-8859-1", response.options().encoding() );

        // get the loaded models
        ColumnModel columns = response.job().project.columnModel;
        assertEquals( 12, columns.columns.size() );

        List<Row> rows = response.job().project.rows;
        assertEquals( "Baugenehmigungen: Neue Wohn-u.Nichtwohngeb. einschl. Wohnh.,",
                rows.get( 0 ).cells.get( 0 ).value );
        assertEquals( 471, rows.size() );
        assertEquals( "neue Wohngeb. mit 1 od.2 Wohnungen, Räume u.Fläche d.Wohn.,",
                rows.get( 1 ).cells.get( 0 ).value );

        CSVFormatAndOptions options = response.options();
        options.setSeparator( "\\t" );
        service.updateOptions( response.job(), options, null );

        // Map<String, String> params = Maps.newHashMap();
        // String columnToRemove = columns.columns.get( 2 ).getName();
        // params.put( "columnName", columnToRemove );
        // params.put( "project", "" + response.job().project.id );
        // service.post( ReorderRowsCommand.class, params );
        //
        // columns = response.job().project.columnModel;
        // assertEquals( 11, columns.columns.size() );
    }
}
