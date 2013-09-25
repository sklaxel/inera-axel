package se.inera.axel.test.fitnesse.fixtures;

public class SystemTestsTearDown {

	public SystemTestsTearDown() throws Exception {
		super();
		SystemTestsSetUp.getContext().stop();
	}

}
