package colpensiones;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;



public class Consolidado {

	public Consolidado (){
		long[] totalAutenticacionServidor = new long[Generator.NUM_ITERACIONES];
		long[] totalAutenticacionCliente = new long[Generator.NUM_ITERACIONES];
		long[] totalTiempoRespuestaConsulta = new long[Generator.NUM_ITERACIONES];
		int[] totalTransaccionesFallidas = new int[Generator.NUM_ITERACIONES];

		int i = 0;
		try{
			//Leo los tiempos
			FileReader fr = new FileReader("./data/tiempos-"+Generator.NUMBER_OF_TASKS+"-"+Generator.GAP_BETWEEN_TASKS+"-"+Generator.NUM_THREADS);
			BufferedReader br = new BufferedReader(fr);
			String pequenhaLinea = br.readLine();
			while (pequenhaLinea!= null){
				String [] lineaLeida = pequenhaLinea.split(":");
				int indicador = i/(3*Generator.NUMBER_OF_TASKS);

				if(lineaLeida[0].equals("ERROR")){
					totalTransaccionesFallidas[indicador] = totalTransaccionesFallidas[indicador]+1;
				}		
				if (lineaLeida[0].equals("tiempoAutenticacionServidor")){
					totalAutenticacionServidor[indicador] =totalAutenticacionServidor[indicador]+Integer.parseInt(lineaLeida[1]); 
				}
				if (lineaLeida[0].equals("tiempoAutenticacionCliente")){
					totalAutenticacionCliente[indicador] =totalAutenticacionCliente[indicador]+Integer.parseInt(lineaLeida[1]); 
				}
				if (lineaLeida[0].equals("tiempoRespuestaConsulta")){
					totalTiempoRespuestaConsulta[indicador] =totalTiempoRespuestaConsulta[indicador]+Integer.parseInt(lineaLeida[1]); 
				}
				i++;
				pequenhaLinea = br.readLine();

			}
			br.close();
			long[] tiempoAutenticacionServidor = new long[Generator.NUM_ITERACIONES];
			long[] tiempoAutenticacionCliente = new long [Generator.NUM_ITERACIONES];
			long[] tiempoRespuestaConsulta = new long [Generator.NUM_ITERACIONES];
			for (int j = 0; j < Generator.NUM_ITERACIONES; j++) {
				tiempoAutenticacionServidor[j] = Generator.NUMBER_OF_TASKS!=totalTransaccionesFallidas[j]? totalAutenticacionServidor[j] / (Generator.NUMBER_OF_TASKS-totalTransaccionesFallidas[j]):-1;
				tiempoAutenticacionCliente[j] = Generator.NUMBER_OF_TASKS!=totalTransaccionesFallidas[j]? totalAutenticacionCliente[j] / (Generator.NUMBER_OF_TASKS-totalTransaccionesFallidas[j]):-1;
				tiempoRespuestaConsulta[j] = Generator.NUMBER_OF_TASKS!=totalTransaccionesFallidas[j]? totalTiempoRespuestaConsulta[j] / (Generator.NUMBER_OF_TASKS-totalTransaccionesFallidas[j]):-1;
			}
			File res2 = new File("./data/consolidado-"+Generator.NUMBER_OF_TASKS+"-"+Generator.GAP_BETWEEN_TASKS+"-"+Generator.NUM_THREADS);
			PrintWriter writer = new PrintWriter(new FileWriter(res2,false));
			writer.println("====Parametros!!=============================");
			writer.println("Numero de transacciones por iteracion: "+Generator.NUMBER_OF_TASKS);
			writer.println("Ramp-up de: "+Generator.GAP_BETWEEN_TASKS);
			writer.println("Numero de threads: "+Generator.NUM_THREADS);
			

			for (int j = 1; j <= Generator.NUM_ITERACIONES; j++) {
				writer.println("====Iteracion "+j+"=============================");
				writer.println(	"tiempo de autenticacion del servidor: " +  tiempoAutenticacionServidor[j-1]);
				writer.println(	"tiempo de autenticacion del cliente: " +  tiempoAutenticacionCliente[j-1]);
				writer.println(	"tiempo de recibir respuesta a la consulta: " +  tiempoRespuestaConsulta[j-1]);
			}
			writer.close();
			writer.close();
		}catch(Exception e){

		}
	}

}
