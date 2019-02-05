/*******************************************************************************
 * Copyright 2018 IIT-CNR
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package iit.cnr.it.peprest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.scheduling.annotation.Async;

import iit.cnr.it.peprest.configuration.Configuration;
import iit.cnr.it.peprest.configuration.PEPConf;
import iit.cnr.it.peprest.proxy.ProxyRequestManager;
import iit.cnr.it.ucsinterface.message.MEAN;
import iit.cnr.it.ucsinterface.message.Message;
import iit.cnr.it.ucsinterface.message.endaccess.EndAccessMessage;
import iit.cnr.it.ucsinterface.message.endaccess.EndAccessResponse;
import iit.cnr.it.ucsinterface.message.reevaluation.ReevaluationResponse;
import iit.cnr.it.ucsinterface.message.startaccess.StartAccessMessage;
import iit.cnr.it.ucsinterface.message.startaccess.StartAccessResponse;
import iit.cnr.it.ucsinterface.message.tryaccess.TryAccessMessage;
import iit.cnr.it.ucsinterface.message.tryaccess.TryAccessMessageBuilder;
import iit.cnr.it.ucsinterface.message.tryaccess.TryAccessResponse;
import iit.cnr.it.ucsinterface.pep.PEPInterface;
import iit.cnr.it.ucsinterface.requestmanager.RequestManagerToExternalInterface;
import iit.cnr.it.utility.Utility;
import it.cnr.iit.xacmlutilities.policy.utility.JAXBUtility;

/**
 * This is the PEP using rest
 * 
 * @author antonio
 *
 */
public class PEPRest implements PEPInterface, Runnable {
	
	private Configuration												configuration;
	
	private String															ip;
	private String															port;
	private String															revokeType;
	// required to give the PEP an endpoint in case of on going notifications
	private String															onGoingEvaluationInterface;
	
	// path to the policy
	private String															POLICY_PATH;
	
	private String															REQUEST_PATH;
	
	protected static final Logger								LOGGER			= Logger
	    .getLogger(PEPRest.class.getName());
	
	private RequestManagerToExternalInterface		requestManager;
	// private ContextHandlerInterface contextHandler;
	
	// map of unanswered messages, the key is the id of the message
	private HashMap<String, Message>						unanswered	= new HashMap<>();
	private ConcurrentHashMap<String, Message>	responses		= new ConcurrentHashMap<>();
	
	private volatile boolean										initialized	= false;
	
	private Object															mutex				= new Object();
	
	public PEPRest() {
		if ((configuration = retrieveConfiguration()) == null) {
			return;
		}
		PEPConf pepConf = configuration.getPepConf();
		this.ip = pepConf.getIp();
		this.port = pepConf.getPort();
		this.revokeType = pepConf.getRevoke();
		this.onGoingEvaluationInterface = pepConf.getStatusChanged();
		this.requestManager = new ProxyRequestManager(configuration.getRMConf());
		this.POLICY_PATH = pepConf.getPolicyPath();
		this.REQUEST_PATH = pepConf.getRequestPath();
		initialized = true;
	}
	
