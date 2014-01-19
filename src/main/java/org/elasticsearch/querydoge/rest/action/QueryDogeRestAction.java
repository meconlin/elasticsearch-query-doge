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
package org.elasticsearch.querydoge.rest.action;

import static org.elasticsearch.common.unit.TimeValue.parseTimeValue;
import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestStatus.BAD_REQUEST;
import static org.elasticsearch.rest.action.support.RestXContentBuilder.restContentBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchOperationThreading;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.XContentRestResponse;
import org.elasticsearch.rest.XContentThrowableRestResponse;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.source.FetchSourceContext;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONArray;
import org.json.JSONObject;

import static org.elasticsearch.common.unit.TimeValue.parseTimeValue;
import static org.elasticsearch.rest.RestRequest.Method.GET;
import static org.elasticsearch.rest.RestRequest.Method.POST;
import static org.elasticsearch.rest.RestStatus.BAD_REQUEST;
import static org.elasticsearch.rest.action.support.RestXContentBuilder.restContentBuilder;
import static org.elasticsearch.search.suggest.SuggestBuilder.termSuggestion;


public class QueryDogeRestAction<X> extends BaseRestHandler  {
	static List<String> targets = Arrays.asList(
    		  "so", 
    		  "wow", 
    		  "amaze", 
    		  "very",
    		  "much",
    		  "such"
    		);
    
	@Inject
    public QueryDogeRestAction(Settings settings, Client client, RestController controller) {
        super(settings, client);
        controller.registerHandler(GET, "/_dogesearch", this);
        controller.registerHandler(POST, "/_dogesearch", this);
        controller.registerHandler(GET, "/{index}/_dogesearch", this);
        controller.registerHandler(POST, "/{index}/_dogesearch", this);
        controller.registerHandler(GET, "/{index}/{type}/_dogesearch", this);
        controller.registerHandler(POST, "/{index}/{type}/_dogesearch", this);
        
        controller.registerHandler(GET, "/_sosearch", this);
        controller.registerHandler(POST, "/_sosearch", this);
        controller.registerHandler(GET, "/{index}/_sosearch", this);
        controller.registerHandler(POST, "/{index}/_sosearch", this);
        controller.registerHandler(GET, "/{index}/{type}/_sosearch", this);
        controller.registerHandler(POST, "/{index}/{type}/_sosearch", this);
        
        controller.registerHandler(GET, "/_amazesearch", this);
        controller.registerHandler(POST, "/_amazesearch", this);
        controller.registerHandler(GET, "/{index}/_amazesearch", this);
        controller.registerHandler(POST, "/{index}/_amazesearch", this);
        controller.registerHandler(GET, "/{index}/{type}/_amazesearch", this);
        controller.registerHandler(POST, "/{index}/{type}/_amazesearch", this);     
        
        controller.registerHandler(GET, "/_muchsearch", this);
        controller.registerHandler(POST, "/_muchsearch", this);
        controller.registerHandler(GET, "/{index}/_muchsearch", this);
        controller.registerHandler(POST, "/{index}/_muchsearch", this);
        controller.registerHandler(GET, "/{index}/{type}/_muchsearch", this);
        controller.registerHandler(POST, "/{index}/{type}/_muchsearch", this); 
    }
			    
    public void handleRequest(final RestRequest request, final RestChannel channel) {
        SearchRequest searchRequest;
        try {
            searchRequest = QueryDogeRestAction.muchParsingVeryRequest(request);
            searchRequest.listenerThreaded(false);
            SearchOperationThreading operationThreading = SearchOperationThreading.fromString(request.param("operation_threading"), null);
            if (operationThreading != null) {
                if (operationThreading == SearchOperationThreading.NO_THREADS) {
                    // since we don't spawn, don't allow no_threads, but change it to a single thread
                    operationThreading = SearchOperationThreading.SINGLE_THREAD;
                }
                searchRequest.operationThreading(operationThreading);
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("failed to parse search request parameters", e);
            }
            try {
                XContentBuilder builder = restContentBuilder(request);
                channel.sendResponse(new XContentRestResponse(request, BAD_REQUEST, builder.startObject().field("error", e.getMessage()).endObject()));
            } catch (IOException e1) {
                logger.error("Failed to send failure response", e1);
            }
            return;
        }
        client.search(searchRequest, new ActionListener<SearchResponse>() {
            @Override
            public void onResponse(SearchResponse response) {
                try {
                    XContentBuilder builder = restContentBuilder(request);
                    builder.startObject();
                    response.toXContent(builder, request);
                    builder.endObject();
                    channel.sendResponse(new XContentRestResponse(request, response.status(), builder));
                } catch (Exception e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("failed to execute search (building response)", e);
                    }
                    onFailure(e);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                try {
                    channel.sendResponse(new XContentThrowableRestResponse(request, e));
                } catch (IOException e1) {
                    logger.error("Failed to send failure response", e1);
                }
            }
        });
    }
    
