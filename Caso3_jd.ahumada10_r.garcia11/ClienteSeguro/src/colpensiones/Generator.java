package colpensiones;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;


public class Generator {
	private LoadGenerator generator;
	public static final int NUMBER_OF_TASKS = 80; //400/200/80
	public static final int GAP_BETWEEN_TASKS = 100;//20/40/100
	public static final int NUM_THREADS = 1;//8/2/1
	public static final int NUM_ITERACIONES = 10;
	public Generator(){
		Task work = createTask();
		generator = new LoadGenerator("Client - Server Load Test", NUMBER_OF_TASKS, work, GAP_BETWEEN_TASKS);
		generator.generate();
	}
	
	public Task createTask(){
		return new ClientServerTask();
	}
	
	public static void main (String[] args){
		for (int i = 0; i < NUM_ITERACIONES; i++) {
			System.out.println("============================================== ITERACION "+(i+1)+"===========================================================");
			@SuppressWarnings("unused")
			Generator generator = new Generator();
		}
		System.out.println("=================================== HEMOS FINALIZADO ========================================");
	}
}
