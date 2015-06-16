package uk.co.modularaudio.componentdesigner.profiling;

import java.awt.Color;

import javax.swing.JLabel;

public class ProfilingLabel extends JLabel
{
	private static final long serialVersionUID = 6040614380668902156L;

	public ProfilingLabel( final String text )
	{
		super(text);
		setColour();
	}

	public ProfilingLabel()
	{
		super();
		setColour();
	}

	private final void setColour()
	{
		setForeground( Color.BLACK );
	}

}
