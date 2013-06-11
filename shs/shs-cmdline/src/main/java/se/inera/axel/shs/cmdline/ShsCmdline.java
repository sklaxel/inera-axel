/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.cmdline;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class ShsCmdline {

	@Parameter(names = "--help", help = true)
	private boolean help = false;

	@Parameter(names = {"-F", "--configFile"}, description = "URI to custom property file. I.e. 'file:my.properties'")
	private String configFile = "classpath:shs-cmdline.properties";


	public static void main(String[] args) throws Throwable	 {

		ShsSendCommand shsSendCommand = new ShsSendCommand();
		ShsRequestCommand shsRequestCommand = new ShsRequestCommand();

		ShsCmdline cmdline = new ShsCmdline();
		JCommander cmd = new JCommander(cmdline);
		cmd.addCommand("send", shsSendCommand);
		cmd.addCommand("request", shsRequestCommand);
		cmd.setProgramName("shs");

		try {
			cmd.parse(args);
		} catch (com.beust.jcommander.ParameterException e) {
			StringBuilder usage = new StringBuilder();

			if (cmd.getParsedCommand() != null)
				cmd.usage(cmd.getParsedCommand(), usage);
			else
				cmd.usage();

			System.err.println(usage);

			if (cmdline.help == true)
				return;

			throw e;
		}


		if (cmdline.help == true) {
			if (cmd.getParsedCommand() != null)
				cmd.usage(cmd.getParsedCommand());
			else
				cmd.usage();
			return;
		}

		System.setProperty("configFile", cmdline.configFile);

		if ("send".equals(cmd.getParsedCommand())) {
			shsSendCommand.execute();
		} else if ("request".equals(cmd.getParsedCommand())) {
			shsRequestCommand.execute();
		}

		if (cmd.getParsedCommand() == null) {
			StringBuilder usage = new StringBuilder();
			cmd.usage(usage);

			System.err.println(usage);
			throw new RuntimeException("Must specify command, e.g. 'send' or 'request'");
		}
	}


}