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
package org.elasticsearch.querydoge.helper;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class HttpHelper {

    private String baseUrl;
    private CloseableHttpClient httpclient = HttpClients.createDefault();
    private ResponseHandler<String> handler = new BasicResponseHandler();
      
    public HttpHelper(String url) {
            baseUrl = url;
    }

	// Create a custom response handler, we are big boys and want all the datas back
    ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

        public String handleResponse(
                final HttpResponse response) throws ClientProtocolException, IOException {
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity) : null;
        }

    };
    
    public String getAndReturnBody(String path) throws ClientProtocolException, IOException
    {
		HttpGet httpget = new HttpGet(baseUrl + path);
		
		System.out.println(httpget);
		
		return handler.handleResponse(httpclient.execute(httpget));
    }
    
    public String postAndReturnBody(String path, String message) throws ClientProtocolException, IOException
    {
    	HttpPost httpPost = new HttpPost(baseUrl + path);
    	StringEntity entity = new StringEntity(message, HTTP.UTF_8);
    	entity.setContentType("application/x-www-form-urlencoded");
    	httpPost.setEntity(entity);
    	
    	System.out.println(httpPost);
    	System.out.println(message);
    	
		return httpclient.execute(httpPost, responseHandler);
    }    
    
    public String putAndReturnBody(String path, String message) throws ClientProtocolException, IOException
    {
    	HttpPut httpPut = new HttpPut(baseUrl + path);
    	StringEntity entity = new StringEntity(message, HTTP.UTF_8);
    	entity.setContentType("application/x-www-form-urlencoded");
    	httpPut.setEntity(entity);
    	
    	System.out.println(httpPut);
    	System.out.println(message);
    	
		return httpclient.execute(httpPut, responseHandler);
    }    
    
}
