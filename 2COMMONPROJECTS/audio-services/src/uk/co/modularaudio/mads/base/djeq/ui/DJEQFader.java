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

	private final DJDeckFaderSliderModel sdm;
	private final SliderDisplayController sdc;

	private final SliderDisplaySlider sds;
	private final SliderDisplayTextbox sdt;

	private final StereoAmpMeter sam;

	public DJEQFader( final DJEQMadDefinition definition,
			final DJEQMadInstance instance,
			final DJEQMadUiInstance uiInstance,
			final int controlIndex )
	{
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
