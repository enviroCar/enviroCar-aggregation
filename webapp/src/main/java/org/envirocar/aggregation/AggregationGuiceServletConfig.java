/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */
package org.envirocar.aggregation;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

public class AggregationGuiceServletConfig extends GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		ServiceLoader<Module> loader = ServiceLoader.load(Module.class);

		List<Module> modules = new ArrayList<Module>();
		for (Module module : loader) {
			modules.add(module);
		}

		modules.add(new ServletModule() {

			@Override
			protected void configureServlets() {
				serve("/receiveTrack/*").with(ReceiveTracksServlet.class);
				serve("/receiveTrack").with(ReceiveTracksServlet.class);
				serve(AggregatedTracksServlet.PATH.concat("/*")).with(AggregatedTracksServlet.class);
				serve(AggregatedTracksServlet.PATH).with(AggregatedTracksServlet.class);
			}

		});

		return Guice.createInjector(modules);
	}
}
