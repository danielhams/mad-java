package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JPanel;

import uk.co.modularaudio.service.gui.plugs.GuiChannelPlug;
import uk.co.modularaudio.service.guicompfactory.impl.components.PaintedComponentDefines;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;

class RealComponentBack extends JPanel
{
	private static final long serialVersionUID = 5211955307472576952L;

	public RealComponentBack( final ResizableBackContainer resizableBackContainer, final RackComponent rc )
	{
		this.setOpaque( false );
		this.setLayout( null );

		for (final GuiChannelPlug plug : resizableBackContainer.plugsToDestroy)
		{
			this.add( plug );
		}

		final Dimension size = new Dimension( PaintedComponentDefines.BACK_MIN_WIDTH,
				PaintedComponentDefines.BACK_MIN_HEIGHT );
		setSize( size );
		setMinimumSize( size );
		setPreferredSize( size );
	}

	public GuiChannelPlug getPlugFromPosition( final Point localPoint )
	{
		// log.debug("Looking for plug at real position " + localPoint );
		GuiChannelPlug retVal = null;
		final Component c = this.getComponentAt( localPoint );
		if (c != null)
		{
			if (c instanceof GuiChannelPlug)
			{
				retVal = (GuiChannelPlug) c;
			}
		}
		return retVal;
	}
}