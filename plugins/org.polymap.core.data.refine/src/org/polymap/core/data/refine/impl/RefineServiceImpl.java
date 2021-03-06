package org.polymap.core.data.refine.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.activation.MimetypesFileTypeMap;

import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Maps;
import com.google.common.net.HttpHeaders;
import com.google.refine.ProjectManager;
import com.google.refine.RefineServlet;
import com.google.refine.commands.Command;
import com.google.refine.commands.importing.ImportingControllerCommand;
import com.google.refine.importing.ImportingJob;
import com.google.refine.importing.ImportingManager;
import com.google.refine.importing.ImportingUtilities;
import com.google.refine.io.FileProjectManager;
import com.google.refine.model.Project;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.data.refine.Messages;
import org.polymap.core.data.refine.RefineService;
import org.polymap.core.data.refine.json.JSONUtil;

public class RefineServiceImpl
        implements RefineService {

    private static Log                            log              = LogFactory
            .getLog( RefineServiceImpl.class );

    private static RefineServiceImpl              refineService;

    static final String                           PARAM_BASEDIR    = "param_basedir";

    private static final long                     AUTOSAVE_PERIOD  = 5;                    // 5
    // minutes

    private final String                          VERSION          = "2.6";

    private final String                          REVISION         = "polymap 1";

    private final String                          FULLNAME         = RefineServlet.FULLNAME
            + VERSION + " [" + REVISION + "]";

    private Map<Class<? extends Command>,Command> commandInstances = Maps.newHashMap();

    // timer for periodically saving projects
    static private ScheduledExecutorService       service          = Executors
            .newSingleThreadScheduledExecutor();


    private RefineServiceImpl( final Path baseDir ) {

        log.info( "Starting " + FULLNAME + "..." );

        log.trace( "> initialize" );
        FileProjectManager.initialize( new File(baseDir.toFile(), "workspace" ) );
        // only used to call servlet.getTempDir() later on
        ImportingManagerRegistry.initialize( new RefineServlet() {

            @Override
            public File getTempDir() {
                return new File( baseDir.toFile(), "tmp" );
            }
        } );

        service.scheduleWithFixedDelay( () -> {
            ProjectManager.singleton.save( false );
        }, AUTOSAVE_PERIOD, AUTOSAVE_PERIOD, TimeUnit.MINUTES );

        log.trace( "< initialize" );
    }
    //
    // @SuppressWarnings("unchecked")
    // protected void activate(ComponentContext context) {
    // Dictionary<String, Object> properties = context.getProperties();
    // File baseDir = (File) properties.get(PARAM_BASEDIR);
    //
    // log.info("activated at " + baseDir.getAbsolutePath());
    // }


    public static RefineServiceImpl INSTANCE( Path refineDir ) {
        if (refineService == null) {
            refineService = new RefineServiceImpl( refineDir );
        }
        return refineService;
    }


    public void destroy() {
        log.trace( "> destroy" );

        // cancel automatic periodic saving and force a complete save.
        // if (_timer != null) {
        // _timer.cancel();
        // _timer = null;
        // }
        if (ProjectManager.singleton != null) {
            ProjectManager.singleton.dispose();
            ProjectManager.singleton = null;
        }

        log.trace( "< destroy" );
    }


    // public Object get( Class<? extends Command> commandClazz ) {
    // try {
    // Command command = command( commandClazz );
    // RefineResponse response = createResponse();
    // command.doGet( createRequest( null ), response );
    // return response.result();
    // }
    // catch (Exception e) {
    // throw new RuntimeException( e );
    // }
    // }
    //
    //
    // public Object post( Class<? extends Command> commandClazz ) {
    // try {
    // Command command = command( commandClazz );
    // RefineResponse response = createResponse();
    // command.doPost( createRequest( null ), response );
    // return response.result();
    // }
    // catch (Exception e) {
    // throw new RuntimeException( e );
    // }
    // }

    public Object post( Class<? extends Command> commandClazz, Map<String,String> params ) {
        try {
            Command command = command( commandClazz );
            RefineResponse response = createResponse();
            command.doPost( createRequest( params ), response );
            return response.result();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    private RefineResponse createResponse() {
        return new RefineResponse();
    }


    private RefineRequest createRequest( Map<String,String> params ) {
        return new RefineRequest( params, null );
    }


    private RefineRequest createRequest( Map<String,String> params, Map<String,String> headers,
            InputStream file, String fileName ) {
        return new RefineRequest( params, headers, file, fileName );
    }


    private Command command( Class<? extends Command> commandClazz )
            throws InstantiationException, IllegalAccessException {
        Command command = commandInstances.get( commandClazz );
        if (command == null) {
            command = commandClazz.newInstance();
            commandInstances.put( commandClazz, command );
        }
        return command;
    }


    private <T extends FormatAndOptions> ImportResponse<T> importData( RefineRequest request,
            T options, IProgressMonitor monitor ) {
        try {
            // create the job
            ImportingJob job = ImportingManager.createJob();

            // copy the file
            // Map<String,String> params = Maps.newHashMap();
            request.setParameter( "jobID", String.valueOf( job.id ) );
            request.setParameter( "subCommand", "load-raw-data" );
            request.setParameter( "controller", "core/filebased-importing-controller" );
            // Map<String,String> headers = Maps.newHashMap();
            RefineResponse response = createResponse();
            command( ImportingControllerCommand.class ).doPost( request, response );

            // get-importing-job-status bis done

            log.info( "JOB:" + job.getOrCreateDefaultConfig() );

            // initialize the parser ui
            String format = JSONUtil.getString( job.getOrCreateDefaultConfig(),
                    "retrievalRecord.files[0].format", null );

            // TODO wait monitor here
            while (format == null) {
                Thread.sleep( 100 );
                format = JSONUtil.getString( job.getOrCreateDefaultConfig(),
                        "retrievalRecord.files[0].format", null );
            }

            Map<String,String> params = Maps.newHashMap();
            params.put( "jobID", String.valueOf( job.id ) );
            params.put( "subCommand", "initialize-parser-ui" );
            params.put( "controller", "core/default-importing-controller" );
            params.put( "format", format );
            command( ImportingControllerCommand.class ).doPost( createRequest( params ), response );
            JSONObject initializeParserUiResponse = new JSONObject( response.result().toString() );
            options.putAll( initializeParserUiResponse.getJSONObject( "options" ) );
            options.setFormat( format );

            // try to find the best separator, with the most columns in the
            // resulting model.
            params.clear();
            params.put( "jobID", String.valueOf( job.id ) );
            params.put( "subCommand", "update-format-and-options" );
            params.put( "controller", "core/default-importing-controller" );
            params.put( "format", format );
            params.put( "options", options.toString() );
            command( ImportingControllerCommand.class ).doPost( createRequest( params ), response );

            log.info( "imported " + job + "; " + options.store() );
            ImportResponse<T> resp = new ImportResponse<T>();
            resp.setJob( job );
            resp.setOptions( options );

            return resp;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public <T extends FormatAndOptions> ImportResponse<T> importFile( File file, T options,
            IProgressMonitor monitor ) {
        Map<String,String> params = Maps.newHashMap();
        params.put( HttpHeaders.CONTENT_TYPE,
                MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType( file ) );
        return importData( new RefineRequest( params, Maps.newHashMap(), file, file.getName() ),
                options, monitor );
    }


    @Override
    public <T extends FormatAndOptions> ImportResponse<T> importStream( InputStream in,
            String fileName, String mimeType, T options, IProgressMonitor monitor ) {
        Map<String,String> params = Maps.newHashMap();
        params.put( HttpHeaders.CONTENT_TYPE, mimeType );
        return importData( new RefineRequest( params, Maps.newHashMap(), in, fileName ), options,
                monitor );
    }


    @Override
    public void updateOptions( ImportingJob job, FormatAndOptions options,
            IProgressMonitor monitor ) {
        try {
            if (monitor != null) {
                monitor.beginTask( Messages.get( "start.updateOptions" ), 100 );
            }
            log.info( "updating job " + options );
            RefineResponse response = createResponse();
            Map<String,String> params = Maps.newHashMap();
            params.put( "jobID", String.valueOf( job.id ) );
            params.put( "subCommand", "update-format-and-options" );
            params.put( "controller", "core/default-importing-controller" );
            params.put( "format", options.format() );
            params.put( "options", options.toString() );
            command( ImportingControllerCommand.class ).doPost( createRequest( params ), response );

            while (job.updating) {

                log.info( "updating: " + job.getOrCreateDefaultConfig() );
                try {
                    Thread.currentThread().sleep( 10 );
                }
                catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            log.info( "updated job " + job.getOrCreateDefaultConfig() );
            if (monitor != null) {
                monitor.done();
            }
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public Project createProject( ImportingJob job, FormatAndOptions options,
            IProgressMonitor monitor ) {
        // SubMonitor subMonitor = SubMonitor.on(monitor, 1);
        // TODO wieso?
        // subMonitor.updateDelayCount.set(1);
        if (monitor != null) {
            monitor.beginTask( Messages.get( "start.createProject" ), 100 );
        }

        String projectName = "" + System.currentTimeMillis();
        options.put( "projectName", projectName );
        List<Exception> exceptions = new LinkedList<Exception>();

        job.updating = true;
        ImportingUtilities.createProject( job, options.format(), options.store(), exceptions,
                false );
        int work = 1;
        while (job.updating) {
            // log.info("updating: " + job.getOrCreateDefaultConfig());
            JSONObject progress = (JSONObject)job.getOrCreateDefaultConfig().get( "progress" );
            log.info( "updating progress: " + progress );
            if (monitor != null) {
                if (progress != null) {
                    monitor.worked( progress.getInt( "percent" ) );
                }
                else {
                    monitor.worked( work++ );
                }
            }
            try {
                Thread.currentThread().sleep( 100 );
            }
            catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // cancel the job
        ImportingManager.disposeJob( job.id );
        if (monitor != null) {
            monitor.done();
        }
        long projectId = ProjectManager.singleton.getProjectID( projectName );
        return ProjectManager.singleton.getProject( projectId );
    }

}
