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

package test.uk.co.modularaudio.mads.visualisation.base.genericsetup;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.miginfocom.swing.MigLayout;

import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.service.gui.impl.guirackpanel.GuiRackPanel;
import uk.co.modularaudio.service.guicompfactory.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.guicompfactory.GuiComponentFactoryService;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madcomponentui.MadComponentUiService;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.spring.PostInitPreShutdownContextHelper;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;
import uk.co.modularaudio.util.table.Span;

public class GenericComponentVisualiser
{
//	private static Log log = LogFactory.getLog( GenericComponentVisualiser.class.getName() );

	private GenericApplicationContext gac;
	public MadComponentService componentService;
	public MadComponentUiService componentUiService;
	public GuiComponentFactoryService guiComponentFactoryService;

	//	public final static Color panelBackgroundColor = new Color( 0.3f, 0.1f, 0.1f );
	//	public final static Color panelBackgroundColor = new Color( 0.25f, 0.25f, 0.25f );
	public final static Color panelBackgroundColor = new Color( 57, 63, 63 );

	public final static boolean USE_LAF = false;

	public GenericComponentVisualiser() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		if( USE_LAF )
		{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			UIManager.put( "Slider.paintValue", Boolean.FALSE );
		}
	}

	public void setUp() throws Exception
	{
		final List<SpringContextHelper> clientHelpers = new ArrayList<SpringContextHelper>();
		final PostInitPreShutdownContextHelper pipsch = new PostInitPreShutdownContextHelper();
		clientHelpers.add( pipsch );
		final SpringComponentHelper sch = new SpringComponentHelper( clientHelpers );
		gac = sch.makeAppContext( "/componentvisualisationbeans.xml", "/componentvisualisation.properties" );
		componentService = gac.getBean( MadComponentService.class );
		componentUiService = gac.getBean( MadComponentUiService.class );
		guiComponentFactoryService = gac.getBean( GuiComponentFactoryService.class );
	}

	public void tearDown() throws Exception
	{
		if( gac != null )
		{
			gac.close();
		}
	}

	public void testAndShowComponent( final String definitionId )
			throws Exception
	{
		final MadDefinition<?,?> compressorDef = componentService.findDefinitionById( definitionId );
		final Map<MadParameterDefinition, String> parameterValues = new HashMap<MadParameterDefinition, String>();
		final String instanceName = "panel_test";
		final MadInstance<?,?> aui = componentService.createInstanceFromDefinition( compressorDef, parameterValues, instanceName );

		final IMadUiInstance<?,?> auui = componentUiService.createUiInstanceForInstance( aui );

		final JFrame testFrame = new JFrame();
		final JPanel testPanel = new JPanel();
		final MigLayout layout = new MigLayout("insets 10, gap 10, fill");
		testPanel.setLayout( layout );
		testFrame.add( testPanel );
		testPanel.setBackground( panelBackgroundColor );

		final RackComponent rackComponent = new RackComponent( "ComponentyQuickWithLongName", aui, auui );
		final AbstractGuiAudioComponent frontComponent = guiComponentFactoryService.createFrontGuiComponent( rackComponent );
		final AbstractGuiAudioComponent backComponent = guiComponentFactoryService.createBackGuiComponent( rackComponent );

		final Span cellSpan = auui.getCellSpan();
		final Dimension gridSize = GuiRackPanel.FRONT_GRID_SIZE;
		final int width = cellSpan.x * gridSize.width;
		final int height = cellSpan.y * gridSize.height;
		final Dimension componentSize = new Dimension( width, height );
		frontComponent.setSize( componentSize );
		frontComponent.setMinimumSize( componentSize );
		backComponent.setSize( componentSize );
		backComponent.setMinimumSize( componentSize );
		testPanel.add( frontComponent, "grow, wrap" );
		testPanel.add( backComponent, "grow");

		testFrame.addComponentListener( new ComponentListener()
		{

			@Override
			public void componentShown( final ComponentEvent e )
			{
			}

			@Override
			public void componentResized( final ComponentEvent e )
			{
//				final Object o = e.getSource();
//				final JFrame frame = (JFrame)o;
//				log.debug("Component resized to be " + frame.getSize() );
			}

			@Override
			public void componentMoved( final ComponentEvent e )
			{
			}

			@Override
			public void componentHidden( final ComponentEvent e )
			{
			}
		} );

		testPanel.validate();

		testFrame.pack();
		testFrame.setVisible( true );

		while( testFrame.isVisible() )
		{
			Thread.sleep( 100 );
		}
		testFrame.dispose();
		auui.destroy();
		componentUiService.destroyUiInstance(auui);
		componentService.destroyInstance(aui);
	}

}