    public void sendReponse(RestRequest request, SearchResponse response, RestChannel channel)
    {
    	try {
            XContentBuilder builder = restContentBuilder(request);
            builder.startObject();
            response.toXContent(builder, request);
            builder.endObject();
            channel.sendResponse(new XContentRestResponse(request, response.status(), builder));
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("failed to execute search (building response)", e);
            }
            sendFailure(request, channel, e);
        }
    }
    
    public void sendFailure(RestRequest request, RestChannel channel, Throwable e)
    {
    	try {
            channel.sendResponse(new XContentThrowableRestResponse(request, e));
        } catch (IOException e1) {
            logger.error("Failed to send failure response", e1);
        }
    }
    
	/**
	 * @param amazeRest
	 * @return
	 */
	private static String veryConvertDogeQuery(RestRequest amazeRest)
	{
		JSONObject originalJson = new JSONObject(amazeRest.content().toUtf8());
		JSONObject replaceJson = new JSONObject(amazeRest.content().toUtf8());
		muchQueryConversion(new JSONObject(amazeRest.content().toUtf8()), replaceJson);
		return replaceJson.toString();
	}
	
	/**
	 * @param json
	 */
	public static void muchQueryConversion(JSONObject json, JSONObject replaceMap)
	{
		Iterator<String> wowKeys = json.keys();
	    while(wowKeys.hasNext()){
	    	String woof = wowKeys.next();
	    	Object val = json.get(woof);
	    	// much doge found!
	    	int idx = woof.indexOf("_");
	    	if(idx > 0){
		        //so sad, un dogeing
		    	if(targets.contains(woof.substring(0, idx)))
		    	{
		    		String wowwowelastickey = woof.substring(idx + 1);
		    		replaceMap.put(wowwowelastickey, replaceMap.remove(woof));
		    		woof = wowwowelastickey;
		    	}
	    	} 
	    	
	    	if(val.getClass()==JSONArray.class){
	    		//much old school looping
	    		for(int i=0;  i < ((JSONArray)val).length(); i++)
	    		{
	    			//so recurse!
	    			if( ((JSONArray)val).get(i).getClass() == JSONObject.class )
	    				muchQueryConversion(((JSONArray)val).getJSONObject(i), (replaceMap.getJSONArray(woof)).getJSONObject(i));
	    		}
	    	} else {
			    if(val.getClass()==JSONObject.class){
			    	//much recurse
			    	muchQueryConversion((JSONObject) val, replaceMap.getJSONObject(woof));
			    } 
	    	}
		}
	}
    
    public static SearchRequest muchParsingVeryRequest(RestRequest request) {
        String[] indices = Strings.splitStringByCommaToArray(request.param("index"));
        SearchRequest searchRequest = new SearchRequest(indices);
        
        if (request.hasContent()) {
        	searchRequest.source( QueryDogeRestAction.veryConvertDogeQuery(request) );
        } else {
            String source = request.param("source");
            if (source != null) {
                searchRequest.source(source);
            }
        }
        // add extra source based on the request parameters
        searchRequest.extraSource(parseSearchSource(request));
        searchRequest.searchType(request.param("search_type"));

        String scroll = request.param("scroll");
        if (scroll != null) {
            searchRequest.scroll(new Scroll(parseTimeValue(scroll, null)));
        }

        searchRequest.types(Strings.splitStringByCommaToArray(request.param("type")));
        searchRequest.routing(request.param("routing"));
        searchRequest.preference(request.param("preference"));
        searchRequest.indicesOptions(IndicesOptions.fromRequest(request, searchRequest.indicesOptions()));

        return searchRequest;
    }

    public static SearchSourceBuilder parseSearchSource(RestRequest request) {
        SearchSourceBuilder searchSourceBuilder = null;
        String queryString = request.param("q");
        if (queryString != null) {
            QueryStringQueryBuilder queryBuilder = QueryBuilders.queryString(queryString);
            queryBuilder.defaultField(request.param("df"));
            queryBuilder.analyzer(request.param("analyzer"));
            queryBuilder.analyzeWildcard(request.paramAsBoolean("analyze_wildcard", false));
            queryBuilder.lowercaseExpandedTerms(request.paramAsBoolean("lowercase_expanded_terms", true));
            queryBuilder.lenient(request.paramAsBooleanOptional("lenient", null));
            String defaultOperator = request.param("default_operator");
            if (defaultOperator != null) {
                if ("OR".equals(defaultOperator)) {
                    queryBuilder.defaultOperator(QueryStringQueryBuilder.Operator.OR);
                } else if ("AND".equals(defaultOperator)) {
                    queryBuilder.defaultOperator(QueryStringQueryBuilder.Operator.AND);
                } else {
                    throw new ElasticsearchIllegalArgumentException("Unsupported defaultOperator [" + defaultOperator + "], can either be [OR] or [AND]");
                }
            }
            if (searchSourceBuilder == null) {
                searchSourceBuilder = new SearchSourceBuilder();
            }
            searchSourceBuilder.query(queryBuilder);
        }

        int from = request.paramAsInt("from", -1);
        if (from != -1) {
            if (searchSourceBuilder == null) {
                searchSourceBuilder = new SearchSourceBuilder();
            }
            searchSourceBuilder.from(from);
        }
        int size = request.paramAsInt("size", -1);
        if (size != -1) {
            if (searchSourceBuilder == null) {
                searchSourceBuilder = new SearchSourceBuilder();
            }
            searchSourceBuilder.size(size);
        }

        if (request.hasParam("explain")) {
            if (searchSourceBuilder == null) {
                searchSourceBuilder = new SearchSourceBuilder();
            }
            searchSourceBuilder.explain(request.paramAsBooleanOptional("explain", null));
        }
        if (request.hasParam("version")) {
            if (searchSourceBuilder == null) {
                searchSourceBuilder = new SearchSourceBuilder();
            }
            searchSourceBuilder.version(request.paramAsBooleanOptional("version", null));
        }
        if (request.hasParam("timeout")) {
            if (searchSourceBuilder == null) {
                searchSourceBuilder = new SearchSourceBuilder();
            }
            searchSourceBuilder.timeout(request.paramAsTime("timeout", null));
        }

        String sField = request.param("fields");
        if (sField != null) {
            if (searchSourceBuilder == null) {
                searchSourceBuilder = new SearchSourceBuilder();
            }
            if (!Strings.hasText(sField)) {
                searchSourceBuilder.noFields();
            } else {
                String[] sFields = Strings.splitStringByCommaToArray(sField);
                if (sFields != null) {
                    for (String field : sFields) {
                        searchSourceBuilder.field(field);
                    }
                }
            }
        }
        FetchSourceContext fetchSourceContext = FetchSourceContext.parseFromRestRequest(request);
        if (fetchSourceContext != null) {
            if (searchSourceBuilder == null) {
                searchSourceBuilder = new SearchSourceBuilder();
            }
            searchSourceBuilder.fetchSource(fetchSourceContext);
        }

        if (request.hasParam("track_scores")) {
            if (searchSourceBuilder == null) {
                searchSourceBuilder = new SearchSourceBuilder();
            }
            searchSourceBuilder.trackScores(request.paramAsBoolean("track_scores", false));
        }

        String sSorts = request.param("sort");
        if (sSorts != null) {
            if (searchSourceBuilder == null) {
                searchSourceBuilder = new SearchSourceBuilder();
            }
            String[] sorts = Strings.splitStringByCommaToArray(sSorts);
            for (String sort : sorts) {
                int delimiter = sort.lastIndexOf(":");
                if (delimiter != -1) {
                    String sortField = sort.substring(0, delimiter);
                    String reverse = sort.substring(delimiter + 1);
                    if ("asc".equals(reverse)) {
                        searchSourceBuilder.sort(sortField, SortOrder.ASC);
                    } else if ("desc".equals(reverse)) {
                        searchSourceBuilder.sort(sortField, SortOrder.DESC);
                    }
                } else {
                    searchSourceBuilder.sort(sort);
                }
            }
        }

        String sIndicesBoost = request.param("indices_boost");
        if (sIndicesBoost != null) {
            if (searchSourceBuilder == null) {
                searchSourceBuilder = new SearchSourceBuilder();
            }
            String[] indicesBoost = Strings.splitStringByCommaToArray(sIndicesBoost);
            for (String indexBoost : indicesBoost) {
                int divisor = indexBoost.indexOf(',');
                if (divisor == -1) {
                    throw new ElasticsearchIllegalArgumentException("Illegal index boost [" + indexBoost + "], no ','");
                }
                String indexName = indexBoost.substring(0, divisor);
                String sBoost = indexBoost.substring(divisor + 1);
                try {
                    searchSourceBuilder.indexBoost(indexName, Float.parseFloat(sBoost));
                } catch (NumberFormatException e) {
                    throw new ElasticsearchIllegalArgumentException("Illegal index boost [" + indexBoost + "], boost not a float number");
                }
            }
        }

        String sStats = request.param("stats");
        if (sStats != null) {
            if (searchSourceBuilder == null) {
                searchSourceBuilder = new SearchSourceBuilder();
            }
            searchSourceBuilder.stats(Strings.splitStringByCommaToArray(sStats));
        }

        String suggestField = request.param("suggest_field");
        if (suggestField != null) {
            String suggestText = request.param("suggest_text", queryString);
            int suggestSize = request.paramAsInt("suggest_size", 5);
            if (searchSourceBuilder == null) {
                searchSourceBuilder = new SearchSourceBuilder();
            }
            String suggestMode = request.param("suggest_mode");
            searchSourceBuilder.suggest().addSuggestion(
                    termSuggestion(suggestField).field(suggestField).text(suggestText).size(suggestSize)
                            .suggestMode(suggestMode)
            );
        }

        return searchSourceBuilder;
    }
        
}
