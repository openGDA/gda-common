/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.observable;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link ObservableComponent}.
 */
public class ObservableComponentTest {

	/**
	 * Test notifyIObservers swallows and does not cause any exceptions
	 */
	@Test
	public void testSwallowedExceptionLoggingDoesNotCauseException() {
		ObservableComponent oc = new ObservableComponent();
		oc.addIObserver(new IObserver() {
			@Override
			public void update(Object source, Object arg) {
				throw new RuntimeException("should be swallowed");
			}
		});

		// notifyIObservers previously caused a NullPointerException
		// when it logged an exception and theObserved was null by
		// calling toString on theObserved
		oc.notifyIObservers(null, "\"theObserved is null\"");

		oc.notifyIObservers(new Integer(1), "theObserved is not null");
	}

	/**
	 * Tests that all observers of an observable component receive an update,
	 * even if one of the observers deletes itself from the list of
	 * observers when it gets an update.
	 */
	@Test
	public void testAllObserversGetUpdateIfAnObserverDeletesItself() {

		TestObserver[] observers = new TestObserver[] {
			new TestObserver("1"),
			new DeleteSelfObserver("2"),
			new TestObserver("3"),
			new TestObserver("4")
		};

		ObservableComponent oc = new ObservableComponent();
		for (IObserver observer : observers) {
			oc.addIObserver(observer);
		}

		oc.notifyIObservers(oc, "test");

		for (TestObserver observer : observers) {
			assertTrue("Observer '" + observer.getName() + "' didn't receive an update", observer.receivedUpdate());
		}
	}

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void testAddingNullObserverThrows() {
		ObservableComponent oc = new ObservableComponent();

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Can't add a null observer");
		oc.addIObserver(null);
	}

	@Test
	public void testRemovingNullObserverThrows() {
		ObservableComponent oc = new ObservableComponent();

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Can't delete a null observer");
		oc.deleteIObserver(null);
	}

	@Test
	public void testIsBeingObserved() {
		ObservableComponent oc = new ObservableComponent();

		// New shouldn't have any observers
		assertFalse(oc.isBeingObserved());

		// Add an observer
		oc.addIObserver(new TestObserver("test observer"));

		// Should now be being observed
		assertTrue(oc.isBeingObserved());

		// Remove all observers
		oc.deleteIObservers();

		// Now shouldn't be being observed again
		assertFalse(oc.isBeingObserved());
	}

	@Test
	public void testNumberOfObservers() {
		ObservableComponent oc = new ObservableComponent();

		// Initially no observers
		assertThat(oc.getNumberOfObservers(), is(equalTo(0)));

		// Add one observer
		oc.addIObserver(mock(IObserver.class));

		// Should now have one
		assertThat(oc.getNumberOfObservers(), is(equalTo(1)));
	}

	@Test
	public void testGettingObservers() {
		ObservableComponent oc = new ObservableComponent();

		// Initially no observers
		assertThat(oc.getObservers(), is(empty()));

		// Add one observer
		IObserver observer = mock(IObserver.class);
		oc.addIObserver(observer);

		// Should now have only that one
		assertThat(oc.getObservers(), contains(observer));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testGettingObserversReturnsUnmodifiableView() {
		ObservableComponent oc = new ObservableComponent();

		// Try to add an observer should throw
		oc.getObservers().add(mock(IObserver.class));
	}

	/**
	 * Implementation of {@link IObserver} for testing, which has an ID and a
	 * flag for indicating whether an update was received.
	 */
	static class TestObserver implements IObserver {

		protected String name;

		protected boolean receivedUpdate;

		public TestObserver(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		@Override
		public void update(Object theObserved, Object changeCode) {
			System.out.println("[" + name + "] received update: " + changeCode);
			receivedUpdate = true;
		}

		public boolean receivedUpdate() {
			return receivedUpdate;
		}
	}

	/**
	 * An {@link IObserver} that deletes itself from the observable's observers
	 * when it receives an update.
	 */
	static class DeleteSelfObserver extends TestObserver {

		public DeleteSelfObserver(String name) {
			super(name);
		}

		@Override
		public void update(Object theObserved, Object changeCode) {
			super.update(theObserved, changeCode);
			System.out.println("[" + name + "] deleting self...");
			((IObservable) theObserved).deleteIObserver(this);
		}
	}

}
