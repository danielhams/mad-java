package uk.co.modularaudio.util.swing.imagebutton;

import uk.co.modularaudio.util.swing.lwtc.LWTCButtonColours;

public class LWTCOptionsButton extends ImageButton
{
	private static final long serialVersionUID = 109978861866189560L;

	private final static ImageButton.ImageButtonProducer STD_OPTIONS_IMAGE_PRODUCER = new OptionsButtonImageProducer();

	public LWTCOptionsButton( final LWTCButtonColours colours, final boolean isImmediate )
	{
		super( colours, null, isImmediate, STD_OPTIONS_IMAGE_PRODUCER );
	}
}
