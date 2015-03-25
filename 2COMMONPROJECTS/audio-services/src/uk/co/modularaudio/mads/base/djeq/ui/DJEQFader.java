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

package uk.co.modularaudio.mads.base.djeq.ui;

import java.awt.Component;

import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadDefinition;
import uk.co.modularaudio.mads.base.djeq.mu.DJEQMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.DJDeckFaderSliderModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplaySlider;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayTextbox;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDoubleClickMouseListener;
import uk.co.modularaudio.util.swing.mvc.sliderdisplay.SliderDoubleClickMouseListener.SliderDoubleClickReceiver;

public class DJEQFader extends PacPanel
	implements IMadUiControlInstance<DJEQMadDefinition, DJEQMadInstance, DJEQMadUiInstance>
{
	private static final long serialVersionUID = -4624215012389837804L;

	private final DJEQMadUiInstance uiInstance;

	private final DJDeckFaderSliderModel sdm;
	private final SliderDisplayController sdc;

	private final SliderDisplaySlider sds;
	private final SliderDisplayTextbox sdt;

	private final StereoAmpMeter sam;

	private boolean previouslyShowing;

	public DJEQFader( final DJEQMadDefinition definition,
			final DJEQMadInstance instance,
			final DJEQMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;

		setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "fill" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		setLayout( msh.createMigLayout() );

		sdm = new DJDeckFaderSliderModel();
		sdc = new SliderDisplayController( sdm );

		sds = new SliderDisplaySlider( sdm, sdc, DisplayOrientation.VERTICAL, DJEQColorDefines.FOREGROUND_COLOR, false );
		this.add( sds, "growy, pushy 100" );


		sam = new StereoAmpMeter( uiInstance, uiInstance.getUiDefinition().getBufferedImageAllocator(), true );
		this.add( sam, "growy, wrap");

		sdt = new SliderDisplayTextbox( sdm, sdc, DJEQColorDefines.UNITS_COLOR, false );
		this.add( sdt, "growy 0, align center, spanx 2" );

		final SliderDoubleClickMouseListener doubleClickMouseListener = new SliderDoubleClickMouseListener( new SliderDoubleClickReceiver()
		{

			@Override
			public void receiveDoubleClick()
			{
				sdc.setValue( this, sdc.getModel().getInitialValue() );
			}
		} );
		sds.addMouseListener( doubleClickMouseListener );

		sdm.addChangeListener( new ValueChangeListener()
		{

			@Override
			public void receiveValueChange( final Object source, final float newValue )
			{
				final float realAmp = AudioMath.dbToLevelF( newValue );
				uiInstance.setFaderAmp( realAmp );
			}
		} );

		uiInstance.setStereoAmpMeter( sam );

	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}

	@Override
	public String getControlValue()
	{
		return "";
	}

	@Override
	public void receiveControlValue( final String value )
	{
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters,
			final long currentGuiTime )
	{
		final boolean showing = isShowing();

		if( previouslyShowing != showing )
		{
			uiInstance.sendUiActive( showing );
			previouslyShowing = showing;
		}

		sam.receiveDisplayTick( currentGuiTime );
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
}
