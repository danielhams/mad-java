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

package uk.co.modularaudio.controller.rack.impl;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.controller.rack.RackController;
import uk.co.modularaudio.mads.base.mixer.mu.MixerMadDefinition;
import uk.co.modularaudio.service.bufferedimageallocation.BufferedImageAllocationService;
import uk.co.modularaudio.service.configuration.ConfigurationService;
import uk.co.modularaudio.service.configuration.ConfigurationServiceHelper;
import uk.co.modularaudio.service.gui.GuiService;
import uk.co.modularaudio.service.gui.RackModelRenderingComponent;
import uk.co.modularaudio.service.madcomponent.MadComponentService;
import uk.co.modularaudio.service.madcomponentui.MadComponentUiService;
import uk.co.modularaudio.service.rack.RackService;
import uk.co.modularaudio.service.rackmarshalling.RackMarshallingService;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.audio.mad.MadClassification;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup;
import uk.co.modularaudio.util.audio.mad.MadClassificationGroup.Visibility;
import uk.co.modularaudio.util.audio.mad.MadDefinition;
import uk.co.modularaudio.util.audio.mad.MadDefinitionListModel;
import uk.co.modularaudio.util.audio.mad.MadParameterDefinition;
import uk.co.modularaudio.util.audio.mad.graph.MadGraphInstance;
import uk.co.modularaudio.util.bufferedimage.AllocationBufferType;
import uk.co.modularaudio.util.bufferedimage.AllocationLifetime;
import uk.co.modularaudio.util.bufferedimage.AllocationMatch;
import uk.co.modularaudio.util.bufferedimage.TiledBufferedImage;
import uk.co.modularaudio.util.component.ComponentWithLifecycle;
import uk.co.modularaudio.util.component.ComponentWithPostInitPreShutdown;
import uk.co.modularaudio.util.exception.ComponentConfigurationException;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.exception.MAConstraintViolationException;
import uk.co.modularaudio.util.exception.RecordNotFoundException;
import uk.co.modularaudio.util.table.ContentsAlreadyAddedException;
import uk.co.modularaudio.util.table.Span;
import uk.co.modularaudio.util.table.TableCellFullException;
import uk.co.modularaudio.util.table.TableIndexOutOfBoundsException;

public class RackControllerImpl implements ComponentWithLifecycle, ComponentWithPostInitPreShutdown, RackController
{
	private static Log log = LogFactory.getLog( RackControllerImpl.class.getName() );

	private static final String CONFIG_KEY_STARTUP_RACK_PAINTING = RackControllerImpl.class.getSimpleName() + ".StartupRackPainting";

	private RackService rackService;
	private RackMarshallingService rackMarshallingService;
	private ConfigurationService configurationService;
	private MadComponentService componentService;
	private MadComponentUiService componentUiService;
	private GuiService guiService;
	private BufferedImageAllocationService bufferedImageAllocationService;

	private boolean doStartupRackPainting;

	@Override
	public void destroy()
	{
	}

	@Override
	public void init() throws ComponentConfigurationException
	{
		if( rackService == null ||
				rackMarshallingService == null ||
				configurationService == null ||
				componentService == null ||
				componentUiService == null ||
				guiService == null ||
				bufferedImageAllocationService == null )
		{
			throw new ComponentConfigurationException( "RackController is missing service dependencies. Check configuration" );
		}
		final Map<String,String> errors = new HashMap<String,String>();
		doStartupRackPainting = ConfigurationServiceHelper.checkForBooleanKey( configurationService, CONFIG_KEY_STARTUP_RACK_PAINTING, errors );
		ConfigurationServiceHelper.errorCheck( errors );
	}

	@Override
	public void postInit() throws ComponentConfigurationException
	{
		if( doStartupRackPainting )
		{
			log.info("Performing component rack painting");
			try
			{
				paintOneRackPerComponent();
			}
			catch( final DatastoreException | ContentsAlreadyAddedException | TableCellFullException | TableIndexOutOfBoundsException | MAConstraintViolationException | RecordNotFoundException e )
			{
				final String msg = "Exception caught during rack painting: " + e.toString();
				log.error( msg, e );
			}

			log.info("Component rack painting complete");
		}
	}

	@Override
	public void preShutdown()
	{
	}

	public void setRackService(final RackService rackService)
	{
		this.rackService = rackService;
	}

	public void setRackMarshallingService(final RackMarshallingService rackMarshallingService)
	{
		this.rackMarshallingService = rackMarshallingService;
	}

	public void setConfigurationService( final ConfigurationService configurationService )
	{
		this.configurationService = configurationService;
	}

	public void setComponentService( final MadComponentService componentService )
	{
		this.componentService = componentService;
	}

	public void setComponentUiService( final MadComponentUiService componentUiService )
	{
		this.componentUiService = componentUiService;
	}

	public void setGuiService( final GuiService guiService )
	{
		this.guiService = guiService;
	}

	public void setBufferedImageAllocationService( final BufferedImageAllocationService bufferedImageAllocationService )
	{
		this.bufferedImageAllocationService = bufferedImageAllocationService;
	}

	@Override
	public RackDataModel createNewRackDataModel( final String rackName, final String rackPath, final int numCols, final int numRows, final boolean withRackIO ) throws DatastoreException
	{
		return rackService.createNewRackDataModel( rackName, rackPath, numCols, numRows, withRackIO );
	}

	@Override
	public void dumpRack( final RackDataModel rdm )
	{
		rackService.dumpRack( rdm );
	}

	@Override
	public RackDataModel loadRackFromFile(final String filename) throws DatastoreException, IOException
	{
		return rackMarshallingService.loadRackFromFile(filename);
	}

