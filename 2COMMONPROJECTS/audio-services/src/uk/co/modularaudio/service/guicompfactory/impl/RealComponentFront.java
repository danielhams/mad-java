package uk.co.modularaudio.service.guicompfactory.impl;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JPanel;

import uk.co.modularaudio.service.guicompfactory.impl.components.PaintedComponentDefines;
import uk.co.modularaudio.util.audio.gui.mad.AbstractMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;

class RealComponentFront extends JPanel
{
	private static final long serialVersionUID = 5211955307472576952L;

	public RealComponentFront( final RackComponent rc )
	{
		this.setOpaque( false );
		this.setLayout( null );
		final AbstractMadUiControlInstance<?,?,?>[] uiControls = rc.getUiControlInstances();
		for( final AbstractMadUiControlInstance<?,?,?> uic : uiControls )
		{
			final Component swingComponent = uic.getControl();
			this.add(swingComponent );
			swingComponent.setBounds( uic.getUiControlDefinition().getControlBounds() );
		}

		final Dimension size = new Dimension( PaintedComponentDefines.FRONT_MIN_WIDTH,
				PaintedComponentDefines.FRONT_MIN_HEIGHT );
		setSize( size );
		setMinimumSize( size );
		setPreferredSize( size );

	}
}