/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.elasticsearch.querydoge;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.node.Node;
import org.elasticsearch.querydoge.helper.HttpHelper;
import org.json.JSONObject;

/**
 * TermListFacetTest
 * 
 * 
 */
public class QueryDogeTest extends TestCase {
	private Node node;
	private final String[] indices = {"day_1", "day_2", "day_3"};
	private final String all_indices_str = "day_1,day_2,day_3";
	private final String type = "test_type";
	private final String cluster = "QueryCacheTest";
	private final String path_data = "target/unit_tests";
	private static final AtomicInteger counter = new AtomicInteger(0);
	private final boolean uselocalhost = false; // helper method for client()
												// will connect you to localhost
												// if this is true
	private final Random _random = new Random(0);
	private final int randomElementsMultiplier = 10; // use this to help
														// control how many
														// random elements to
														// create
	private final int numOfElements = randomElementsMultiplier + _random.nextInt(10);
	private final int numberOfShards = 1;
	private final int numberOfReplicas = 0;
	private List<String> randomStrings = new ArrayList<String>();
	private Set<String> uniqText = new HashSet<String>();
	private Set<String> uniqAllText = new HashSet<String>();

	private HttpHelper httpHelper = new HttpHelper("http://localhost:9209/");

	@Override
	protected void tearDown() throws Exception {
		System.out.println("tearDown : START");
		super.tearDown();
		try{
			deleteAllIndices(client(), indices[0]);
			deleteAllIndices(client(), indices[1]);
			deleteAllIndices(client(), indices[2]);
			node.stop();
			node.close();
		} catch (Exception e) {
			System.out.println("tearDown : Exception : " + e.toString());
		} finally {
			removeFile(path_data);
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Settings settings = ImmutableSettings.settingsBuilder().put("http.port", 9209).put("node.http.enabled", true)
				.put("index.gateway.type", "none").put("index.number_of_shards", numberOfShards)
				.put("index.number_of_replicas", numberOfReplicas).put("path.data", path_data).put("index.cache.field.type", "soft")
				.build();

		node = nodeBuilder().local(true).settings(settings).clusterName(cluster).node();
		node.start();
		client().admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();

		// mapping and index create
		//
		XContentBuilder mapping = this.mapping(type);
		deleteAllIndices(client(), indices[0]);
		deleteAllIndices(client(), indices[1]);
		deleteAllIndices(client(), indices[2]);
		createIndex(indices[0], client(), mapping);
		createIndex(indices[1], client(), mapping);
		createIndex(indices[2], client(), mapping);
		runStandardPutsAndFlush(indices[0]);
		runStandardPutsAndFlush(indices[1]);
		runStandardPutsAndFlush(indices[2]);
	}

	/********************************************************************************/
	
	public void testSetUpTearDown()
	{
		//so fail, much problems, amaze you cant pass test
	}
	
	public void testRestEndPointTDDHelper() throws Exception {
		// just run a query man, just like run it and print it, use this to test with during dev
		JSONObject bodySpecialSearch = new JSONObject(httpHelper.postAndReturnBody(all_indices_str +"/_dogesearch", this.jsonFileAsString("src/test/resources/query_all_facet_multi.json")));
		System.out.println(bodySpecialSearch.toString());
	}

	/*************************************************************************************************/
	
	public void testQueryAllFacetMultiple() throws Exception {
		JSONObject answer = new JSONObject(httpHelper.postAndReturnBody(all_indices_str +"/_dogesearch", this.jsonFileAsString("src/test/resources/query_all_facet_multi.json")));
		assertEquals(answer.getJSONObject("hits").get("total"),this.indices.length*10);
		assertEquals(answer.getJSONObject("_shards").get("total"), this.numberOfShards*this.indices.length);
		assertEquals(answer.getJSONObject("_shards").get("failed"), 0);
		assertEquals(answer.getJSONObject("_shards").get("successful"), this.numberOfShards*this.indices.length);
	}
	
	public void testQueryAll() throws Exception {
		JSONObject answer = new JSONObject(httpHelper.postAndReturnBody(all_indices_str +"/_amazesearch", this.jsonFileAsString("src/test/resources/query_all.json")));
		assertEquals(answer.getJSONObject("hits").get("total"),this.indices.length*10);
		assertEquals(answer.getJSONObject("_shards").get("total"), this.numberOfShards*this.indices.length);
		assertEquals(answer.getJSONObject("_shards").get("failed"), 0);
		assertEquals(answer.getJSONObject("_shards").get("successful"), this.numberOfShards*this.indices.length);
	}
	
	public void testQueryAllFacetHist() throws Exception {
		JSONObject answer = new JSONObject(httpHelper.postAndReturnBody(all_indices_str +"/_sosearch", this.jsonFileAsString("src/test/resources/query_facet_histogram.json")));
		assertEquals(answer.getJSONObject("hits").get("total"),this.indices.length*10);
		assertEquals(answer.getJSONObject("_shards").get("total"), this.numberOfShards*this.indices.length);
		assertEquals(answer.getJSONObject("_shards").get("failed"), 0);
		assertEquals(answer.getJSONObject("_shards").get("successful"), this.numberOfShards*this.indices.length);
	}
	
	public void testQueryAllRangeFilterHist() throws Exception {
		JSONObject answer = new JSONObject(httpHelper.postAndReturnBody(all_indices_str +"/_dogesearch", this.jsonFileAsString("src/test/resources/query_filter_range_facet_histogram.json")));
		System.out.println(answer);
		assertEquals(answer.getJSONObject("_shards").get("total"), this.numberOfShards*this.indices.length);
		assertEquals(answer.getJSONObject("_shards").get("failed"), 0);
		assertEquals(answer.getJSONObject("_shards").get("successful"), this.numberOfShards*this.indices.length);
	}
	
	/******/
		
	public void testRestEndPointBasics() throws Exception {
		JSONObject bodySpecialSearch = new JSONObject(httpHelper.getAndReturnBody(all_indices_str+"/_dogesearch"));
		JSONObject bodyRegularSearch = new JSONObject(httpHelper.getAndReturnBody(all_indices_str+"/_search"));
		assertEquals(bodyRegularSearch.getJSONObject("hits").get("total"), bodySpecialSearch.getJSONObject("hits").get("total"));

		bodySpecialSearch = new JSONObject(httpHelper.getAndReturnBody(all_indices_str+"/_sosearch"));
		bodyRegularSearch = new JSONObject(httpHelper.getAndReturnBody(all_indices_str+"/_search"));
		assertEquals(bodyRegularSearch.getJSONObject("hits").get("total"), bodySpecialSearch.getJSONObject("hits").get("total"));
		
		bodySpecialSearch = new JSONObject(httpHelper.postAndReturnBody(all_indices_str+"/_dogesearch", "{such_query:{very_match_all:{}}}"));
		bodyRegularSearch = new JSONObject(httpHelper.postAndReturnBody(all_indices_str+"/_search", "{query:{match_all:{}}}"));
		assertEquals(bodyRegularSearch.getJSONObject("hits").get("total"), bodySpecialSearch.getJSONObject("hits").get("total"));
		
		bodySpecialSearch = new JSONObject(httpHelper.postAndReturnBody(all_indices_str+"/_amazesearch", "{such_query:{very_match_all:{}}}"));
		bodyRegularSearch = new JSONObject(httpHelper.postAndReturnBody(all_indices_str+"/_search", "{query:{match_all:{}}}"));
		assertEquals(bodyRegularSearch.getJSONObject("hits").get("total"), bodySpecialSearch.getJSONObject("hits").get("total"));
		
		bodySpecialSearch = new JSONObject(httpHelper.postAndReturnBody(all_indices_str+"/_muchsearch", "{such_query:{very_match_all:{}}}"));
		bodyRegularSearch = new JSONObject(httpHelper.postAndReturnBody(all_indices_str+"/_search", "{query:{match_all:{}}}"));
		assertEquals(bodyRegularSearch.getJSONObject("hits").get("total"), bodySpecialSearch.getJSONObject("hits").get("total"));
	}

	/*****************************************************************************************************/
	/*****************************************************************************************************/

	public XContentBuilder mapping(String type) throws Exception {
		XContentBuilder xbMapping = XContentFactory.jsonBuilder().startObject()
				.startObject(type)
					.startObject("properties")
						.startObject("_timestamp").field("enabled", true).field("path","ts").endObject()
						.startObject("name").field("type", "string").endObject()
						.startObject("gender").field("type", "string").endObject()
					.endObject()
				.endObject()
			.endObject();
		
		System.out.println("Mapping : \n" + xbMapping.string());
		return xbMapping;
	}
	
	private void createIndex(String index, Client c, XContentBuilder settings) throws Exception {
		try {
			CreateIndexResponse cir = null;
			
			if (settings != null) {
				cir = c.admin().indices().prepareCreate(index).setSettings(settings.string()).execute().actionGet();
			} else {
				cir = c.admin().indices().prepareCreate(index).execute().actionGet();
			}
			
			if (!cir.isAcknowledged()) {
				System.out.println("Create was not acknowledged : " + index);
				throw new Exception("Failed Create of Index : " + index);
			}

			// wait for green after index create
			//
			c.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();

		} catch (Exception e) { // be sure to tear down if we fail during setup,
								// to prevent index already created issues from
								// repeating
			deleteAllIndices(c, index);
			System.out.println("Error during Index Create : " + e.toString());
			throw new Exception("Error during Setup : ", e);
		}
	}

	public void removeFile(String name) throws Exception {
		String path = new java.io.File(".").getCanonicalPath();
		File f = new File(path + File.separator + name);
		FileSystemUtils.deleteRecursively(f);
	}

	private String jsonFileAsString(String jsonFileName) {
		try {
			return new Scanner(new File(jsonFileName)).useDelimiter("\\Z").next();
		} catch (Exception e) {
			e.printStackTrace(System.out);
			fail("this test failed");
		}
		
		return null;
	}
	
	/**
	 * deleteAllIndices
	 * 
	 * @throws Exception
	 */
	private void deleteAllIndices(Client c, String index) throws Exception {
		IndicesExistsResponse af = c.admin().indices().exists(new IndicesExistsRequest(index)).actionGet();
		if (af.isExists()) {
			DeleteIndexResponse dir = c.admin().indices().delete(new DeleteIndexRequest(index)).actionGet();

			if (!dir.isAcknowledged()) {
				throw new Exception("Failed Delete of Index : " + index);
			}
		}

		// wait for green after delete
		//
		c.admin().cluster().prepareHealth().setWaitForGreenStatus().execute().actionGet();
	}

	/**
	 * runStandardPutsAndFlush generate random data, count it for comparison
	 * later call es with puts of the generated data
	 * 
	 * @param index
	 * @throws IOException
	 */
	public void runStandardPutsAndFlush(String index) throws IOException {
		randomStrings = new ArrayList<String>();
		randomStrings = this.generateRandomStrings(numOfElements);

		List<Map<String, Object>> listofmaps = this.generateData(randomStrings, numOfElements);

		for (int i = 0; i < numOfElements; i++) {
			this.putSimpleData(index, type, listofmaps.get(i));
		}

		for (String s : randomStrings) {
			uniqText.add(s);
		}

		uniqAllText.addAll(uniqText);

		flush(index);

		// our index has all of elements
		assertEquals(numOfElements, countAll(index));
	}

	/**
	 * putSimpleNestedData
	 * 
	 * @param index
	 * @param type
	 * @param data
	 * @throws IOException
	 */
	private void putSimpleData(String index, String type, Map<String, Object> data) throws IOException {
		XContentBuilder builder = XContentFactory.jsonBuilder().startObject().
				field("myid", data.get("id")).
				field("name", data.get("name")).
				field("ts", data.get("ts")).
				field("gender", data.get("gender")).
				field("int", data.get("int")).
				field("float", data.get("float")).
				field("double", data.get("double")).
				field("long", data.get("long")).
			endObject();

		// put the data in es
		client().prepareIndex(index, type, (String) data.get("id")).setRefresh(true).setRouting((String) data.get("id")).setSource(builder)
				.execute().actionGet();
		client().prepareGet(index, type, (String) data.get("id")).execute().actionGet();
	}

	/**
	 * @return
	 */
	private long generateRandomTimeStamp() {
		long offset = Timestamp.valueOf("2013-01-01 00:00:00").getTime();
		long end = Timestamp.valueOf("2014-01-01 00:00:00").getTime();
		long diff = end - offset + 1;
		Timestamp rand = new Timestamp(offset + (long) (Math.random() * diff));
		return rand.getTime();
	}

	/**
	 * generateRandomStrings
	 * 
	 * @param numberOfWords
	 * @return
	 */
	private List<String> generateRandomStrings(int numberOfWords) {
		return this.generateRandomStrings(numberOfWords, true);
	}

	/**
	 * generateRandomStrings
	 * 
	 * @param numberOfWords
	 * @param lowercaseonly
	 * @return
	 */
	private List<String> generateRandomStrings(final int numberOfWords, boolean lowercaseonly) {
		int cardinalityOfLetters = 5;
		final String[] randomStrings = new String[numberOfWords];
		List<String> myList = new ArrayList<>();
		for (int i = 0; i < numberOfWords; i++) {
			final char[] word = new char[_random.nextInt(3) + 3];

			for (int j = 0; j < word.length; j++) {
				if (lowercaseonly) {
					word[j] = (char) ('a' + _random.nextInt(cardinalityOfLetters));
				} else {
					if (_random.nextInt(10) > 5) {
						word[j] = (char) ('A' + _random.nextInt(cardinalityOfLetters));
					} else {
						word[j] = (char) ('a' + _random.nextInt(cardinalityOfLetters));
					}
				}
			}

			randomStrings[i] = new String(word);
			myList.add(randomStrings[i]);
		}

		return myList;
	}

	/**
	 * @param index
	 */
	private void flush(String index) {
		// flush it to ensure data is present
		client().admin().indices().flush(new FlushRequest(index).force(true)).actionGet();
	}

	/**
	 * generateSimpleNestedData
	 * 
	 * @param count
	 * @return
	 * @throws IOException
	 */
	private List<Map<String, Object>> generateData(List<String> randomParentNames, int count) throws IOException {
		List<Map<String, Object>> simpleNestedList = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < randomParentNames.size(); i++) {
			String stringID = String.valueOf(newID());
			Map<String, Object> parentData = new HashMap<String, Object>();
			parentData.put("id", stringID);
			
			parentData.put("int", i % 50);
			parentData.put("float", (i % 50)*2.56f);
			parentData.put("long", (i % 50)*1000L);
			parentData.put("double", (i % 50)*1000.0d);
			
			parentData.put("name", randomParentNames.get(i));
			parentData.put("gender", ((i & 1) == 0) ? "M" : "F");
			parentData.put("ts", this.generateRandomTimeStamp());

			simpleNestedList.add(parentData);
		}

		return simpleNestedList;
	}

	private static int newID() {
		return counter.getAndIncrement();
	}

	private long countAll(String index) {
		return client().prepareCount(index).execute().actionGet().getCount();
	}

	private Client client() {
		if (uselocalhost) {
			return localclient();
		} else {
			return node.client();
		}
	}

	/**
	 * helper if you want to hit local es install to do some testing
	 * 
	 * @return
	 */
	private Client localclient() {
		Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").build();
		return new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress("localhost", 9300));
	}

}
