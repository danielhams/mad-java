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

package test.uk.co.modularaudio.util.table;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import test.uk.co.modularaudio.util.table.testobjs.AudioComponent;
import test.uk.co.modularaudio.util.table.testobjs.AudioComponentProperties;
import test.uk.co.modularaudio.util.table.testobjs.DJMixerComponent;
import test.uk.co.modularaudio.util.table.testobjs.DJMixerComponentProperties;
import test.uk.co.modularaudio.util.table.testobjs.TestTableListener;
import uk.co.modularaudio.util.exception.DatastoreException;
import uk.co.modularaudio.util.table.ContentsAlreadyAddedException;
import uk.co.modularaudio.util.table.NoSuchContentsException;
import uk.co.modularaudio.util.table.Span;
import uk.co.modularaudio.util.table.TableCellFullException;
import uk.co.modularaudio.util.table.TableIndexOutOfBoundsException;
import uk.co.modularaudio.util.table.TableInterface;
import uk.co.modularaudio.util.table.TableModelEvent;
import uk.co.modularaudio.util.table.TablePosition;
import uk.co.modularaudio.util.table.impl.Table;
import uk.co.modularaudio.util.table.impl.TablePrinter;

public class TestTable extends TestCase
{
	private static Log log = LogFactory.getLog( TestTable.class.getName() );

	static
	{
		// Setup log4j
//		BasicConfigurator.configure();
	}

	public void testCreatingAndAddingToTable() throws ContentsAlreadyAddedException, TableCellFullException, TableIndexOutOfBoundsException, DatastoreException
	{
		log.info("testCreatingAndAddingToTable beginning.");
		final TableInterface<AudioComponent, AudioComponentProperties> table = new Table<AudioComponent, AudioComponentProperties>( 10, 10 );
		// Add something that is 2 by 2 at 0,0
		final DJMixerComponent firstObj = new DJMixerComponent();
		table.addContentsAtPosition( firstObj, 0, 0 );

		final Span ecCellSpan = firstObj.getCellSpan();
		for( int i = 0 ; i < ecCellSpan.x ; i++ )
		{
			for( int j = 0 ; j < ecCellSpan.y ; j++ )
			{
				final AudioComponent check = table.getContentsAtPosition( i, j );
				assertTrue( check == firstObj );
			}
		}
		assertNull( table.getContentsAtPosition( 0, 3 ) );
		assertNull( table.getContentsAtPosition( 3, 0 ) );

		// Now try adding it again to get an exception saying it's already in the table
		boolean thrown = false;
		try
		{
			table.addContentsAtPosition( firstObj, 2, 2 );
		}
		catch (final ContentsAlreadyAddedException e)
		{
			thrown = true;
		}
		assertTrue( thrown );
		assertNull( table.getContentsAtPosition( 5, 0 ) );
		assertNull( table.getContentsAtPosition( 0, 5 ) );

		// Now create a different component and force a failure
		thrown = false;
		final DJMixerComponent secondObj = new DJMixerComponent();
		try
		{
			table.addContentsAtPosition( secondObj, 0, 1 );
		}
		catch (final TableCellFullException e)
		{
			thrown = true;
		}
		assertTrue( thrown );
		table.addContentsAtPosition( secondObj, 0, 2 );

		for( int i = 0 ; i < ecCellSpan.x ; i++ )
		{
			for( int j = 2 ; j < ecCellSpan.y ; j++ )
			{
				final AudioComponent check = table.getContentsAtPosition( i, j );
				assertTrue( check == secondObj );
			}
		}

		// Now print out whats in the table
		final TablePrinter<AudioComponent, AudioComponentProperties> printer = new TablePrinter<AudioComponent, AudioComponentProperties>();
		printer.printTableContents( table );

		final TablePosition firstObjOrigin = table.getContentsOriginReturnNull( firstObj );
		log.debug("For the first object I get origin: " + firstObjOrigin);
		assertEquals( firstObjOrigin, new TablePosition(0,0) );

		final TablePosition secondObjOrigin = table.getContentsOriginReturnNull( secondObj );
		log.debug("For the second object I get origin: " + secondObjOrigin);
		assertEquals( secondObjOrigin, new TablePosition( 0, 2 ));

		// Make sure there are indeed only two objects in the table
		assertTrue( table.getNumEntries() == 2 );

		// Now try storing one outside of the bounds of the table
		final DJMixerComponent lastObj = new DJMixerComponent();
		thrown = false;
		try
		{
			table.addContentsAtPosition( lastObj, 500, 500 );
		}
		catch(final TableIndexOutOfBoundsException e)
		{
			thrown = true;
		}
		assertTrue( thrown );
	}

