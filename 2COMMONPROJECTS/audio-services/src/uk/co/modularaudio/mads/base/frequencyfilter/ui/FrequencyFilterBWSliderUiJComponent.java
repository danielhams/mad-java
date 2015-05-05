package uk.co.modularaudio.mads.base.frequencyfilter.ui;

import java.awt.Component;

import uk.co.modularaudio.mads.base.frequencyfilter.mu.FrequencyFilterMadDefinition;
import uk.co.modularaudio.mads.base.frequencyfilter.mu.FrequencyFilterMadInstance;
import uk.co.modularaudio.util.audio.gui.mad.IMadUiControlInstance;
import uk.co.modularaudio.util.audio.mad.ioqueue.ThreadSpecificTemporaryEventStorage;
import uk.co.modularaudio.util.audio.mad.timing.MadTimingParameters;
import uk.co.modularaudio.util.audio.mvc.displayslider.models.OscillatorFrequencySliderModel;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayController;
import uk.co.modularaudio.util.mvc.displayslider.SliderDisplayModel.ValueChangeListener;
import uk.co.modularaudio.util.swing.lwtc.LWTCControlConstants;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.DisplayOrientation;
import uk.co.modularaudio.util.swing.mvc.lwtcsliderdisplay.LWTCSliderDisplayView.SatelliteOrientation;

public class FrequencyFilterBWSliderUiJComponent
	implements IMadUiControlInstance<FrequencyFilterMadDefinition, FrequencyFilterMadInstance, FrequencyFilterMadUiInstance>
{
	private final OscillatorFrequencySliderModel model;
	private final SliderDisplayController controller;
	private final LWTCSliderDisplayView view;

	public FrequencyFilterBWSliderUiJComponent(
			final FrequencyFilterMadDefinition definition,
			final FrequencyFilterMadInstance instance,
			final FrequencyFilterMadUiInstance uiInstance,
			final int controlIndex )
	{

		model = new OscillatorFrequencySliderModel();
		controller = new SliderDisplayController( model );
		view = new LWTCSliderDisplayView(
				model,
				controller,
				SatelliteOrientation.LEFT,
				DisplayOrientation.HORIZONTAL,
				SatelliteOrientation.RIGHT,
				LWTCControlConstants.SLIDER_VIEW_COLORS,
				"BW:",
				false );

		view.setLabelMinSize( FrequencyFilterMadUiDefinition.SLIDER_LABEL_MIN_WIDTH, 30 );

		model.addChangeListener( new ValueChangeListener()
		{

			@Override
			public void receiveValueChange( final Object source, final float newValue )
			{
				uiInstance.sendBandwidthChange( model.getValue() );
			}
		} );
	}

	@Override
	public boolean needsDisplayProcessing()
	{
		return false;
	}

	@Override
	public String getControlValue()
	{
		return Float.toString( model.getValue() );
	}

	@Override
	public void receiveControlValue( final String value )
	{
		final float val = Float.parseFloat( value );
		controller.setValue( this, val );
	}

	@Override
	public void doDisplayProcessing( final ThreadSpecificTemporaryEventStorage tempEventStorage,
			final MadTimingParameters timingParameters, final long currentGuiTime )
	{
	}

	@Override
	public Component getControl()
	{
		return view;
	}

	@Override
	public void destroy()
	{
	}

}