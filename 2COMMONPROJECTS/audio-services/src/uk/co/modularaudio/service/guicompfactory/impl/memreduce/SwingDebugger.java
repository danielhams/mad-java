package uk.co.modularaudio.service.guicompfactory.impl.memreduce;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JPanel;

import uk.co.modularaudio.service.guicompfactory.impl.components.PaintedComponentDefines;
import uk.co.modularaudio.service.guicompfactory.impl.memreduce.fakemad.FakeRackComponent;
import uk.co.modularaudio.util.audio.gui.mad.rack.RackComponent;
import uk.co.modularaudio.util.audio.mad.MadProcessingException;
import uk.co.modularaudio.util.bufferedimage.BufferedImageAllocator;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class SwingDebugger extends JFrame
{
	private static final long serialVersionUID = 5527718503179725139L;

	private final MemReducedComponentFactory mrcf;

	private class DebugPanel extends JPanel
	{
		private static final long serialVersionUID = 4364479399708430464L;

		DebugPanel()
		{
			setOpaque( false );
			setBackground( Color.PINK );
			final Dimension size = new Dimension( 100, 200 );
			setMinimumSize( size );
		}

		@Override
		public void paint( final Graphics g )
		{
			final Graphics2D g2d = (Graphics2D)g;
			g2d.setComposite( PaintedComponentDefines.ERASE_COMPOSITE );
			g2d.fillRect( 0, 0, getWidth(), getHeight() );
			g2d.setComposite( PaintedComponentDefines.SRCOVER_COMPOSITE);
			g2d.setColor( PaintedComponentDefines.BLANK_FRONT_COLOR );
			g2d.fillRect( 0, 0, getWidth(), getHeight() );
			mrcf.paint( g );
		}
	};

	public SwingDebugger( final BufferedImageAllocator bia, final MemReducedComponentFactory mrcf ) throws MadProcessingException, DatastoreException
	{
		this.mrcf = mrcf;

		this.setMinimumSize( new Dimension( 400, 400 ) );
		this.setBackground( Color.GREEN );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();
		msh.addLayoutConstraint( "fill" );
		this.setLayout( msh.createMigLayout() );

		this.add( new DebugPanel(), "grow, wrap");

		final RackComponent rc = FakeRackComponent.createInstance( bia );
		this.add( new ResizableFrontContainer( mrcf.getFrontDecorationImages(), rc ), "grow, wrap");
		this.add( new ResizableBackContainer( mrcf.getBackDecorationImages(), rc ), "grow");

		this.setVisible( true );
	}
}