	public void testAddingProperties()
		throws ContentsAlreadyAddedException, TableCellFullException, NoSuchContentsException, TableIndexOutOfBoundsException
	{
		log.info("testAddingProperties beginning.");
		final TableInterface<AudioComponent, AudioComponentProperties> table = new Table<AudioComponent, AudioComponentProperties>( 10, 10 );

		// Add something that is 2 by 2 at 0,0
		final DJMixerComponent firstObj = new DJMixerComponent();
		final DJMixerComponentProperties firstObjProperties = new DJMixerComponentProperties();
		table.addContentsAndPropertiesAtPosition( firstObj, firstObjProperties, 0, 0 );

		final AudioComponentProperties getTest = table.getPropertiesAtPosition( 0, 0 );
		assertNotNull( getTest );
		assertTrue( getTest == firstObjProperties );

		// Now print it
		assertTrue( table.getNumEntries() == 1 );
		final TablePrinter<AudioComponent, AudioComponentProperties> printer = new TablePrinter<AudioComponent, AudioComponentProperties>();
		printer.printTableContents( table );
	}

	public void testRemovingFromTable()
		throws ContentsAlreadyAddedException, TableCellFullException, NoSuchContentsException, TableIndexOutOfBoundsException, DatastoreException
	{
		log.info("testRemovingFromTable beginning.");
		TableInterface<AudioComponent, AudioComponentProperties> testTable =
			new Table<AudioComponent, AudioComponentProperties>( 2, 10 );

		final DJMixerComponent firstObj = new DJMixerComponent();

		testTable.addContentsAtPosition( firstObj, 0, 5 );

		boolean thrown = false;
		final DJMixerComponent nonObj = new DJMixerComponent();

		try
		{
			testTable.removeContents(nonObj);
		}
		catch (final NoSuchContentsException e)
		{
			thrown = true;
		}
		assertTrue( thrown );
		thrown = false;
		testTable.removeContents( firstObj );
		// Now check if it has really been removed
		final AudioComponent nothing = testTable.getContentsAtPosition( 0, 5 );
		assertNull( nothing );
		final TablePrinter<AudioComponent, AudioComponentProperties> printer = new TablePrinter<AudioComponent, AudioComponentProperties>();
		log.debug("Table should be empty.");
		printer.printTableContents( testTable );
		log.debug("After printing table.");


		// Now add five objects to a table, and delete the third
		testTable = new Table<AudioComponent, AudioComponentProperties>( 20, 20 );
		final DJMixerComponent[] objsToAdd = new DJMixerComponent[ 5 ];
		for( int i = 0 ; i < objsToAdd.length ; i++ )
		{
			objsToAdd[ i ] = new DJMixerComponent();
			testTable.addContentsAtPosition( objsToAdd[ i  ], 0, i * 2 );
		}

		assertTrue( testTable.getEntriesAsSet().size() == 5 );
		log.debug("The five object table insertion test:");
		printer.printTableContents( testTable );

		final TestTableListener removalListener = new TestTableListener();
		testTable.addListener( removalListener );

		final TablePosition positionBeforeDelete = testTable.getContentsOriginReturnNull( objsToAdd[ 2 ] );

		testTable.removeContents( objsToAdd[ 2 ] );
		log.debug("The five object table insertion test after deleting third object:");
		printer.printTableContents( testTable );

		assertTrue( removalListener.numTimesCalled == 1 );
		assertTrue( removalListener.eventType == TableModelEvent.DELETE );

		final AudioComponent noSuchComponent = testTable.getContentsAtPosition( positionBeforeDelete.x, positionBeforeDelete.y );
		assertNull( noSuchComponent );

	}

	public void testTableListenerFunctionality()
		throws ContentsAlreadyAddedException, TableCellFullException, NoSuchContentsException, TableIndexOutOfBoundsException, DatastoreException
	{
		log.info("testTableListenerFunctionality beginning.");

		final TestTableListener lis = new TestTableListener();

		final TableInterface<AudioComponent, AudioComponentProperties> testTable =
			new Table<AudioComponent, AudioComponentProperties>( 2, 10 );

		testTable.addListener(lis);

		final DJMixerComponent firstObj = new DJMixerComponent();
		final DJMixerComponent secondObj = new DJMixerComponent();

		testTable.addContentsAtPosition( firstObj, 0, 0 );

		assertTrue( testTable.getNumEntries() == 1 );

		assertTrue( lis.numTimesCalled == 1 );
		assertTrue( lis.sourceObject == testTable );
		assertTrue( lis.eventType == TableModelEvent.INSERT );
		assertTrue( lis.firstRow == 0 );
		assertTrue( lis.lastRow == 0 );

		testTable.removeContents( firstObj );

		assertTrue( testTable.getNumEntries() == 0 );

		assertTrue( lis.numTimesCalled == 2 );
		assertTrue( lis.sourceObject == testTable );
		assertTrue( lis.eventType == TableModelEvent.DELETE );
		assertTrue( lis.firstRow == 0 );
		assertTrue( lis.lastRow == 0 );

		// Now add it at row 6
		testTable.addContentsAtPosition( firstObj, 0, 5 );

		assertTrue( testTable.getNumEntries() == 1 );

		assertTrue( lis.numTimesCalled == 3 );
		assertTrue( lis.sourceObject == testTable );
		assertTrue( lis.eventType == TableModelEvent.INSERT );
		assertTrue( lis.firstRow == 0 );
		assertTrue( lis.lastRow == 0 );

		testTable.addContentsAtPosition( secondObj, 0, 0 );

		assertTrue( testTable.getNumEntries() == 2 );

		assertTrue( lis.numTimesCalled == 4 );
		assertTrue( lis.sourceObject == testTable );
		assertTrue( lis.eventType == TableModelEvent.INSERT );
		assertTrue( lis.firstRow == 1 );
		assertTrue( lis.lastRow == 1 );
	}

