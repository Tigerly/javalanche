/*
* Copyright (C) 2011 Saarland University
* 
* This file is part of Javalanche.
* 
* Javalanche is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Javalanche is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Lesser Public License for more details.
* 
* You should have received a copy of the GNU Lesser Public License
* along with Javalanche.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.junit.internal.runners;

import junit.extensions.TestDecorator;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.junit.runner.Describable;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class JUnit38ClassRunner extends Runner implements Filterable, Sortable {
	private final class OldTestClassAdaptingListener implements TestListener {
		private final RunNotifier fNotifier;

		private OldTestClassAdaptingListener(RunNotifier notifier) {
			fNotifier = notifier;
		}

		public void endTest(Test test) {
			fNotifier.fireTestFinished(asDescription(test));
		}

		public void startTest(Test test) {
			fNotifier.fireTestStarted(asDescription(test));
		}

		// Implement junit.framework.TestListener
		public void addError(Test test, Throwable t) {
			Failure failure = new Failure(asDescription(test), t);
			fNotifier.fireTestFailure(failure);
		}

		private Description asDescription(Test test) {
			if (test instanceof Describable) {
				Describable facade = (Describable) test;
				return facade.getDescription();
			}
			return Description.createTestDescription(getEffectiveClass(test),
					getName(test));
		}

		private Class<? extends Test> getEffectiveClass(Test test) {
			return test.getClass();
		}

		private String getName(Test test) {
			if (test instanceof TestCase)
				return ((TestCase) test).getName();
			else
				return test.toString();
		}

		public void addFailure(Test test, AssertionFailedError t) {
			addError(test, t);
		}
	}

	private Test fTest;

	public JUnit38ClassRunner(Class<?> klass) {
		this(new TestSuite(klass.asSubclass(TestCase.class)));
	}

	public JUnit38ClassRunner(Test test) {
		super();
		setTest(test);
	}

	@Override
	public void run(RunNotifier notifier) {
		TestResult result = new TestResult();
		result.addListener(createAdaptingListener(notifier));
		getTest().run(result);
	}

	public TestListener createAdaptingListener(final RunNotifier notifier) {
		return new OldTestClassAdaptingListener(notifier);
	}

	@Override
	public Description getDescription() {
		return makeDescription(getTest());
	}

	private static Description makeDescription(Test test) {
		if (test instanceof TestCase) {
			TestCase tc = (TestCase) test;
			return Description.createTestDescription(tc.getClass(),
					tc.getName());
		} else if (test instanceof TestSuite) {
			TestSuite ts = (TestSuite) test;
			String name = ts.getName() == null ? createSuiteDescription(ts)
					: ts.getName();
			Description description = Description.createSuiteDescription(name);
			int n = ts.testCount();
			for (int i = 0; i < n; i++) {
				Description made = makeDescription(ts.testAt(i));
				description.addChild(made);
			}
			return description;
		} else if (test instanceof Describable) {
			Describable adapter = (Describable) test;
			return adapter.getDescription();
		} else if (test instanceof TestDecorator) {
			TestDecorator decorator = (TestDecorator) test;
			return makeDescription(decorator.getTest());
		} else {
			// This is the best we can do in this case
			return Description.createSuiteDescription(test.getClass());
		}
	}

	private static String createSuiteDescription(TestSuite ts) {
		int count = ts.countTestCases();
		String example = count == 0 ? "" : String.format(" [example: %s]",
				ts.testAt(0));
		return String.format("TestSuite with %s tests%s", count, example);
	}

	public void filter(Filter filter) throws NoTestsRemainException {
		Test test = getTest();
		Test filteredTest = filter(filter, test);
		int testCases = filteredTest.countTestCases();
		if (testCases == 0) {
			throw new NoTestsRemainException();
		}
		setTest(filteredTest);
	}

	private Test filter(Filter filter, Test testToFilter)
			throws NoTestsRemainException {
		if (testToFilter instanceof Filterable) {
			Filterable adapter = (Filterable) testToFilter;
			adapter.filter(filter);
		} else if (testToFilter instanceof TestSuite) {
			TestSuite suite = (TestSuite) testToFilter;
			TestSuite filtered = new TestSuite(suite.getName());
			int n = suite.testCount();
			for (int i = 0; i < n; i++) {
				Test test = suite.testAt(i);
				if (test instanceof TestSuite) {
					test = filter(filter, test);
					if (test.countTestCases() > 0)
						filtered.addTest(test);
				} else if (filter.shouldRun(makeDescription(test)))
					filtered.addTest(test);
			}
			return filtered;
		}
		return testToFilter;
	}

	public void sort(Sorter sorter) {
		if (getTest() instanceof Sortable) {
			Sortable adapter = (Sortable) getTest();
			adapter.sort(sorter);
		}
	}

	private void setTest(Test test) {
		fTest = test;
	}

	private Test getTest() {
		return fTest;
	}
}
