/**
 * Copyright 2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.envirocar.analyse.properties.GlobalProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Singleton
public class AggregatedTracksServlet extends HttpServlet {
    
    /**
     *
     */
    private static final Logger logger = LoggerFactory.getLogger(AggregatedTracksServlet.class);
    private static final long serialVersionUID = 1L;
    private static final String AGGREGATION_DATE = "aggregation_date";
    public static final String PATH = "/aggregatedTracks";
    private final Map<String, PostgresConnection> connections = new HashMap<>();
    private String aggregatedTracksTableName;
    private String query;
    private SimpleDateFormat df;
    private ObjectMapper om;
    private String trackQuery;
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        try {
            for (Properties p : Util.getAlgorithmConfigurations()) {
                String db = p.getProperty("database");
                this.connections.put(db, new PostgresConnection(db));
            }
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
            throw new ServletException(ex);
        }
        
        this.aggregatedTracksTableName = (String) GlobalProperties
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
        
        String[] dbAndTrackId = resolveDatabaseNameAndTrackId(subPath);
        

        String json;
        if (dbAndTrackId == null || dbAndTrackId.length == 0) {
            json = "{\"error\": \"no database name provided\"}";
            resp.setStatus(400);
        }
        else {
            if (dbAndTrackId.length >= 1) {
                String dbName = dbAndTrackId[0];
                String trackId = dbAndTrackId.length > 1 ? dbAndTrackId[1] : null;
                try {
                    if (trackId != null) {
                        json = createTrackExists(dbName, trackId);
                        resp.setStatus(200);
                    }
                    else {
                        json = createAggregatedTracksList(dbName);
                        resp.setStatus(200);
                    }
                } catch (SQLException e) {
                    throw new IOException(e);
                }
            }
            else {
                json = "{\"error\": \"invalid request structure\"}";
                resp.setStatus(400);
            }
        }
        

        /**
         * write the json
         */
        writer.append(json);
        
        writer.flush();
        writer.close();
        
    }
    
    protected String[] resolveDatabaseNameAndTrackId(String subPath) {
        String dbAndTrackId = null;
        if (!subPath.isEmpty() && !(subPath.length() == 1 && subPath.equals("/"))) {
            dbAndTrackId = subPath.startsWith("/") ? subPath.substring(1) : subPath;
        }

        if (dbAndTrackId == null || dbAndTrackId.isEmpty()) {
            return new String[0];
        }
        else {
            String[] array = dbAndTrackId.split("/");
            
            List<String> result = new ArrayList<>(array.length);
            for (int i = 0; i < array.length; i++) {
                array[i] = array[i].replace("/", "");
                if (!array[i].isEmpty()) {
                    result.add(array[i]);
                }
            }
            
            return result.toArray(new String[result.size()]);
        }
    }
    
    private String createTrackExists(String db, String trackId) throws SQLException {
        PostgresConnection conn = this.connections.get(db);
        
        if (conn == null) {
            return "{\"error\": \"invalid database\"}";
        }
        
        ResultSet rs = conn.executeQueryStatement(String.format(trackQuery, trackId));
        
        ObjectNode result = om.createObjectNode();
        
        result.put("aggregated", rs != null ? rs.next() : false);
        
        if (rs != null) {
            rs.close();
        }
        
        return result.toString();
    }
    
    private String createAggregatedTracksList(String db) throws SQLException {
        PostgresConnection conn = this.connections.get(db);
        
        if (conn == null) {
            return "{\"error\": \"invalid database\"}";
        }
        
        ResultSet rs = conn.executeQueryStatement(query);
        
        ArrayNode array = om.createArrayNode();
        ObjectNode object;
        String id;
        Timestamp ts;
        if (rs != null) {
            while (rs.next()) {
                object = om.createObjectNode();
                id = rs.getString("id");
                ts = rs.getTimestamp(AGGREGATION_DATE);

                object.put(id, df.format(new Date(ts.getTime())));

                array.add(object);
            }

            rs.close();
        }
        
        ObjectNode node = om.createObjectNode();
        
        node.put("tracks", array);
        return node.toString();
    }
    
}
