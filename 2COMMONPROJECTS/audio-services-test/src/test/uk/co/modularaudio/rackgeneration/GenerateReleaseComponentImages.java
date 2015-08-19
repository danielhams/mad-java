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

package test.uk.co.modularaudio.rackgeneration;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.service.gui.impl.guirackpanel.GuiRackPanel;
import uk.co.modularaudio.service.guicompfactory.AbstractGuiAudioComponent;
import uk.co.modularaudio.service.guicompfactory.GuiComponentFactoryService;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madcomponentui.MadComponentUiService;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.audio.mad.MadInstance;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.spring.PostInitPreShutdownContextHelper;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;
import uk.co.modularaudio.util.table.Span;

public class GenerateReleaseComponentImages
{
	private static Log log = LogFactory.getLog( GenerateReleaseComponentImages.class.getName() );

	private final GenericApplicationContext gac;

	private final MadComponentService componentService;
	private final MadComponentUiService componentUiService;

	private final GuiComponentFactoryService guiComponentFactoryService;

	public GenerateReleaseComponentImages() throws DatastoreException
	{
		final List<SpringContextHelper> clientHelpers = new ArrayList<SpringContextHelper>();
		final PostInitPreShutdownContextHelper pipsch = new PostInitPreShutdownContextHelper();
		clientHelpers.add( pipsch );
		final SpringComponentHelper sch = new SpringComponentHelper( clientHelpers );
		gac = sch.makeAppContext( "/componentvisualisationbeans.xml", "/componentvisualisation.properties" );
		componentService = gac.getBean( MadComponentService.class );
		componentService.setReleaseLevel( true, true );
		componentUiService = gac.getBean( MadComponentUiService.class );
		guiComponentFactoryService = gac.getBean( GuiComponentFactoryService.class );
	}

	public void generateRack() throws Exception
	{
		final ArrayList<MadDefinition<?, ?>> defsToAdd = new ArrayList<MadDefinition<?,?>>();

		// Create a list of the mad definitions we'll put into the rack
		// so we can compute the necessary size of it.
		final MadDefinitionListModel madDefinitions = componentService.listDefinitionsAvailable();
		final int numMads = madDefinitions.getSize();

		for( int i = 0 ; i < numMads ; ++i )
		{
			final MadDefinition<?, ?> def = madDefinitions.getElementAt( i );
			if( def.getClassification().getState() == ReleaseState.RELEASED )
			{
				defsToAdd.add( def );
			}
		}

		log.info("Generating " + defsToAdd.size() + " images of components");

		for( final MadDefinition<?, ?> def : defsToAdd )
		{
			generateImageForDefinition( def );
		}

	}

	private void generateImageForDefinition( final MadDefinition<?, ?> def ) throws DatastoreException, RecordNotFoundException, MadProcessingException, IOException
	{
		final Map<MadParameterDefinition, String> emptyParams = new HashMap<MadParameterDefinition, String>();
		final MadInstance<?,?> aui = componentService.createInstanceFromDefinition( def, emptyParams, def.getName() );

		final IMadUiInstance<?,?> auui = componentUiService.createUiInstanceForInstance( aui );

		final RackComponent rackComponent = new RackComponent( def.getName(), aui, auui );
		final AbstractGuiAudioComponent frontComponent = guiComponentFactoryService.createFrontGuiComponent( rackComponent );

		final JFrame testFrame = new JFrame();
		final JPanel testPanel = new JPanel();
		final MigLayout layout = new MigLayout("insets 0, gap 0, fill");
		testPanel.setLayout( layout );
		testPanel.setOpaque( false );

		final Span cellSpan = auui.getCellSpan();
		final Dimension gridSize = GuiRackPanel.FRONT_GRID_SIZE;
		final int width = cellSpan.x * gridSize.width;
		final int height = cellSpan.y * gridSize.height;
		final Dimension componentSize = new Dimension( width, height );

		final BufferedImage bi = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		final Graphics2D g2d = bi.createGraphics();

		frontComponent.setBounds( new Rectangle( 0, 0, width, height ) );
		frontComponent.setSize( componentSize );
		frontComponent.setMinimumSize( componentSize );

		testPanel.add( frontComponent );
		testFrame.add( testPanel );

		testPanel.validate();
		testFrame.pack();

		testFrame.setVisible( true );
		try
		{
			Thread.sleep( 100 );
		}
		catch( final InterruptedException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		testPanel.paint( g2d );
		testFrame.setVisible( false );
		testFrame.dispose();

		final File defOutputFile = new File( "tmpoutput/" + def.getId() + ".png" );
		ImageIO.write( bi, "png", defOutputFile );

		componentUiService.destroyUiInstance( auui );
		componentService.destroyInstance( aui );
	}

	public void close()
	{
		gac.close();
	}

	public static void main( final String[] args ) throws Exception
	{
		final GenerateReleaseComponentImages grr = new GenerateReleaseComponentImages();
		grr.generateRack();

		grr.close();
	}

}
