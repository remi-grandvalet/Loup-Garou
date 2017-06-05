package sma.salvator_controller;



import jade.core.Agent;
import sma.generic.interfaces.IController;
import sma.generic_vote.SynchronousVoteBehaviour;
import sma.model.DFServices;
import sma.model.Roles;

/**
 * Controleur gestion du tour hunter
 * @author Kyrion
 *
 */
public class SalvatorControllerAgent extends Agent implements IController {
	private int gameid;
	
	public SalvatorControllerAgent() {
		super();	
	}
	

	@Override
	protected void setup() {
		
		Object[] args = this.getArguments();
		this.gameid = (Integer) args[0];
		
		DFServices.registerGameControllerAgent(Roles.SALVATOR, this, this.gameid);		
		this.addBehaviour(new SynchronousVoteBehaviour(this));
	
		
	}


	public int getGameid() {
		return gameid;
	}
	

}