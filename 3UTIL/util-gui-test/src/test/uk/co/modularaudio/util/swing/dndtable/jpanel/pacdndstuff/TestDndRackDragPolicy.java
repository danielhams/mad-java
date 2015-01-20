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

package test.uk.co.modularaudio.util.swing.dndtable.jpanel.pacdndstuff;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.uk.co.modularaudio.util.swing.dndtable.jpanel.TestGC;
import test.uk.co.modularaudio.util.swing.dndtable.jpanel.TestTC;
import test.uk.co.modularaudio.util.swing.dndtable.jpanel.TestTP;
import uk.co.modularaudio.util.swing.dndtable.jpanel.JPanelDndTable;
import uk.co.modularaudio.util.swing.dndtable.jpanel.JPanelDndTablePolicy;
import uk.co.modularaudio.util.swing.scrollpane.AutoScrollingMouseListener;
import uk.co.modularaudio.util.swing.table.GuiTableDataModel;
import uk.co.modularaudio.util.table.NoSuchContentsException;
import uk.co.modularaudio.util.table.Span;
import uk.co.modularaudio.util.table.TablePosition;

public class TestDndRackDragPolicy 
	implements JPanelDndTablePolicy<TestTC, TestTP, TestGC>
{
	private static final float GHOST_TRANSPARENCY_MULTIPLIER = 0.95f;

	private static Log log = LogFactory.getLog( TestDndRackDragPolicy.class.getName() );
	
	private GuiTableDataModel<TestTC, TestTP> dataModel = null;
	
	// SCROLL
	private AutoScrollingMouseListener scrollingMouseListener = new AutoScrollingMouseListener();

	private static final float strongChannelValue = 1.0f;
	private static final float weakChannelValue = 0.1f;
	private static final Color VALID_REGION_HINT_COLOR = new Color( weakChannelValue, strongChannelValue, weakChannelValue );
	private static final Color INVALID_REGION_HINT_COLOR = new Color( strongChannelValue, weakChannelValue, weakChannelValue );
	private static final Color SOURCE_REGION_HINT_COLOR = Color.ORANGE;

//	private TestDndRackDecorations decorations = null;
	private TestDndRackDragRegionHintDecoration regionHintDecorator = null;
	private TestDndRackDragGhostHintDecoration ghostHintDecorator = null;
	private TestDndRackDragTargetPositionHintDecorator targetPositionHintDecorator = null;
	
	public TestDndRackDragPolicy( GuiTableDataModel<TestTC, TestTP> dataModel,
			TestDndRackDecorations decorations )
	{
		this.dataModel = dataModel;
//		this.decorations = decorations;
		regionHintDecorator = decorations.getRegionHintDecorator();
		ghostHintDecorator = decorations.getGhostHintDecorator();
		targetPositionHintDecorator = decorations.getTargetPositionHintDecorator();
	}
	
	@Override
	public boolean isMouseOverDndSource( JPanelDndTable<TestTC, TestTP, TestGC> table,
			TestGC component,
			Point localPoint,
			Point tablePoint)
	{
		boolean isDraggable = false;
		if( component != null )
		{
			if( component.isPointLocalDragRegion( localPoint ) )
			{
				if( component != regionHintSourceAreaImageComponent )
				{
					regionHintSourceAreaImageComponent = component;
					// It's a different component - regenerate the region hint and set it
					regionHintSourceAreaImage = generateRegionHintImage( component, SOURCE_REGION_HINT_COLOR );
				}

				TestTC tableComponent = table.getTableModelComponentFromGui( component );
				TablePosition tp = dataModel.getContentsOriginReturnNull( tableComponent );
				Dimension gridSize = table.getGridSize();
				int hintXOffset = tp.x * gridSize.width;
				int hintYOffset = tp.y * gridSize.height;
				regionHintDecorator.setRegionHint( regionHintSourceAreaImage, hintXOffset, hintYOffset );
				regionHintDecorator.setActive( true );
				isDraggable = true;
			}
		}
		if( !isDraggable )
		{
			regionHintDecorator.setActive( false );
		}
		dragSourceMouseOffset = tablePoint;
		return isDraggable;
	}
	
	// Stuff used to highlight the possible source of a drag region
	private BufferedImage regionHintSourceAreaImage = null;
	private TestGC regionHintSourceAreaImageComponent = null;
	
	// Copies of data the policy hangs on to to help with tests, or with the move itself.
	private TestGC dragSourceGuiComponent = null;
	private TestTC dragSourceTableComponent = null;
	private Span dragSourceCellSpan = null;
	private BufferedImage dragSourceBufferedImage = null;
	private Point dragSourceMouseOffset = null;
	
	private BufferedImage regionHintValidImage = null;
	private BufferedImage regionHintInvalidImage = null;
	
	// Setup by start drag
	private TestDndRackDragPolicyDragTargetHelper dragTargetHelper = null;
	private TestDndRackDragMatch dragMatch = null;

	@Override
	public void startDrag( JPanelDndTable<TestTC, TestTP, TestGC> table,
			TestGC component,
			Point dragLocalPoint,
			Point dragTablePoint)
	{
		log.debug("Drag begun.");
		this.dragSourceGuiComponent = component;
		dragSourceTableComponent = table.getTableModelComponentFromGui( dragSourceGuiComponent );
		dragSourceCellSpan = dragSourceTableComponent.getCellSpan();

		// Take a buffered image snapshot of the component as the "ghost"
		dragSourceBufferedImage = new BufferedImage( component.getWidth(), component.getHeight(), BufferedImage.TYPE_INT_ARGB );
		Graphics2D biG = dragSourceBufferedImage.createGraphics();
		biG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, GHOST_TRANSPARENCY_MULTIPLIER));
		component.paint( biG );
		dragSourceMouseOffset = new Point( -dragLocalPoint.x, -dragLocalPoint.y );
		ghostHintDecorator.setImageAndOffset( dragSourceBufferedImage, dragSourceMouseOffset );
		ghostHintDecorator.setActive( true );
		
		targetPositionHintDecorator.setActive( true );
		
		// Generate the region hint images for success and failure
		regionHintValidImage = generateRegionHintImage( dragSourceGuiComponent, VALID_REGION_HINT_COLOR );
		regionHintInvalidImage = generateRegionHintImage( dragSourceGuiComponent, INVALID_REGION_HINT_COLOR );
		
		// Make sure no position is currently hinted (from a previous failed drag)
		regionHintDecorator.setActive( true );
		
		dragTargetHelper = new TestDndRackDragPolicyDragTargetHelper( table,
				dataModel,
				dragSourceTableComponent,
				dragSourceCellSpan, 
				targetPositionHintDecorator, 
				dragSourceMouseOffset);

		// SCROLL
		table.addMouseMotionListener( scrollingMouseListener );
	}
	
	private static final float REGION_HINT_BACKGROUND_TRANSPARENCY = 0.4f;
	private static final float REGION_HINT_OUTLINE_TRANSPARENCY = 0.7f;
	
	private BufferedImage generateRegionHintImage( TestGC sourceGuiComponent, Color regionHintColor)
	{
		int width = sourceGuiComponent.getWidth();
		int height = sourceGuiComponent.getHeight();
		
		BufferedImage retVal = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		Graphics2D g2d = (Graphics2D)retVal.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				REGION_HINT_BACKGROUND_TRANSPARENCY));
		g2d.setColor( regionHintColor );
		g2d.fillRect( 0, 0, width - 1, height - 1);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				REGION_HINT_OUTLINE_TRANSPARENCY));
		g2d.drawRect( 0, 0, width - 1, height - 1);
		return retVal;
	}
	
	@Override
	public boolean isValidDragTarget(JPanelDndTable<TestTC, TestTP, TestGC> table,
			TestGC component,
			Point dragLocalPoint,
			Point mouseDragTargetPoint)
	{
		boolean isValid = false;
		try
		{
			dragMatch = dragTargetHelper.lookupDragMatchUseCache( dragLocalPoint, 
					mouseDragTargetPoint );
			isValid = dragMatch.canMoveHere;
		}
		catch(TestDndRackDragTargetLookupException e )
		{
			log.error( e );
			return false;
		}
		catch (NoSuchContentsException e)
		{
			// The table is telling is that the drag source component isn't in the table anymore - perhaps removed by a different thread?
			log.error( e );
			return false;
		}
		// Drag match is setup and valid - now setup the target hint to show if we can drop or not
		int gridSizeWidth = table.getGridSize().width;
		int gridSizeHeight = table.getGridSize().height;
		int regionHintX = gridSizeWidth * dragMatch.colsOffset;
		int regionHintY = gridSizeHeight * dragMatch.rowsOffset;
		regionHintDecorator.setRegionHint( (isValid ? regionHintValidImage : regionHintInvalidImage), regionHintX, regionHintY );
		regionHintDecorator.setActive( true );
		
		return isValid;
	}

	@Override
	public void endDrag(JPanelDndTable<TestTC, TestTP, TestGC> table,
			TestGC component,
			Point dragLocalPoint,
			Point dragEndPoint)
	{
		log.debug("Drag ended.");
		
		// SCROLL
		// Remove the auto scroll behaviour
		scrollingMouseListener.stop();
		table.removeMouseMotionListener( scrollingMouseListener );
		
		// Now do the move
		try
		{
			dataModel.removeContents( dragSourceTableComponent );
			dataModel.addContentsAtPosition( dragSourceTableComponent, dragMatch.colsOffset, dragMatch.rowsOffset );
		}
		catch (Exception e)
		{
			String msg = "Exception caught finishing the drag: " + e.toString();
			log.error( msg, e);
		}

		cleanupAfterDrag();
	}
	
	@Override
	public void endInvalidDrag(JPanelDndTable<TestTC, TestTP, TestGC> table,
			TestGC component,
			Point dragLocalPoint,
			Point dragEndPoint)
	{
		log.debug("Invalid drag ended.");
		cleanupAfterDrag();
	}
	
	private void cleanupAfterDrag()
	{
		// Clean up GUI drag hint stuff before we do the actual move
		ghostHintDecorator.setActive( false );

		// Clear the region hint too
		regionHintDecorator.setActive( false );

		// Finally clear up the internal variables we used during the move.
		dragSourceGuiComponent = null;
		dragSourceTableComponent = null;
		dragSourceCellSpan = null;
		dragSourceMouseOffset = null;
	}
}
