package uk.co.modularaudio.componentdesigner.profiling;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.modularaudio.componentdesigner.controller.front.ComponentDesignerFrontController;
import uk.co.modularaudio.util.swing.general.MigLayoutStringHelper;

public class ProfilingPanel extends JPanel
{
	private static final long serialVersionUID = 6597946142348036824L;

	private static Log log = LogFactory.getLog( ProfilingPanel.class.getName() );

	private final ProfileVisualiser pv;
	private final JScrollPane scrollPane;

	public ProfilingPanel( final ComponentDesignerFrontController fc )
	{
		pv = new ProfileVisualiser( fc );

		final MigLayoutStringHelper msh = new MigLayoutStringHelper();

//		msh.addLayoutConstraint( "debug" );
		msh.addLayoutConstraint( "insets 0" );
		msh.addLayoutConstraint( "gap 0" );
		msh.addLayoutConstraint( "fill" );

		this.setLayout( msh.createMigLayout() );

		scrollPane = new JScrollPane();

		scrollPane.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		scrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );

		scrollPane.setViewportView( pv );

		this.add( scrollPane, "grow");
	}

	public void refresh()
	{
		final int prevScrollbarValue = scrollPane.getVerticalScrollBar().getValue();
		pv.refresh();
		validate();
		SwingUtilities.invokeLater( new Runnable()
		{

			@Override
			public void run()
			{
				scrollPane.getVerticalScrollBar().setValue(prevScrollbarValue );
			}
		} );
	}

}
