package sma.generic_init;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import sma.environment_agent.ReceiveBehaviour;
import sma.generic_vote.AbstractVoteBehaviour;
import sma.model.DFServices;
import sma.model.Roles;
import sma.model.ScoreResults;
import sma.model.VoteRequest;
import sma.model.VoteResults;
import sma.player_agent.PlayerAgent;

public class NewMainRoleBehaviour extends CyclicBehaviour{

	//TODO REGISTER TO DFSERVICES 
	private String step;
	private String nextStep;

	private final static String STATE_WAIT_ROLE ="STATE_WAIT_ROLE";
	private final static String STATE_ANSWER_INIT_ROLE ="STATE_ANSWER_INIT_ROLE";

	private PlayerAgent agent;
	private AID sender; // sender of the request

	public NewMainRoleBehaviour(PlayerAgent agent){
		super();
		this.agent = agent;
		this.sender = null;
		this.nextStep = "";
		this.step = STATE_WAIT_ROLE;
	

	}

	@Override
	public void action() {

		if(step.equals(STATE_WAIT_ROLE))
		{
			//Message get from InitBehaviour game_control_Agent
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
					MessageTemplate.MatchConversationId("ATTRIBUTION_ROLE"));

			ACLMessage message = this.myAgent.receive(mt);
			if(message != null)
			{
				//System.out.println("WAIT NEW MAIN ROLE BEHAVIOUR : "+message.getContent());
				String new_role = message.getContent();
				this.sender = message.getSender();
				
				ACLMessage reply = null;
				//Test if already got a role
				String main_role = this.agent.getMain_role();

				if (main_role.isEmpty()){
					//System.out.println("SET NEW ROLE : "+new_role+ " TO THIS PLAYER "+this.agent.getName());				

					//TODO Look if better place to be				
					this.agent.addBehaviour(new GenericInitBehaviour(this.agent));
					this.agent.setMain_role(new_role);
					//TODO Check if in first position
					//DFServices.modifyPlayerAgent(old_role, new_role, this.agent, this.agent.getGameid());
					//DFServices.registerPlayerAgent(this.agent.getMain_role(), this.agent, this.agent.getGameid());
					reply = new ACLMessage(ACLMessage.CONFIRM);

					ACLMessage messageRequest = new ACLMessage(ACLMessage.REQUEST);
					messageRequest.addReceiver(this.agent.getAID());
					messageRequest.setSender(this.agent.getAID());
					messageRequest.setConversationId("INIT_ROLE");

					messageRequest.setContent(this.agent.getMain_role());
					this.myAgent.send(messageRequest);
					this.nextStep = STATE_ANSWER_INIT_ROLE;

				}
				else if (main_role == new_role){
					System.out.println("Already got this role "+new_role+ "TO THIS PLAYER "+this.agent.getName());

					reply.setConversationId("NEW_ROLE");
					reply.setSender(this.myAgent.getAID());
					reply.addReceiver(message.getSender());
					reply = new ACLMessage(ACLMessage.FAILURE);
					this.nextStep = "";

				}
				else if (main_role != new_role){
					//TODO If voleur can swap role SO DO this case 
					System.out.println("Got another role "+main_role+ "TO THIS PLAYER "+this.agent.getName());

					System.out.println("ATTRIBUTION => "+new_role);
					ACLMessage messageRequest = new ACLMessage(ACLMessage.REQUEST);
					messageRequest.addReceiver(this.agent.getAID());
					messageRequest.setSender(this.agent.getAID());
					messageRequest.setConversationId("INIT_ROLE");

					messageRequest.setContent(new_role);
					this.myAgent.send(messageRequest);

					this.nextStep = STATE_ANSWER_INIT_ROLE;

					//reply = new ACLMessage(ACLMessage.FAILURE); Treat this one for voleur
				}
			}
			else
			{
				this.nextStep = "";
				block();
			}
		}



		else if(step.equals(STATE_ANSWER_INIT_ROLE))
		{
			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchConversationId("INIT_ROLE"));

			//TODO failure answer
			ACLMessage message = this.myAgent.receive(mt);
			if(message != null)
			{
				System.out.println("INIT ROLE DONE");
				ACLMessage messageRequest = new ACLMessage(ACLMessage.INFORM);
				messageRequest.setSender(this.agent.getAID());

				messageRequest.addReceiver(this.sender);
				messageRequest.setConversationId("ATTRIBUTION_ROLE");
				this.myAgent.send(messageRequest);


				this.nextStep = STATE_WAIT_ROLE; //Return to begin 
			}
			else
			{
				this.nextStep = "";
				block();
			}

		}
		if(!this.nextStep.isEmpty())
		{
			this.step = this.nextStep;
			this.nextStep ="";
		}
	}

}
