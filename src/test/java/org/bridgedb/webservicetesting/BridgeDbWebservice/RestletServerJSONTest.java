// Copyright 2023 Egon Willighagen
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.bridgedb.webservicetesting.BridgeDbWebservice;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RestletServerJSONTest {

	private static int port = 1074;
	private static RestletServer server;

    @BeforeAll
    public static void startServer() throws IOException {
        // set up a test Derby file
        File derbyFile = File.createTempFile("bdb", "bridge");
        derbyFile.deleteOnExit();
        InputStream stream = RestletServerJSONTest.class.getClassLoader().getResourceAsStream("humancorona-2021-11-27.bridge");
        FileOutputStream derbyStream = new FileOutputStream(derbyFile);
        stream.transferTo(derbyStream);
        derbyStream.close();
        stream.close();

        // set up the GDB config file
        File configFile = File.createTempFile("gdb", "config");
        configFile.deleteOnExit();
        FileOutputStream outputStream = new FileOutputStream(configFile);
        BufferedOutputStream bufferStream = new BufferedOutputStream(outputStream);
        String configFileContent = "*\t" +  derbyFile.getAbsolutePath();
        bufferStream.write(configFileContent.getBytes());
        bufferStream.close();
        outputStream.close();

        // set up the REST service
        RestletServerJSONTest.server = new RestletServer();
        RestletServerJSONTest.server.run(port, configFile, false, false);
    }

    @AfterAll
    public static void stopServer() {
    	RestletServerJSONTest.server.stop();
    }

    @Test
    public void testSources() throws Exception {
    	String reply =  TestHelper.getJSONContent("http://127.0.0.1:" + port + "/Human/sourceDataSources");
        assertTrue(reply.contains("Wikidata"));
        JSONTokener tokener = new JSONTokener(reply);
        JSONObject root = new JSONObject(tokener);
        JSONArray sources = (JSONArray)root.get("supportedSourceDatasources");
        assertNotNull(sources);
        assertTrue(sources.toList().contains("Wikidata"));
    }

    @Test
    public void testSources_UnknownSpecies() throws Exception {
    	String reply =  TestHelper.getJSONContent("http://127.0.0.1:" + port + "/Catz/sourceDataSources");
        assertTrue(reply.contains("<html>"));
        assertTrue(reply.contains("Unknown organism"));
    }

    @Test
    public void testProperties() throws Exception {
        String reply =  TestHelper.getJSONContent("http://127.0.0.1:" + port + "/Human/properties");
        System.out.println(reply);
        JSONTokener tokener = new JSONTokener(reply);
        JSONObject root = new JSONObject(tokener);
        String source = (String)root.get("DATASOURCENAME");
        assertNotNull(source);
        assertTrue(source.equals("Wikidata"));
        String version = (String)root.get("SCHEMAVERSION");
        assertNotNull(version);
        assertTrue(version.equals("3"));
    }

}
