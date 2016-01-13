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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.envirocar.analyse.postgres.PostgresConnection;
import org.envirocar.analyse.properties.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;

@Singleton
public class AggregatedTracksServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String AGGREGATION_DATE = "aggregation_date";
	public static final String PATH = "/aggregatedTracks";
	private PostgresConnection connection;
	private String aggregatedTracksTableName;
	private String query;
	private SimpleDateFormat df;
	private ObjectMapper om;
	private String trackQuery;

	@Override
	public void init() throws ServletException {
		super.init();

		this.connection = new PostgresConnection();

		this.aggregatedTracksTableName = (String) Properties
				.getProperty("aggregatedTracksTableName");
		this.query = "SELECT * FROM " + this.aggregatedTracksTableName
				+ " ORDER BY id DESC";
		this.trackQuery = "SELECT * FROM "+ this.aggregatedTracksTableName +" WHERE id = '%s'";

		TimeZone tz = TimeZone.getTimeZone("UTC");
		df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(tz);
		
		om = new ObjectMapper();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		OutputStreamWriter writer = new OutputStreamWriter(
				resp.getOutputStream(), Charset.forName("UTF-8"));

		resp.setContentType("application/json");
		
		String uri = req.getRequestURI();
		String subPath = uri.substring(uri.indexOf(PATH) + PATH.length());
		
		String trackId = null;
		if (!subPath.isEmpty() && !(subPath.length() == 1 && subPath.equals("/"))) {
			trackId = subPath.startsWith("/") ? subPath.substring(1) : subPath;
		}
		
		String json;
		try {
			if (trackId != null) {
				json = createTrackExists(trackId);
			}
			else {
				json = createAggregatedTracksList();
			}
		} catch (SQLException e) {
			throw new IOException(e);
		}

		writer.append(json);

		writer.flush();
		writer.close();

		resp.setStatus(200);
	}

	private String createTrackExists(String trackId) throws SQLException {
		ResultSet rs = this.connection.executeQueryStatement(String.format(trackQuery, trackId));
		
		ObjectNode result = om.createObjectNode();

		result.put("aggregated", rs.next());
		
		rs.close();
		
		return result.toString();
	}
	
	private String createAggregatedTracksList() throws SQLException {
		ResultSet rs = this.connection.executeQueryStatement(query);

		ArrayNode array = om.createArrayNode();
		ObjectNode object;
		String id;
		Timestamp ts;
		while (rs.next()) {
			object = om.createObjectNode();
			id = rs.getString("id");
			ts = rs.getTimestamp(AGGREGATION_DATE);

			object.put(id, df.format(new Date(ts.getTime())));

			array.add(object);
		}

		rs.close();

		ObjectNode node = om.createObjectNode();

		node.put("tracks", array);
		return node.toString();
	}

}
