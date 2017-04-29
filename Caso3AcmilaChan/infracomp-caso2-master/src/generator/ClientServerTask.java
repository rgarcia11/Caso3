package generator;

import main.Cliente;
import main.ClienteSinSeguridad;
import uniandes.gload.core.Task;

/**
 * GLoad Core Class - Task
 * @author Victor Guana at University of Los Andes (vm.guana26@uniandes.edu.co)
 * Systems and Computing Engineering Department - Engineering Faculty
 * Licensed with Academic Free License version 2.1
 * 
 * ------------------------------------------------------------
 * Example Class Client Server:
 * This Class Represents the task that we want to generate in a concurrent way
 * ------------------------------------------------------------
 * 
 */
public class ClientServerTask extends Task
{
	Cliente client;
//	ClienteSinSeguridad client;

	@Override
	public void execute() 
	{
		// TODO Auto-generated method stub
		client = new Cliente();
//		client = new ClienteSinSeguridad();
		try {
			client.main(new String[1]);
			Valores.addTAuth(client.gettAuth());
			Valores.addTResp(client.gettResp());
		} catch (Exception e) {
			Valores.incNoResp();
		}
		
		
	}

	@Override
	public void fail() 
	{
		// TODO Auto-generated method stub
		System.out.println(Task.MENSAJE_FAIL);
		
	}

	@Override
	public void success() 
	{
		// TODO Auto-generated method stub
		System.out.println(Task.OK_MESSAGE);
		
		
		
	}

}
