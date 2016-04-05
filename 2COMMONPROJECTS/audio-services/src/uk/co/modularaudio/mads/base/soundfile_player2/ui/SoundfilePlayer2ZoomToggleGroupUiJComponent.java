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

package uk.co.modularaudio.mads.base.soundfile_player2.ui;

import java.awt.Component;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.soundfile_player2.mu.SoundfilePlayer2MadDefinition;
import uk.co.modularaudio.mads.base.soundfile_player2.mu.SoundfilePlayer2MadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.lwtc.LWTCToggleButton;
import uk.co.modularaudio.util.swing.lwtc.LWTCToggleGroup;

public class SoundfilePlayer2ZoomToggleGroupUiJComponent extends JPanel
	implements IMadUiControlInstance<SoundfilePlayer2MadDefinition, SoundfilePlayer2MadInstance, SoundfilePlayer2MadUiInstance>
{
	private static final long serialVersionUID = -5668580477214022847L;

	private final static String[] TOGGLE_LABELS = new String[] {
		"+",
		"=",
		"-"
	};

//	private static Log log = LogFactory.getLog( SoundfilePlayer2ZoomToggleGroupUiJComponent.class.getName() );

	private final LWTCToggleGroup toggleGroup;

	public enum ZoomLevel
	{
		ZOOMED_IN(1250.0f),
		ZOOMED_DEFAULT(2500.0f),
		ZOOMED_OUT(5000.0f);

		private float millisFoLevel;

		private ZoomLevel( final float millisForLevel )
		{
			this.millisFoLevel = millisForLevel;
		}

		public float getMillisForLevel()
		{
			return millisFoLevel;
		}
	};

	public final static ZoomLevel DEFAULT_ZOOM_LEVEL = ZoomLevel.ZOOMED_DEFAULT;

	public SoundfilePlayer2ZoomToggleGroupUiJComponent( final SoundfilePlayer2MadDefinition definition,
			final SoundfilePlayer2MadInstance instance,
			final SoundfilePlayer2MadUiInstance uiInstance,
			final int controlIndex )
	{
		setOpaque(false);

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint("fill");
		msh.addLayoutConstraint("gap 0");
		msh.addLayoutConstraint("insets 0");
		setLayout( msh.createMigLayout() );

		toggleGroup = new LWTCToggleGroup( LWTCControlConstants.STD_TOGGLE_BUTTON_COLOURS,
				TOGGLE_LABELS,
				1,
				false )
		{
			@Override
			public void receiveUpdateEvent(final int previousSelection, final int newSelection)
			{
				uiInstance.setZoomLevel( ZoomLevel.values()[newSelection] );
			}
		};

		for( final LWTCToggleButton tb : toggleGroup.getToggleButtons() )
		{
			add( tb, "grow, shrink, wrap");
		}
	}

	@Override
	public String getControlValue()
	{
		return String.valueOf(toggleGroup.getSelectedItemIndex());
	}

	@Override
	public void receiveControlValue(final String value)
	{
//		if( log.isDebugEnabled() )
//		{
//			log.debug("Receiving control value " + value );
//		}
		@SuppressWarnings("boxing")
		final int intValue = Integer.valueOf(value);
		toggleGroup.setSelectedItemIndex( intValue );
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
