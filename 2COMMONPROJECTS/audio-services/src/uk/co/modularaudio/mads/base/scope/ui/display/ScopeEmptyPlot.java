package uk.co.modularaudio.mads.base.scope.ui.display;

import java.awt.Dimension;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.scope.ui.ScopeColours;
import uk.co.modularaudio.mads.base.scope.ui.ScopeDisplayUiJComponent;

public class ScopeEmptyPlot extends JPanel
{
	private static final long serialVersionUID = -290572139583210940L;

	public ScopeEmptyPlot()
	{
		setBackground( ScopeColours.BACKGROUND_COLOR );
		this.setMinimumSize( new Dimension( ScopeDisplayUiJComponent.AXIS_MARKS_LENGTH, ScopeDisplayUiJComponent.AXIS_MARKS_LENGTH ) );
	}
}
