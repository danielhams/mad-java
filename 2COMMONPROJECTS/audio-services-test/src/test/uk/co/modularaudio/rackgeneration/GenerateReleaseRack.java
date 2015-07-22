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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.GenericApplicationContext;

import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madcomponentui.MadComponentUiService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.rackmarshalling.RackMarshallingService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadClassification.ReleaseState;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.spring.PostInitPreShutdownContextHelper;
import uk.co.modularaudio.util.spring.SpringComponentHelper;
import uk.co.modularaudio.util.spring.SpringContextHelper;
import uk.co.modularaudio.util.table.Span;

public class GenerateReleaseRack
{
	private static Log log = LogFactory.getLog( GenerateReleaseRack.class.getName() );

	private final GenericApplicationContext gac;

	private final MadComponentService componentService;
	private final MadComponentUiService componentUiService;

	private final RackService rackService;
	private final RackMarshallingService rackMarshallingService;

	public GenerateReleaseRack() throws DatastoreException
	{
		final List<SpringContextHelper> clientHelpers = new ArrayList<SpringContextHelper>();
		final PostInitPreShutdownContextHelper pipsch = new PostInitPreShutdownContextHelper();
		clientHelpers.add( pipsch );
		final SpringComponentHelper sch = new SpringComponentHelper( clientHelpers );
		gac = sch.makeAppContext( "/componentvisualisationbeans.xml", "/componentvisualisation.properties" );
		componentService = gac.getBean( MadComponentService.class );
		componentService.setReleaseLevel( true, true );
		componentUiService = gac.getBean( MadComponentUiService.class );
		rackService = gac.getBean( RackService.class );
		rackMarshallingService = gac.getBean( RackMarshallingService.class );
	}

	public void generateRack() throws Exception
	{
		final ArrayList<MadDefinition<?, ?>> defsToAdd = new ArrayList<MadDefinition<?,?>>();

		// Create a list of the mad definitions we'll put into the rack
		// so we can compute the necessary size of it.
		final MadDefinitionListModel madDefinitions = componentService.listDefinitionsAvailable();
		final int numMads = madDefinitions.getSize();
		int numRowsRequired = 2;
		for( int i = 0 ; i < numMads ; ++i )
		{
			final MadDefinition<?, ?> def = madDefinitions.getElementAt( i );
			if( def.getClassification().getState() == ReleaseState.RELEASED )
			{
				defsToAdd.add( def );

				final Span uiSpan = componentUiService.getUiSpanForDefinition( def );
				numRowsRequired += uiSpan.y;
			}
		}
		log.debug("Have " + defsToAdd.size() + " defs to add to rack requiring " + numRowsRequired + " rows");
		final RackDataModel rdm = rackService.createNewSubRackDataModel( "ReleaseRack", "", RackService.DEFAULT_RACK_COLS, numRowsRequired, true );

		final HashMap<MadParameterDefinition, String> emptyParameterValues = new HashMap<MadParameterDefinition, String>();
		for( final MadDefinition<?, ?> def : defsToAdd )
		{
			rackService.createComponent( rdm, def, emptyParameterValues, def.getName() + " Example" );
		}

		final String outputRackFilename = "tmpoutput/releaserack.xml";
		final File outputRackFile = new File(outputRackFilename);
		final File parentDir = outputRackFile.getParentFile();
		parentDir.mkdirs();
		rackMarshallingService.saveBaseRackToFile( rdm, outputRackFile.getAbsolutePath() );

		rackService.destroyRackDataModel( rdm );
	}

	public void close()
	{
		gac.close();
	}

	public static void main( final String[] args ) throws Exception
	{
		final GenerateReleaseRack grr = new GenerateReleaseRack();
		grr.generateRack();

		grr.close();
	}

}
