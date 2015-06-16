/**
 *
 * Copyright (C) 2015 - Daniel Hams, Modular Audio Limited
 *                      daniel.hams@gmail.com
 *
 * Mad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Mad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Mad.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.co.modularaudio.componentdesigner;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.componentdesigner.mainframe.ExitSignalReceiver;
import uk.co.modularaudio.componentdesigner.mainframe.MainFrame;
import uk.co.modularaudio.componentdesigner.mainframe.MainFrameActions;
import uk.co.modularaudio.componentdesigner.mainframe.MainFrameManipulator;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesActions;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesDialog;
import uk.co.modularaudio.componentdesigner.preferences.PreferencesManipulator;
import uk.co.modularaudio.componentdesigner.profiling.ProfilingWindow;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.imagefactory.ComponentImageFactory;
import uk.co.modularaudio.util.audio.fft.JTransformsConfigurator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.spring.PostInitPreShutdownContextHelper;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;
import uk.co.modularaudio.util.springhibernate.SpringHibernateContextHelper;
import uk.co.modularaudio.util.swing.general.FontResetter;
import uk.co.modularaudio.util.thread.ThreadUtils;
import uk.co.modularaudio.util.thread.ThreadUtils.MAThreadPriority;

public class ComponentDesigner implements ExitSignalReceiver
{
	private static Log log = LogFactory.getLog( ComponentDesigner.class.getName() );

	private static final String BEANS_RESOURCE_PATH = "/cdbeans.xml";
	private static String configResourcePath = "/cdconfiguration.properties";
	private static final String PLUGIN_BEANS_RESOURCE_PATH = "/pluginbeans.xml";
	private static final String PLUGIN_CONFIG_RESOURCE_PATH = "/pluginconfiguration.properties";

	// Gui bits
	private MainFrame mainFrame;
	private MainFrameActions mainFrameActions;
	@SuppressWarnings("unused")
	private MainFrameManipulator mainFrameManipulator;

	private PreferencesDialog preferencesDialog;
	private PreferencesActions preferencesActions;
	@SuppressWarnings("unused")
	private PreferencesManipulator preferencesManipulator;

	// Spring components
	private SpringComponentHelper sch;
	private GenericApplicationContext gac;
	private ComponentDesignerFrontController componentDesignerFrontController;
	private ConfigurationService configurationService;
	private ComponentImageFactory componentImageFactory;

	private ProfilingWindow profilingWindow;

	public ComponentDesigner()
	{
	}

	public void init( final boolean showAlpha, final boolean showBeta,
			final String additionalBeansResource, final String additionalConfigResource ) throws DatastoreException
	{
		// Setup the application context and get the necessary references to the gui controller
		setupApplicationContext( showAlpha, showBeta, additionalBeansResource, additionalConfigResource );

		mainFrame = new MainFrame();

		preferencesDialog = new PreferencesDialog( componentDesignerFrontController, mainFrame );

		profilingWindow = new ProfilingWindow( componentDesignerFrontController );

		mainFrameActions = new MainFrameActions( this, componentDesignerFrontController,
				mainFrame,
				preferencesDialog,
				profilingWindow,
				configurationService );

		mainFrameManipulator = new MainFrameManipulator( componentDesignerFrontController, componentImageFactory, configurationService, mainFrame, mainFrameActions );
		preferencesActions = new PreferencesActions( this, componentDesignerFrontController, preferencesDialog, configurationService );
		preferencesManipulator = new PreferencesManipulator( componentDesignerFrontController, componentImageFactory, configurationService, preferencesDialog, preferencesActions );

	}

	public void setupApplicationContext( final boolean showAlpha , final boolean showBeta,
			final String additionalBeansResource, final String additionalConfigResource )
		throws DatastoreException
	{
		try
		{
			// We will be using postInit preShutdown calls to setup things we need after all the spring components are there
			final List<SpringContextHelper> contextHelperList = new ArrayList<SpringContextHelper>();
			contextHelperList.add( new PostRefreshSetMadReleaseLevelContextHelper( showAlpha, showBeta ) );
			contextHelperList.add( new PostInitPreShutdownContextHelper() );
			contextHelperList.add( new SpringHibernateContextHelper() );
			sch = new SpringComponentHelper( contextHelperList );
			String[] additionalBeansResources = null;
			if( additionalBeansResource != null )
			{
				additionalBeansResources = new String[1];
				additionalBeansResources[0] = additionalBeansResource;
			}
			String[] additionalConfigResources = null;
			if( additionalConfigResource != null )
			{
				additionalConfigResources = new String[1];
				additionalConfigResources[0] = additionalConfigResource;
			}
			gac = sch.makeAppContext( BEANS_RESOURCE_PATH, configResourcePath, additionalBeansResources, additionalConfigResources );
			componentDesignerFrontController = gac.getBean( ComponentDesignerFrontController.class );
			componentImageFactory = gac.getBean( ComponentImageFactory.class );
			configurationService = gac.getBean( ConfigurationService.class );
		}
		catch(final Exception e)
		{
			final String msg = "Exception caught setting up spring context: " + e.toString();
			log.error( msg, e );
			throw new DatastoreException( msg, e );
		}
	}

	public void destroyApplicationContext()
	{
		sch.destroyAppContext( gac );
	}

	public void go()
	{
		mainFrame.setVisible( true );
		final Action checkAudioConfigurationAction = mainFrameActions.getCheckAudioConfigurationAction();
		final ActionEvent tmpActionEvent = new ActionEvent( this, 1, "blah");
		checkAudioConfigurationAction.actionPerformed( tmpActionEvent );
	}

	public void registerCloseAction() throws DatastoreException
	{
		mainFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener( new WindowListener()
		{
			@Override
			public void windowClosed(final WindowEvent e)
			{
				log.debug("Window closed event received");
			}

			@Override
			public void windowOpened(final WindowEvent e)
			{
				try
				{
					log.debug("Window opening event received - setting thread to lowest priority.");
					ThreadUtils.setCurrentThreadPriority( MAThreadPriority.APPLICATION );
					if( log.isDebugEnabled() )
					{
						log.debug("Now set to " + MAThreadPriority.APPLICATION );
					}
				}
				catch ( final Exception ie)
				{
					final String msg = "Exception caught setting gui thread priority: " + ie.toString();
					log.error( msg, ie );
				}
			}
			@Override
			public void windowClosing(final WindowEvent e)
			{
				log.debug("Window closing event received.");
				final Action exitAction = mainFrameActions.getExitAction();
				final ActionEvent exitActionEvent = new ActionEvent( e.getSource(), e.getID(), "");
				exitAction.actionPerformed( exitActionEvent );
			}
			@Override
			public void windowIconified(final WindowEvent e) {}
			@Override
			public void windowDeiconified(final WindowEvent e) {}
			@Override
			public void windowActivated(final WindowEvent e) {}
			@Override
			public void windowDeactivated(final WindowEvent e) {}
		});
	}

	@Override
	public void signalPreExit()
	{
	}

	@Override
	public void signalPostExit()
	{
		preferencesDialog.setVisible( false );
		preferencesDialog.dispose();
		preferencesDialog = null;
		preferencesActions = null;
		preferencesManipulator = null;

		mainFrame.setVisible( false );
		mainFrame.dispose();
		mainFrame = null;
		mainFrameActions = null;
		mainFrameManipulator = null;
		destroyApplicationContext();
		log.debug("signalExit() terminating.");
		// Not needed as swing properly terminates.
//		System.exit( 0 );
	}

	public static void main(final String[] args) throws Exception
	{
		boolean useSystemLookAndFeel = false;

		boolean showAlpha = false;
		boolean showBeta = false;
		String additionalBeansResource = null;
		String additionalConfigResource = null;

		if( args.length > 0 )
		{
			for( int i = 0 ; i < args.length ; ++i )
			{
				final String arg = args[i];
				if( arg.equals("--useSlaf") )
				{
					useSystemLookAndFeel = true;
				}
				else if( arg.equals("--beta") )
				{
					showBeta = true;
				}
				else if( arg.equals("--alpha") )
				{
					showAlpha = true;
					showBeta = true;
				}
				else if( arg.equals("--pluginJar") )
				{
					additionalBeansResource = PLUGIN_BEANS_RESOURCE_PATH;
					additionalConfigResource = PLUGIN_CONFIG_RESOURCE_PATH;

					if( log.isDebugEnabled() )
					{
						log.debug( "Will append plugin beans: " + additionalBeansResource );
						log.debug( "Will append plugin config file: " + additionalConfigResource );
					}
				}
				else if( arg.equals( "--development") )
				{
					// Let me specify certain things with hard paths
					configResourcePath = "/cddevelopment.properties";
					log.info("In development mode. Will use /cddevelopment.properties for configuration");
				}
				else if( arg.equals( "--jprofiler") )
				{
					configResourcePath = "/cdjprofiler.properties";
					log.info("In jprofiler mode - using absolute paths from /cdjprofiler.properties for configuration");
				}
			}
			if( useSystemLookAndFeel )
			{
				log.info( "System look and feel activated" );
			}
			if( showAlpha )
			{
				log.info("Showing alpha components");
			}
			if( showBeta )
			{
				log.info("Showing beta components");
			}
		}

		if( useSystemLookAndFeel )
		{
			final String gtkLookAndFeelClassName = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
			boolean foundGtkLaf = false;

			final LookAndFeelInfo lafis[] = UIManager.getInstalledLookAndFeels();

			for( final LookAndFeelInfo lafi : lafis )
			{
				final String lc = lafi.getClassName();
				if( lc.equals( gtkLookAndFeelClassName ) )
				{
					foundGtkLaf = true;
					break;
				}
			}

			if( foundGtkLaf )
			{
				log.debug("Found available GTK laf. Will set active");
				UIManager.setLookAndFeel( gtkLookAndFeelClassName );
			}
			UIManager.put( "Slider.paintValue",  Boolean.FALSE );
		}

		final Font f = Font.decode( "" );
		final String fontName = f.getName();
		FontResetter.setUIFontFromString( fontName, Font.PLAIN, 10 );

		log.info( "ComponentDesigner starting.");
		// Set the fft library to only use current thread
		JTransformsConfigurator.setThreadsToOne();

		final ComponentDesigner application = new ComponentDesigner();
		application.init( showAlpha, showBeta, additionalBeansResource, additionalConfigResource );

		SwingUtilities.invokeLater( new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					application.go();
					application.registerCloseAction();
				}
				catch (final Exception e)
				{
					final String msg = "Exception caught at top level of ComponentDesigner launch: " + e.toString();
					log.error( msg, e );
					System.exit(0);
				}
				log.debug("Leaving runnable run section.");
			}
		});
	}

	public GenericApplicationContext getApplicationContext()
	{
		return gac;
	}

}
