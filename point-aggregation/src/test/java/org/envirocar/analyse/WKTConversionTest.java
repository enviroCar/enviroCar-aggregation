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
package org.envirocar.analyse;

import static org.junit.Assert.assertTrue;

import org.envirocar.analyse.util.Utils;
import org.junit.Test;

public class WKTConversionTest {

	
	@Test
	public void testWKTConversion(){
		
		double[] convertedWKT = Utils.convertWKTPointToXY("POINT(7.607246 51.960133)");
		
		assertTrue(convertedWKT[0] == 7.607246);
		assertTrue(convertedWKT[1] == 51.960133);
		
	}
	
}
