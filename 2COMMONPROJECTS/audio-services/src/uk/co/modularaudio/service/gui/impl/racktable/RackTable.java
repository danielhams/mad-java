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

package uk.co.modularaudio.service.gui.impl.racktable;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import uk.co.modularaudio.service.guicompfactory.AbstractGuiAudioComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponentProperties;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackDataModel;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTable;
import uk.co.modularaudio.util.swing.dndtable.layeredpane.LayeredPaneDndTableDecorations;


public class RackTable
	extends LayeredPaneDndTable<RackComponent, RackComponentProperties, AbstractGuiAudioComponent>
{
	private static final long serialVersionUID = -7415174078798644069L;

//	private static Log log = LogFactory.getLog( RackTable.class.getName() );

	protected RackDataModel rackDataModel;
	private final RackTableDndPolicy dndPolicy;

	public RackTable(
			final RackDataModel rackDataModel,
			final RackTableEmptyCellPainter emptyCellPainter,
			final RackTableGuiFactory factory,
			final RackTableDndPolicy dndPolicy,
			final LayeredPaneDndTableDecorations dndDecorations,
			final Dimension gridSize,
			final boolean showGrid,
			final Color gridColour)
	{
		super( rackDataModel, factory, dndPolicy, dndDecorations, gridSize, showGrid, gridColour, emptyCellPainter);
		this.dndPolicy = dndPolicy;
		this.rackDataModel = rackDataModel;
	}

	public void setRackDataModel(final RackDataModel rackDataModel) throws DatastoreException
	{
		super.setDataModel( rackDataModel );
		this.rackDataModel = rackDataModel;
		// Reset the data model referred to in the policy, too
		dndPolicy.setRackDataModel( rackDataModel );
	}

	public JComponent getJComponent()
	{
		return this;
	}

	@Override
	public void contentsChangeBegin()
	{
		super.contentsChangeBegin();
	}

	@Override
	public void contentsChangeEnd()
	{
		super.contentsChangeEnd();
//		log.debug("NewRackTable not fully repainting itself...");
//		this.validate();
//		this.repaint();
		final MouseEvent syntheticMouseEvent = new MouseEvent( this, -1, 2323, 0, -1, -1, -1, -1, 0, false, 0 );
		// Synthesise a mouse moved event too so that selection state is correct
		dndMouseListener.mouseMoved( syntheticMouseEvent );
	}

	@Override
	public void destroy()
	{
		super.destroy();
		dndPolicy.destroy();
		rackDataModel = null;
	}

	public void setForceRepaints( final boolean forceRepaints )
	{
		this.forcePaint = forceRepaints;
	}


//	@Override
//	public void paint( final Graphics g )
//	{
//		super.paint( g );
//		log.debug("RackTable paint called");
//	}
}
