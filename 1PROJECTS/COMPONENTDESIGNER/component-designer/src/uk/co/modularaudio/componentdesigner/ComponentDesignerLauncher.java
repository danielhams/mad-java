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

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.util.audio.fft.JTransformsConfigurator;
import uk.co.modularaudio.util.swing.general.FontResetter;

public class ComponentDesignerLauncher extends ComponentDesigner
{
	private static Log log = LogFactory.getLog( ComponentDesignerLauncher.class.getName() );

	public static void main(final String[] args) throws Exception
	{
		boolean useSystemLookAndFeel = false;

		boolean showAlpha = false;
		boolean showBeta = false;
		String additionalBeansResource = null;
		String additionalConfigResource = null;

		String configResourcePath = CDCONFIG_PROPERTIES;

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
					configResourcePath = CDDEVELOPMENT_PROPERTIES;
					log.info("In development mode. Will use development properties for configuration");
				}
				else if( arg.equals( "--jprofiler") )
				{
					configResourcePath = CDJPROFILER_PROPERTIES;
					log.info("In jprofiler mode - using jprofiler properties for configuration");
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

		log.debug( "ComponentDesigner starting.");
		// Set the fft library to only use current thread
		JTransformsConfigurator.setThreadsToOne();

		final ComponentDesignerLauncher application = new ComponentDesignerLauncher();
		application.init( configResourcePath, additionalBeansResource, additionalConfigResource, showAlpha, showBeta );

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
}
