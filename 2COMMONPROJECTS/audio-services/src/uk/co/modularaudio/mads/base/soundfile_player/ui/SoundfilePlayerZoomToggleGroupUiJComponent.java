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

package uk.co.modularaudio.mads.base.soundfile_player.ui;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player.mu.SoundfilePlayerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacToggleButton;
import uk.co.modularaudio.util.audio.gui.paccontrols.PacToggleGroup;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class SoundfilePlayerZoomToggleGroupUiJComponent extends JPanel
	implements IMadUiControlInstance<SoundfilePlayerMadDefinition, SoundfilePlayerMadInstance, SoundfilePlayerMadUiInstance>,
	SoundfilePlayerZoomProducer
{
	private static final long serialVersionUID = -5668580477214022847L;

	private final static String[] TOGGLE_LABELS = new String[] {
		"+",
		"=",
		"-"
	};

	private static Log log = LogFactory.getLog( SoundfilePlayerZoomToggleGroupUiJComponent.class.getName() );

	private final PacToggleGroup toggleGroup;

	private final float[] ZOOM_MILLIS = new float[] {
			1250.0f,
			2500.0f,
			5000.0f
	};
	private float currentZoomMillis = 2500.0f;

	private ZoomDataListener dataListener;

	public SoundfilePlayerZoomToggleGroupUiJComponent( SoundfilePlayerMadDefinition definition,
			SoundfilePlayerMadInstance instance,
			SoundfilePlayerMadUiInstance uiInstance,
			int controlIndex )
	{
		setOpaque(true);

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint("fill");
		msh.addLayoutConstraint("gap 0");
		msh.addLayoutConstraint("insets 0");
		setLayout( msh.createMigLayout() );

		toggleGroup = new PacToggleGroup( TOGGLE_LABELS, 1)
		{
			@Override
			public void receiveUpdateEvent(int previousSelection, int newSelection)
			{
				if( dataListener != null )
				{
					currentZoomMillis = ZOOM_MILLIS[ newSelection ];
//					log.debug("Set zoom millis to " + currentZoomMillis );
					dataListener.setZoomMillis(currentZoomMillis);
				}
			}
		};

		for( final PacToggleButton tb : toggleGroup.getToggleButtons() )
		{
//			Font f = tb.getFont().deriveFont( 8.0f );
			final Font f = tb.getFont();
			tb.setFont( f );
			add( tb, "grow, wrap");
		}

		uiInstance.setZoomProducer( this );
	}

	@Override
	public void setZoomDataListener( final ZoomDataListener dataListener )
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Received data listener - will set zoom millis to " + currentZoomMillis );
		}
		this.dataListener = dataListener;
		dataListener.setZoomMillis( currentZoomMillis );
	}

	@Override
	public String getControlValue()
	{
		return String.valueOf(toggleGroup.getSelectedItemIndex());
	}

	@Override
	public void receiveControlValue(String value)
	{
		if( log.isDebugEnabled() )
		{
			log.debug("Receiving control value " + value );
		}
		final int intValue = Integer.valueOf(value);
		toggleGroup.setSelectedItemIndex( intValue );
		this.currentZoomMillis = ZOOM_MILLIS[intValue];
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime)
	{
	}

	@Override
	public Component getControl()
	{
		return this;
	}

	@Override
	public void destroy()
	{
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}
}
