package colpensiones;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;


public class Generator {
	private LoadGenerator generator;
	public static final int NUMBER_OF_TASKS = 100;
	public static final int GAP_BETWEEN_TASKS = 1000;
	public Generator(){
		Task work = createTask();
		generator = new LoadGenerator("Client - Server Load Test", NUMBER_OF_TASKS, work, GAP_BETWEEN_TASKS);
		generator.generate();
	}
	
	public Task createTask(){
		return new ClientServerTask();
	}
	
	public static void main (String[] args){
		for (int i = 0; i < 10; i++) {
			@SuppressWarnings("unused")
			Generator generator = new Generator();
		}
	}
}