	// Known to not work yet
	/*
	public void testResizeTable() throws ContentsAlreadyAddedException, TableCellFullException, TableResizeException, TableIndexOutOfBoundsException, DatastoreException
	{
		// For debugging
		final TablePrinter<AudioComponent, AudioComponentProperties> printer = new TablePrinter<AudioComponent, AudioComponentProperties>();

		final TableInterface<AudioComponent, AudioComponentProperties> table = new Table<AudioComponent, AudioComponentProperties>(2, 2);
		final DJMixerComponent testComponent = new DJMixerComponent();
		table.addContentsAtPosition( testComponent, 0, 0 );
		assertTrue( table.getNumEntries() == 1 );
		log.debug("Initial table for resizing: ");
		printer.printTableContents( table );

		assertTrue( testComponent == table.getContentsAtPosition( 0, 0 ) );

		// Now resize it too small based on dimensions
		boolean thrown = false;
		try
		{
			table.resize( 0, -1 );
		}
		catch(final TableResizeException e )
		{
			thrown = true;
		}
		assertTrue( thrown );

		assertTrue( testComponent == table.getContentsAtPosition( 0, 0 ) );

		thrown = false;
		// Now resize it too small based on contents in the table
		try
		{
			table.resize( 1, 1 );
		}
		catch(final TableResizeException e )
		{
			thrown = true;
		}
		assertTrue( thrown );

		assertTrue( testComponent == table.getContentsAtPosition( 0, 0 ) );

		// Now a valid resize.
		log.debug("Table before resize: ");
		printer.printTableContents( table );

		table.resize( 6, 6 );

		assertTrue( table.getNumCols() == 6 );
		assertTrue( table.getNumRows() == 6 );
		assertTrue( table.getNumEntries() == 1 );
		assertTrue( testComponent == table.getContentsAtPosition( 0, 0 ) );

		log.debug("Table after resize: ");
		printer.printTableContents( table );
		// Finally stored something later on in the table to verify that it really is that big.
		final DJMixerComponent secondObj = new DJMixerComponent();
		table.addContentsAtPosition( secondObj, 1, 4 );
		assertTrue( table.getNumEntries() == 2 );
		log.debug("Table after adding second object: ");
		printer.printTableContents( table );
		assertTrue( secondObj == table.getContentsAtPosition( 1,4 ) );
		assertTrue( secondObj == table.getContentsAtPosition( 2,4 ) );
		assertTrue( secondObj == table.getContentsAtPosition( 1,5 ) );
		assertTrue( secondObj == table.getContentsAtPosition( 2,5 ) );
	}
	*/

	// Know to not work yet
	/*
	public void testInsertColumn() throws ContentsAlreadyAddedException, TableCellFullException, TableIndexOutOfBoundsException, TableResizeException, DatastoreException
	{
		TablePrinter<AudioComponent, AudioComponentProperties> printer =
			new TablePrinter<AudioComponent, AudioComponentProperties>();

		TableInterface<AudioComponent, AudioComponentProperties> table =
			new Table<AudioComponent, AudioComponentProperties>(4, 4);

		DJMixerComponent componentOne = new DJMixerComponent();

		DJMixerComponent componentTwo = new DJMixerComponent();

		// Add a component that spans between columns 0 and 1 - this component shouldn't move
		table.addContentsAtPosition( componentOne, 0, 0 );

		// Add a component that starts at column 1 and spans to 2 - this component should move to column 2 with the insert
		table.addContentsAtPosition( componentTwo, 1,2 );

		log.debug("Table before column insert:");
		printer.printTableContents(table);

		table.insertColumn( 1 );

		log.debug("Table after column insert:");
		printer.printTableContents(table);

		assertTrue( table.getNumCols() == 3 );

		// Verify that the component at zero zero is still there
		assertTrue( table.getNumEntries() == 2 );
		assertTrue( table.getContentsAtPosition( 0, 0 ) == componentOne );
		// Verify that the second component has been moved one to the right
		assertTrue( table.getContentsAtPosition( 2,2 ) == componentTwo );
		TablePosition c2NewPosition = table.getContentsOriginReturnNull( componentTwo );
		assertTrue( c2NewPosition.x == 2 );
		assertTrue( c2NewPosition.y == 2 );

		// Now check the listener gets insert column events, too
		TestTableListener tl = new TestTableListener();
		table = new Table<AudioComponent, AudioComponentProperties>(4, 4);
		table.addListener( tl );
		table.addContentsAtPosition( componentOne, 2, 2 );
		// Verify we got the inset event for the contents
		assertTrue( tl.eventType == TableModelEvent.INSERT );
		assertTrue( tl.firstRow == 0 );
		assertTrue( tl.lastRow == 0 );
		assertTrue( tl.numTimesCalled == 1 );
	}
	*/

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
	}

}
