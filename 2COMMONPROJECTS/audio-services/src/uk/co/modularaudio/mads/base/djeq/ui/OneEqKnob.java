package uk.co.modularaudio.mads.base.djeq.ui;

import uk.co.modularaudio.util.audio.gui.madswingcontrols.PacPanel;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayController;
import uk.co.modularaudio.util.mvc.displayrotary.RotaryDisplayModel;
import uk.co.modularaudio.util.mvc.displayrotary.SimpleRotaryIntToFloatConverter;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayKnob.KnobType;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView.SatelliteOrientation;
import uk.co.modularaudio.util.swing.mvc.rotarydisplay.RotaryDisplayView;

public class OneEqKnob extends PacPanel
{
	private static final long serialVersionUID = 537398070381235844L;

	private final RotaryDisplayModel rdm;
	private final RotaryDisplayController rdc;
	private final RotaryDisplayView rdv;

	public OneEqKnob( final String label )
	{
		setOpaque( false );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );
//		msh.addLayoutConstraint( "debug" );

		setLayout( msh.createMigLayout() );

		rdm = new RotaryDisplayModel(
				-26.0f,
				9.0f,
				0.0f,
				3500,
				500,
				new SimpleRotaryIntToFloatConverter(),
				3,
				1,
				"dB");

		rdc = new RotaryDisplayController( rdm );

		rdv = new RotaryDisplayView(
				rdm,
				rdc,
				KnobType.BIPOLAR,
				SatelliteOrientation.ABOVE,
				SatelliteOrientation.RIGHT,
				label,
				DJEQColorDefines.LABEL_COLOR,
				DJEQColorDefines.UNITS_COLOR,
				DJEQColorDefines.BACKGROUND_COLOR,
				DJEQColorDefines.FOREGROUND_COLOR,
				DJEQColorDefines.KNOB_COLOR,
				DJEQColorDefines.OUTLINE_COLOR,
				DJEQColorDefines.INDICATOR_COLOR,
				false,
				true );

		this.add( rdv, "grow" );
	}

	public RotaryDisplayModel getModel()
	{
		return rdm;
	}
}
