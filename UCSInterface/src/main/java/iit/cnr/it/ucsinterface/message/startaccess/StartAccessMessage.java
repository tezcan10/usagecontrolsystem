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
package iit.cnr.it.ucsinterface.message.startaccess;

import iit.cnr.it.ucsinterface.message.Message;
import iit.cnr.it.ucsinterface.message.PART;
import iit.cnr.it.ucsinterface.message.PURPOSE;

/**
 * Structure of the startaccess message.
 * <p>
 * This is the structure of the startaccess message.
 * </p>
 * 
 * @author antonio
 *
 */
public final class StartAccessMessage extends Message {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;
	// id of the session that has to be started
	private String						sessionId;
	// initialized flag
	private volatile boolean	isInitialized			= false;
	
	/**
	 * Constructor of the StartAccessMessage
	 * 
	 * @param source
	 *          the source of the message
	 * @param destination
	 *          the destination of the message
	 */
	public StartAccessMessage(String source, String destination) {
		super(source, destination);
		if (isInitialized()) {
			purpose = PURPOSE.STARTACCESS;
			isInitialized = true;
		}
		
	}
	
	/**
	 * Default constructor of the StartAccessMessage
	 */
	public StartAccessMessage() {
		super(PART.PEP.toString(), PART.CH.toString());
		if (isInitialized()) {
			purpose = PURPOSE.STARTACCESS;
			isInitialized = true;
		}
		
	}
	
	// ---------------------------------------------------------------------------
	// GETTERS and SETTERS
	// ---------------------------------------------------------------------------
	public boolean setSessionId(String sessionId) {
		// BEGIN parameter checking
		if (!isInitialized || sessionId == null || sessionId.isEmpty()) {
			isInitialized = false;
			return false;
		}
		// END parameter checking
		this.sessionId = sessionId;
		return true;
	}
	
	public String getSessionId() {
		// BEGIN parameter checking
		if (!isInitialized) {
			return null;
		}
		// END parameter checking
		return sessionId;
	}
	
	@Override
	public int compareTo(Message o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}