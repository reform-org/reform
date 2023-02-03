import { globalAgent, Agent, request } from "http";
import {
	globalAgent as _globalAgent,
	Agent as _Agent,
	request as _request,
} from "https";

//set the time (in seconds) for connection to be alive
var keepAliveTimeout = 30 * 1000;

if (globalAgent && globalAgent.hasOwnProperty("keepAlive")) {
	globalAgent.keepAlive = true;
	_globalAgent.keepAlive = true;
	globalAgent.keepAliveMsecs = keepAliveTimeout;
	_globalAgent.keepAliveMsecs = keepAliveTimeout;
} else {
	var agent = new Agent({
		keepAlive: true,
		keepAliveMsecs: keepAliveTimeout,
	});

	var secureAgent = new _Agent({
		keepAlive: true,
		keepAliveMsecs: keepAliveTimeout,
	});

	var httpRequest = request;
	var httpsRequest = _request;

	request = function (options, callback) {
		if (options.protocol == "https:") {
			options["agent"] = secureAgent;
			return httpsRequest(options, callback);
		} else {
			options["agent"] = agent;
			return httpRequest(options, callback);
		}
	};
}
