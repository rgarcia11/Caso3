package generator;

import java.time.LocalDateTime;
import java.util.Scanner;

import uniandes.gload.core.LoadGenerator;
import uniandes.gload.core.Task;

/**
 * GLoad Core Class - Task
 * @author Victor Guana at University of Los Andes (vm.guana26@uniandes.edu.co)
 * Systems and Computing Engineering Department - Engineering Faculty
 * Licensed with Academic Free License version 2.1
 * 
 * ------------------------------------------------------------
 * Example Class Client Server:
 * This is Generator Controller - Setup and Launch the Load 
 * Over the Server Using a Task
 * ------------------------------------------------------------
 * 
 */
public class Generator
{
	/**
	 * Load Generator Service (From GLoad 1.0)
	 */
	private LoadGenerator generator;
	private long promTAuth;
	private long promTResp;
	private int totalNoResp;
	
	/**
	 * Constructs a new Generator
	 */
	public Generator (int nOT, int gBT)
	{
		Task work = createTask();
		int numberOfTasks = nOT;
		int gapBetweenTasks = gBT;
		generator = new LoadGenerator("Client - Server Load Test", numberOfTasks, work, gapBetweenTasks);
		generator.generate();
		boolean end=false;
		while(!end){
			int total=0;
			for (Thread t: Thread.getAllStackTraces().keySet()){
				if(t.getState()==Thread.State.RUNNABLE){
					total++;
				}
			}
			//System.out.println(total);
			if(total==3){
				end=true;
			}
		}
		System.out.println("Acabe");
		promTAuth=(Valores.getPromTauth()/numberOfTasks);
		promTResp=(Valores.getPromTresp()/numberOfTasks);
		totalNoResp=(Valores.getTotalNoResp());
		
		Valores.init();
	}
	
	/**
	 * Helper that Constructs a Task
	 */
	private Task createTask()
	{
		return new ClientServerTask();
	}
	

	public static void main (String ... args)
	{
		
		int[] nOT = {400,200,80};
		int[] gBT = {20,40,80};
		long[] tAuth = new long[3];
		long[] tResp = new long[3];
		double[] noResp = new double[3];
		StringBuilder sb = new StringBuilder();
		
		for(int i=0;i<nOT.length;i++){
//			StringBuilder sb = new StringBuilder();
			sb.append("Hora inicio: "+LocalDateTime.now()+"\n");
			
			for(int j=0;j<10;j++){
				Generator gen = new Generator(nOT[i], gBT[i]);
				tAuth[i]+=gen.promTAuth;
				tResp[i]+=gen.promTResp;
				noResp[i]+=gen.totalNoResp;
				
				
			}
			tAuth[i]/=10;
			tResp[i]/=10;
			noResp[i]/=10;
			
			//Scanner in = new Scanner(System.in);
			//in.nextLine();
			sb.append("Hora Fin: "+LocalDateTime.now()+"\n");
			sb.append("Numero de transacciones: "+nOT[i]+"\n");
			sb.append("Tiempo entre transacciones: "+gBT[i]+"\n");
			sb.append("\n");
			sb.append("Tiempo promedio de autorización: "+tAuth[i]+"\n");
			sb.append("Tiempo promedio de respuesta: "+tResp[i]+"\n");
			sb.append("Promedio de solicitudes no respondidas: "+noResp[i]+"\n");
			sb.append("\n");
			sb.append("\n");
			
//			System.out.println(sb.toString());
			
		}
		System.out.println(sb.toString());
		
		
		
	}

}