	private Configuration retrieveConfiguration() {
		try {
			String xml = "";
			InputStream stream = PEPRest.class.getClassLoader()
			    .getResourceAsStream("conf.xml");
			BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
			String line = "";
			
			while ((line = buffer.readLine()) != null) {
				xml += line;
			}
			buffer.close();
			stream.close();
			Configuration configuration = JAXBUtility
			    .unmarshalToObject(Configuration.class, xml);
			return configuration;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String tryAccess() {
		String request;
		String policy;
		int random = 0;
		if (random % 2 == 0) {
			policy = Utility.readFileAbsPath(POLICY_PATH);
			request = Utility.readFileAbsPath(REQUEST_PATH);
		} else {
			policy = Utility.readFileAbsPath(POLICY_PATH);
			request = Utility.readFileAbsPath(REQUEST_PATH);
		}
		
		TryAccessMessageBuilder tryAccessBuilder = new TryAccessMessageBuilder(
		    configuration.getPepConf().getId(), configuration.getPepConf().getIp());
		tryAccessBuilder.setPepUri(buildOnGoingEvaluationInterface())
		    .setPolicy(policy).setRequest(request);
		TryAccessMessage tryAccessMessage = tryAccessBuilder.build();
		tryAccessMessage.setCallback(buildResponseInterface("tryAccessResponse"),
		    MEAN.REST);
		unanswered.put(tryAccessMessage.getID(), tryAccessMessage);
		// Gson gson = new Gson();
		// String s = gson.toJson(tryAccessMessage);
		// System.out.println(s);
		System.out.println("[TIME] TRYACCESS " + System.currentTimeMillis());
		Message message = requestManager.sendMessageToCH(tryAccessMessage);
		return tryAccessMessage.getID();
		
		// return (TryAccessResponse) contextHandler.tryAccess(tryAccessMessage);
	}
	
	public String startAccess(String sessionId) {
		StartAccessMessage startAccessMessage = new StartAccessMessage(
		    configuration.getPepConf().getId(), configuration.getPepConf().getIp());
		startAccessMessage.setSessionId(sessionId);
		startAccessMessage
		    .setCallback(buildResponseInterface("startAccessResponse"), MEAN.REST);
		unanswered.put(startAccessMessage.getID(), startAccessMessage);
		try {
			System.out.println("[TIME] STARTACCESS " + System.currentTimeMillis());
			Message message = requestManager.sendMessageToCH(startAccessMessage);
			
			// return (StartAccessResponse) contextHandler
			// .startAccess(startAccessMessage);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return startAccessMessage.getID();
	}
	
	private String endAccess(String sessionId) {
		EndAccessMessage endAccessMessage = new EndAccessMessage(
		    configuration.getPepConf().getId(), configuration.getPepConf().getIp());
		endAccessMessage.setSessionId(sessionId);
		endAccessMessage.setCallback(buildResponseInterface("endAccessResponse"),
		    MEAN.REST);
		unanswered.put(endAccessMessage.getID(), endAccessMessage);
		try {
			System.out.println("[TIME] ENDACCESS " + System.currentTimeMillis());
			Message message = requestManager.sendMessageToCH(endAccessMessage);
			message = waitForResponse(endAccessMessage.getID());
			System.out.println("[TIME] ENDACCESS END" + System.currentTimeMillis());
			System.exit(0);
			// return (StartAccessResponse) contextHandler
			// .startAccess(startAccessMessage);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return endAccessMessage.getID();
	}
	
	/*
	 * public void setCHInterface(ContextHandlerInterface contextHandlerInterface)
	 * { this.contextHandler = contextHandlerInterface; }
	 */
	
	public void setRequestManagerInterface(
	    RequestManagerToExternalInterface requestManager) {
		this.requestManager = requestManager;
	}
	
	@Override
	@Async
	public Message onGoingEvaluation(Message message) {
		// BEGIN parameter checking
		if (message == null || !(message instanceof ReevaluationResponse)) {
			System.err.println("Message not valid");
			return null;
		}
		if (!initialized) {
			System.err.println("Cannot answer the message");
			return null;
		}
		// END parameter checking
		
		System.out.println("[TIME] ON_GOING_EVAL " + System.currentTimeMillis());
		
		ReevaluationResponse chPepMessage = (ReevaluationResponse) message;
		if (revokeType.equals("HARD")) {
			System.out
			    .println("[TIME] sending endacces " + System.currentTimeMillis());
			EndAccessMessage endAccess = new EndAccessMessage(
			    configuration.getPepConf().getId(),
			    configuration.getPepConf().getIp());
			endAccess.setCallback(null, MEAN.REST);
			endAccess.setSessionId(chPepMessage.getPDPEvaluation().getSessionId());
			requestManager.sendMessageToCH(endAccess);
			
			try {
				message = waitForResponse(endAccess.getID());
				System.out.println("[TIME] endacces END" + System.currentTimeMillis());
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {
			if (chPepMessage.getPDPEvaluation().getResponse().contains("Permit")) {
				LOGGER.log(Level.INFO, "RESUME EXECUTION");
			}
			if (chPepMessage.getPDPEvaluation().getResponse().contains("Deny")) {
				LOGGER.log(Level.INFO, "STOP EXECUTION");
			}
		}
		// contextHandler.endAccess(endAccess);
		message.setMotivation("OK");
		
		return message;
	}
	
	@Override
	@Async
	public void receiveResponse(Message message) {
		responses.put(message.getID(), message);
		synchronized (mutex) {
			mutex.notifyAll();
		}
	}
	
	@Override
	public void run() {
		try {
			String id = tryAccess();
			TryAccessResponse tryAccessResponse = (TryAccessResponse) waitForResponse(
			    id);
			System.out.println("[TIME] TRYACCESS END" + System.currentTimeMillis());
			if (tryAccessResponse.getPDPEvaluation().getResponse()
			    .contains("Permit")) {
				id = startAccess(tryAccessResponse.getSessionId());
				StartAccessResponse startAccessResponse = (StartAccessResponse) waitForResponse(
				    id);
				System.out
				    .println("[TIME] STARTACCESS END" + System.currentTimeMillis());
				if (startAccessResponse.getPDPEvaluation().getResponse()
				    .contains("Permit")) {
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Message waitForResponse(String id)
	    throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		TaskBody taskBody = new TaskBody(id);
		return executor.submit(taskBody).get();
	}
	
	private class TaskBody implements Callable<Message> {
		
		private String id;
		
		public TaskBody(String id) {
			this.id = id;
		}
		
		@Override
		public Message call() {
			try {
				while (!responses.containsKey(id)) {
					System.out.println("First wait");
					synchronized (mutex) {
						mutex.wait();
					}
				}
				System.out.println("WAKE UP!");
				return responses.remove(id);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
	}
	
	public ConcurrentHashMap<String, Message> getResponses() {
		return responses;
	}
	
	private final String buildResponseInterface(String name) {
		StringBuilder response = new StringBuilder();
		response.append("http://" + ip + ":");
		response.append(port + "/");
		response.append(name);
		return response.toString();
	}
	
	private String buildOnGoingEvaluationInterface() {
		StringBuilder response = new StringBuilder();
		response.append("http://" + ip + ":");
		response.append(port + "/");
		response.append(onGoingEvaluationInterface);
		return response.toString();
	}
	
	public void end(String sessionId)
	    throws InterruptedException, ExecutionException {
		LOGGER.log(Level.INFO,
		    "[TIME] Sending endAccess " + System.currentTimeMillis());
		String id = endAccess(sessionId);
		EndAccessResponse endAccessResponse = (EndAccessResponse) waitForResponse(
		    id);
		LOGGER.log(Level.INFO,
		    "[TIME] END ACCESS RESPONSE: " + System.currentTimeMillis());
	}
	
}