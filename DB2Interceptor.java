package com.chrysler.rp.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.EmptyInterceptor;

public class DB2Interceptor extends EmptyInterceptor {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(DB2Interceptor.class);

	@Override
	public String onPrepareStatement(String str) {
		String compstr = str.toLowerCase();

		// Check if we're dealing with a simple select statement
		if (compstr.matches("^select.*") && !compstr.matches(".*for update.*")) {
			if (!compstr.matches(".*with ur.*") && !compstr.matches(".*for fetch only.*")) {
				str += " with ur for fetch only ";
				logger.debug("Appending \"WITH UR FOR FETCH ONLY\" to query.");
			}
		}
		return str;
	}
}