	@Override
	public void saveRackToFile(final RackDataModel dataModel, final String filename) throws DatastoreException, IOException
	{
		rackMarshallingService.saveRackToFile(dataModel, filename);
		rackService.setRackDirty( dataModel, false );
	}

	@Override
	public void destroyRackDataModel(final RackDataModel rackDataModel) throws DatastoreException, MAConstraintViolationException
	{
		rackService.destroyRackDataModel( rackDataModel );
	}

	@Override
	public MadGraphInstance<?,?> getRackGraphInstance( final RackDataModel rack )
	{
		return rackService.getRackGraphInstance( rack );
	}

	@Override
	public RackComponent createComponent( final RackDataModel rack, final MadDefinition<?,?> definition,
			final Map<MadParameterDefinition, String> parameterValues, final String name )
			throws TableCellFullException, TableIndexOutOfBoundsException, DatastoreException,
			MAConstraintViolationException, RecordNotFoundException
	{
		return rackService.createComponent( rack, definition, parameterValues, name );
	}

	private void paintOneRackPerComponent()
			throws DatastoreException, ContentsAlreadyAddedException, TableCellFullException,
				TableIndexOutOfBoundsException, MAConstraintViolationException, RecordNotFoundException
	{
		final MadDefinitionListModel defs = componentService.listDefinitionsAvailable();

		final Map<MadParameterDefinition,String> emptyParameterValues = new HashMap<MadParameterDefinition, String>();

		final int numDefs = defs.getSize();
		for( int i = 0 ; i < numDefs ; ++i )
		{
			final MadDefinition<?,?> def = defs.getElementAt( i );
			if( isDefinitionPublic( def ) )
			{
				final Span curComponentCellSpan = componentUiService.getUiSpanForDefinition( def );

				// If it's the channel 8 mixer, paint the rack master with it
				final boolean paintRackMasterToo = ( def.getId().equals( MixerMadDefinition.DEFINITION_ID ) );
				final int rackWidthToUse = ( paintRackMasterToo ? RackService.DEFAULT_RACK_COLS : curComponentCellSpan.x );
				final int rackHeightToUse = ( paintRackMasterToo ? RackService.DEFAULT_RACK_ROWS : curComponentCellSpan.y + 2 );

				final RackDataModel cacheRack = rackService.createNewRackDataModel( "cachingrack",
						"",
						rackWidthToUse,
						rackHeightToUse,
						paintRackMasterToo );

				final String name = def.getId() + i;
				rackService.createComponent( cacheRack, def, emptyParameterValues, name );

				forceHotspotRackPainting( cacheRack, def.getId() );

				rackService.destroyRackDataModel( cacheRack );
			}
		}
	}

	private final static boolean isDefinitionPublic( final MadDefinition<?,?> definition )
	{
		final MadClassification auc = definition.getClassification();
		final MadClassificationGroup aug = auc.getGroup();
		return ( aug != null ? aug.getVisibility() == Visibility.PUBLIC : false );
	}

	private void forceHotspotRackPainting( final RackDataModel cacheRack, final String componentNameBeingDrawn )
			throws DatastoreException
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Performing rack painting for " + componentNameBeingDrawn);
		}
		// Now create a temporary gui for it and get them to render their front and back into it
		// This will populate the image buffer cache and clear up the stuff read from disk
		RackModelRenderingComponent hotspotRenderingComponent = guiService.createGuiForRackDataModel( cacheRack );
		hotspotRenderingComponent.setForceRepaints( true );

		final JComponent renderingJComponent = hotspotRenderingComponent.getJComponent();
		final Dimension renderingPreferredSize = renderingJComponent.getPreferredSize();
		renderingJComponent.setSize( renderingPreferredSize );
		layoutComponent( renderingJComponent );

		// Paint the front
		final AllocationMatch localAllocationMatch = new AllocationMatch();
		final TiledBufferedImage hotspotPaintTiledImage = bufferedImageAllocationService.allocateBufferedImage( this.getClass().getSimpleName(),
				localAllocationMatch,
				AllocationLifetime.SHORT,
				AllocationBufferType.TYPE_INT_RGB,
				renderingPreferredSize.width,
				renderingPreferredSize.height );
		final BufferedImage imageToRenderInto = hotspotPaintTiledImage.getUnderlyingBufferedImage();
		final Graphics hotspotPaintGraphics = imageToRenderInto.createGraphics();
		final CellRendererPane crp = new CellRendererPane();
		crp.add( renderingJComponent );
		crp.paintComponent( hotspotPaintGraphics, renderingJComponent, crp, 0, 0, renderingPreferredSize.width, renderingPreferredSize.height, true );

		// And now the back
		hotspotRenderingComponent.rotateRack();
		crp.paintComponent( hotspotPaintGraphics, renderingJComponent, crp, 0, 0, renderingPreferredSize.width, renderingPreferredSize.height, true );

		final RackDataModel emptyRack = rackService.createNewRackDataModel( "", "", 2, 2, false );

		// Remove references to the data model passed in
		hotspotRenderingComponent.setRackDataModel( emptyRack );

		// And clear it up
		hotspotRenderingComponent.destroy();
		hotspotRenderingComponent = null;

		bufferedImageAllocationService.freeBufferedImage( hotspotPaintTiledImage );
	}

	private final static void layoutComponent( final Component renderingJComponent )
	{
        synchronized (renderingJComponent.getTreeLock()) {
        	renderingJComponent.doLayout();
            if (renderingJComponent instanceof Container)
                for (final Component child : ((Container) renderingJComponent).getComponents())
                    layoutComponent(child);
        }
	}
}
