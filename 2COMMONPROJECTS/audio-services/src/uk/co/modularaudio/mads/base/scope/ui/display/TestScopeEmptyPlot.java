package uk.co.modularaudio.mads.base.scope.ui.display;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;

import uk.co.modularaudio.mads.base.scope.ui.ScopeDisplayUiJComponent;

public class TestScopeEmptyPlot extends JPanel
{
	private static final long serialVersionUID = -290572139583210940L;

	public TestScopeEmptyPlot( final Color testColor )
	{
		setBackground( testColor );
		this.setMinimumSize( new Dimension( ScopeDisplayUiJComponent.AXIS_MARKS_LENGTH, ScopeDisplayUiJComponent.AXIS_MARKS_LENGTH ) );
	}
}
