package de.unisb.cs.st.javalanche.mutation.runtime.jmx;

import de.unisb.cs.st.javalanche.mutation.properties.MutationProperties;
import de.unisb.cs.st.javalanche.mutation.results.Mutation;
import de.unisb.cs.st.javalanche.mutation.runtime.testDriver.MutationTestListener;

public class JmxMutationTestListener implements MutationTestListener {

	private MutationMX bean;
	private MXBeanRegisterer beanReg = new MXBeanRegisterer();

	public void end() {
		// System.out.println("JmxMutationTestListener.end()");
		if (bean != null) {
			beanReg.unregister(bean);
		}
	}

	public void mutationEnd(Mutation mutation) {
	}

	public void mutationStart(Mutation mutation) {
		// System.out.println("JmxMutationTestListener.mutationStart()");
		if (bean != null) {
			bean.addMutation(mutation);
		}
	}

	public void start() {
		int runNumber = getRunNumber();
		if (runNumber >= 0) {
			bean = beanReg.registerMutationMXBean(runNumber);
		}

	}

	private static int getRunNumber() {
		String run = MutationProperties.MUTATION_FILE_NAME;
		int start = run.lastIndexOf('-') + 1;
		int end = run.lastIndexOf(".txt");
		if (start > -1 && end > 0) {
			String numberString = run.substring(start, end);
			int result = Integer.parseInt(numberString);
			return result;
		}
		return -1;
	}

	public void testEnd(String testName) {
	}

	public void testStart(String testName) {
		// System.out.println("JmxMutationTestListener.testStart()" + bean);
		if (bean != null) {
			bean.setTest(testName);
		}
	}

	public static void main(String[] args) {
		new JmxMutationTestListener().start();
	}
}