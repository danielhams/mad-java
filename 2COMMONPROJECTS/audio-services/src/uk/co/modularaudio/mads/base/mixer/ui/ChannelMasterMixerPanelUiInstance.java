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

package uk.co.modularaudio.mads.base.mixer.ui;

import java.awt.Color;

import javax.swing.JComponent;

import net.miginfocom.swing.MigLayout;
import uk.co.modularaudio.mads.base.mixer.mu.MixerMadDefinition;
import uk.co.modularaudio.mads.base.mixer.mu.MixerMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.math.AudioMath;
import uk.co.modularaudio.util.audio.math.DbToLevelComputer;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ChannelMasterMixerPanelUiInstance extends PacPanel
	implements IMadUiControlInstance<MixerMadDefinition, MixerMadInstance, MixerMadUiInstance>,
	AmpSliderChangeReceiver, MeterValueReceiver, PanSliderChangeReceiver
{
//	private static Log log = LogFactory.getLog( ChannelMasterMixerPanelUiInstance.class.getName() );

	private static final long serialVersionUID = 24665241385474657L;

	private final MixerMadUiInstance uiInstance;

	private final PanSlider panSlider;

	private final AmpSliderAndMeter ampSliderAndMeter;

	private final DbToLevelComputer dBToLevelComputer;

	private boolean previouslyShowing;

	public ChannelMasterMixerPanelUiInstance( final MixerMadDefinition definition,
			final MixerMadInstance instance,
			final MixerMadUiInstance uiInstance,
			final int controlIndex )
	{
		this.uiInstance = uiInstance;

		this.setOpaque( true );
		this.setBackground( MixerMadUiDefinition.MASTER_BG_COLOR );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addRowConstraint( "[grow 0][fill][grow 0]" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );
//		msh.addLayoutConstraint( "debug" );

		final MigLayout compLayout = msh.createMigLayout();
		this.setLayout( compLayout );

		panSlider = new PanSlider( this, Color.BLACK );
		this.add( panSlider, "growx, growy 0, wrap" );

		ampSliderAndMeter = new AmpSliderAndMeter( uiInstance,
				uiInstance.getUiDefinition().getBufferedImageAllocator(),
				true,
				Color.BLACK );

		dBToLevelComputer = ampSliderAndMeter.getDbToLevelComputer();

		this.add( ampSliderAndMeter, "growx, growy" );

		ampSliderAndMeter.setChangeReceiver( this );

		uiInstance.registerMasterMeterReceiver( this );
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

		ampSliderAndMeter.receiveDisplayTick( currentGuiTime );
	}

	@Override
	public JComponent getControl()
	{
		return this;
	}

	@Override
	public void receiveAmpSliderChange( final float newValue )
	{
		// Now translate this into dB and then into amplitude
		final float dbForValue = dBToLevelComputer.toDbFromNormalisedLevel( newValue );
//		log.debug("Using db " + dbForValue );
		final float ampForDb = (float)AudioMath.dbToLevel( dbForValue );
		uiInstance.sendMasterAmp( ampForDb );
	}

	@Override
	public String getControlValue()
	{
		return ampSliderAndMeter.getControlValue() + ":" + panSlider.getControlValue();
	}

	@Override
	public void receiveControlValue( final String value )
	{
		final String[] vals = value.split( ":" );
		if( vals.length == 2 )
		{
			ampSliderAndMeter.receiveControlValue( vals[0] );
			panSlider.receiveControlValue( vals[1] );
		}
	}

	@Override
	public void receiveMeterReadingLevel( final long currentTimestamp, final int channelNum, final float meterReadingLevel )
	{
		final float meterReadingDb = (float)AudioMath.levelToDb( meterReadingLevel );
		ampSliderAndMeter.receiveMeterReadingInDb( currentTimestamp, channelNum, meterReadingDb );
	}

	@Override
	public void receiveMuteSet( final long currentTimestamp, final boolean muted )
	{
		// Ignore
	}

	@Override
	public void receiveSoloSet( final long currentTimestamp, final boolean muted )
	{
		// Ignore
	}

	@Override
	public void destroy()
	{
		ampSliderAndMeter.destroy();
	}

	@Override
	public void receivePanChange( final float panValue )
	{
		uiInstance.sendMasterPan( panValue );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return true;
	}
}
