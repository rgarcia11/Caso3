package colpensiones;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.util.encoders.Base64;

import colpensiones.Generator;


public class ClienteInseguro {

	private static final int PUERTO = 8081;
//	private static final String DIR = "localhost";
	private static final String DIR = "192.168.0.13";
	private Socket s;
	private PrintWriter out;
	private BufferedReader in;
	private ManejadorCertificado mc;
	private KeyPair keyAsin;
	private CifradorAsimetricoRSA cifradorAsim;
	//-------------------------
	private Long inicioAutenticacionServidor;
	private Long tiempoAutenticacionServidor;
	private Long inicioAutenticacionCliente;
	private Long tiempoAutenticacionCliente;
	private Long inicioRespuesta;
	private Long tiempoRespuesta;
	public ClienteInseguro(){
		try{
			//-------------
			inicioAutenticacionCliente = (long) 0;
			inicioAutenticacionServidor = (long) 0;
			inicioRespuesta= (long) 0;
			tiempoAutenticacionCliente = (long) 0;
			tiempoAutenticacionServidor = (long) 0;
			tiempoRespuesta = (long) 0;

			conectar();
			conversar();
		} catch(Exception e){

		}
	}

	public void conectar(){
		try 
		{
			s = new Socket(DIR, PUERTO);
			out = new PrintWriter(s.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			mc = new ManejadorCertificado();
			cifradorAsim = new CifradorAsimetricoRSA();
			keyAsin = cifradorAsim.darLlave();
		} 
		catch (Exception e) 
		{
			try{
				File tiempos = new File("./data/tiempos-"+Generator.NUMBER_OF_TASKS+"-"+Generator.GAP_BETWEEN_TASKS+"-"+Generator.NUM_THREADS);
				PrintWriter writer = new PrintWriter(new FileWriter(tiempos,true));
				writer.println("ERROR:(");
				writer.close();
			}catch (Exception x){
				x.printStackTrace();
			}
		} 
	}

	public void conversar() throws Exception{
		out.println("HOLA");
		String rta = in.readLine();
		if(!rta.equals("OK")){
			throw new Exception("Respuesta inesperada.");
		}

		out.println("ALGORITMOS:AES:RSA:HMACMD5");
		rta = in.readLine();
		if(rta.equals("OK"))
		{
		}
		else if(rta.equals("ERROR")){
			throw new Exception("Algoritmos no soportados.");
		}else{
			throw new Exception("Respuesta inesperada");
		}

		PublicKey llavePublicaServer = mc.creationYProcesamiento(keyAsin,s.getOutputStream(),out, in);

		//Es parte del protocolo aunque no salga en el enunciado. 
		out.println("");


		BigInteger bg = new BigInteger(100, new Random());		
		byte [] bytes = bg.toByteArray();
		String reto1 = Hex.encodeHexString(bytes);
		System.out.println("Hexa: "+reto1+" decimal: "+bg.intValue());

		//Tiempo1 autenticacion servidor
		inicioAutenticacionServidor = System.nanoTime();
		out.println(reto1);

		String reto1rta = in.readLine();
		bytes = Hex.decodeHex(reto1rta.toCharArray());
		BigInteger respuestica = new BigInteger(bytes);
		if(bg.intValue() != respuestica.intValue()){
			out.println("ERROR");
			throw new Exception ("El servidor respondio mal el reto");
		}

		//Tiempo2 autenticacion servidor
		tiempoAutenticacionServidor=System.nanoTime()-inicioAutenticacionServidor;

		out.println("OK");

		//Tiempo1 autenticacion cliente
		inicioAutenticacionCliente = System.nanoTime();

		rta = in.readLine();

		out.println(rta);
		//Tiempo2 autenticacion cliente
		tiempoAutenticacionCliente = System.nanoTime() - inicioAutenticacionCliente;

		rta = in.readLine();
		String cc = "1020802369";

		//Tiempo1 consulta!
		inicioRespuesta = System.nanoTime();
		out.println(cc+":"+cc);

		rta = in.readLine();
		//Tiempo2 consulta servidor!
		tiempoRespuesta = System.nanoTime() - inicioRespuesta;
		
		out.println("OK");

		imprimirResultados();
		
		System.out.println("Se termino!");
	}

	private void imprimirResultados() {
		try{
			File tiempos = new File("./data/tiempos-"+Generator.NUMBER_OF_TASKS+"-"+Generator.GAP_BETWEEN_TASKS+"-"+Generator.NUM_THREADS);
			PrintWriter writer = new PrintWriter(new FileWriter(tiempos,true));
			writer.println("tiempoAutenticacionServidor:"+ TimeUnit.MILLISECONDS.convert(tiempoAutenticacionServidor, TimeUnit.NANOSECONDS));
			writer.println("tiempoAutenticacionCliente:"+ TimeUnit.MILLISECONDS.convert(tiempoAutenticacionCliente, TimeUnit.NANOSECONDS));
			writer.println("tiempoRespuestaConsulta:"+ TimeUnit.MILLISECONDS.convert(tiempoRespuesta, TimeUnit.NANOSECONDS));
			writer.close();
		}catch (Exception e){
			e.printStackTrace();
		}		
	}
	
	public static void main(String[] args) {
		ClienteInseguro ci = new ClienteInseguro();
	}

}
