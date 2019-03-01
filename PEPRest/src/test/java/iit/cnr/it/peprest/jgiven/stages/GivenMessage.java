package iit.cnr.it.peprest.jgiven.stages;

import java.util.Arrays;
import java.util.UUID;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.annotation.BeforeScenario;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;

import iit.cnr.it.peprest.PEPRestServiceScenarioTest.PEPRestOperation;
import iit.cnr.it.ucsinterface.message.Message;
import iit.cnr.it.ucsinterface.message.PDPResponse;
import iit.cnr.it.ucsinterface.message.endaccess.EndAccessResponse;
import iit.cnr.it.ucsinterface.message.reevaluation.ReevaluationResponse;
import iit.cnr.it.ucsinterface.message.startaccess.StartAccessResponse;
import iit.cnr.it.ucsinterface.message.tryaccess.TryAccessResponse;
import iit.cnr.it.ucsinterface.message.tryaccess.TryAccessResponseContent;
import oasis.names.tc.xacml.core.schema.wd_17.DecisionType;
import oasis.names.tc.xacml.core.schema.wd_17.ResponseType;
import oasis.names.tc.xacml.core.schema.wd_17.ResultType;

public class GivenMessage extends Stage<GivenMessage> {

    @ProvidedScenarioState
    String sessionId;

    @ProvidedScenarioState
    Message message;

    @BeforeScenario
    public void init() {
    	sessionId = UUID.randomUUID().toString();
    }

    public GivenMessage a_ReevaluationResponse_request_with_decision_$(DecisionType decisionType){
    	message = buildReevaluationResponse(decisionType);
    	return self();
    }

    public GivenMessage a_TryAccessResponse_request_with_decision_$(DecisionType decisionType){
    	message = buildTryAccessResponse(decisionType);
    	return self();
    }

	protected StartAccessResponse buildStartAccessResponsePermit() {
		return buildStartAccessResponse(DecisionType.PERMIT);
	}

	protected ReevaluationResponse buildReevaluationResponse(DecisionType decisionType) {
		PDPResponse pdpEvaluation = buildPDPResponse(decisionType);
		ReevaluationResponse reevaluationResponse = new ReevaluationResponse();
		reevaluationResponse.setPDPEvaluation(pdpEvaluation);
		return reevaluationResponse;
	}

	protected PDPResponse buildPDPResponse(DecisionType decision) {
		ResultType resultType = new ResultType();
		resultType.setDecision(decision);
		ResponseType responseType = new ResponseType();
		responseType.setResult(Arrays.asList(resultType));
		PDPResponse pdpResponse = new PDPResponse();
		pdpResponse.setResponseType(responseType);
		pdpResponse.setInitialized(true);
		return pdpResponse;
	}

	public GivenMessage create_permit_response_for_$(PEPRestOperation operation) {
		switch(operation) {
		case TRY_ACCESS_RESPONSE: 
			message = buildTryAccessResponse(DecisionType.PERMIT);
			break;
		case START_ACCESS_RESPONSE: 
			message = buildStartAccessResponse(DecisionType.PERMIT);
			break;
		case END_ACCESS_RESPONSE:
			message = buildEndAccessResponse(DecisionType.PERMIT);
			break;
		default:
			break;
		}
		return self();
	}

	private TryAccessResponse buildTryAccessResponse(DecisionType decisionType) {
		//CODE added because sessionId seems to be null
		if(sessionId == null) {
			init();
		}
		PDPResponse pdpEvaluation = buildPDPResponse(decisionType);
		TryAccessResponseContent content = new TryAccessResponseContent();
		content.setSessionId(sessionId);
		content.setPDPEvaluation(pdpEvaluation);
		TryAccessResponse tryAccessResponse = new TryAccessResponse(sessionId);
		tryAccessResponse.setContent(content);
		return tryAccessResponse;
	}

	protected StartAccessResponse buildStartAccessResponse(DecisionType decisionType) {
		//CODE added because sessionId seems to be null
		if(sessionId == null) {
			init();
		}
		PDPResponse pdpEvaluation = buildPDPResponse(decisionType);
		StartAccessResponse startAccessResponse = new StartAccessResponse(sessionId);
		startAccessResponse.setResponse(pdpEvaluation);
		return startAccessResponse;
	}

	protected EndAccessResponse buildEndAccessResponse(DecisionType decisionType) {
		//CODE added because sessionId seems to be null
		if(sessionId == null) {
			init();
		}
		PDPResponse pdpEvaluation = buildPDPResponse(decisionType);
		EndAccessResponse endAccessResponse = new EndAccessResponse(sessionId);
		endAccessResponse.setResponse(pdpEvaluation);
		return endAccessResponse;
	}

	public String getMessageId() {
		return message.getID();
	}
}