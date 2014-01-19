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
package org.elasticsearch.plugin.querydoge;

import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.querydoge.rest.action.QueryDogeRestAction;
import org.elasticsearch.rest.RestModule;

public class QueryDogePlugin extends AbstractPlugin {

	private final Settings settings;

	public String name() {
		return "query-doge";
	}

	public String description() {
		return "I am a doge cache plugin, so amaze";
	}

	public QueryDogePlugin(Settings settings) {		
		this.settings = settings;
	}

	@Override
	public void processModule(Module module) {
		if (module instanceof RestModule) {
			((RestModule) module).addRestAction(QueryDogeRestAction.class);
		}
	}


}