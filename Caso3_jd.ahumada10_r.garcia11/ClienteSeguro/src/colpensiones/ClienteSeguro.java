package colpensiones;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.util.encoders.Base64;


public class ClienteSeguro {

	private static final int PUERTO = 8081;
//	private static final String DIR = "192.168.0.11";
//	private static final String DIR = "186.31.46.188";
	private static final String DIR = "localhost";
	private static final String algoritmoSimetrico = "AES";
	private static final String algoritmoAsimetrico = "RSA";
	private static final String algoritmoDigest = "HMACMD5";

	private Socket s;
	private PrintWriter out;
	private BufferedReader in;

	private PublicKey llaveServidor;
	private KeyPair llaveCliente;
	private SecretKey llaveSesion;

	//-------------------------
	private ManejadorCertificado mc;
	private KeyPair keyAsin;
	private CifradorAsimetricoRSA cifradorAsim;
	private CifradorSimetricoAES cifradorSim;
	private CalculadorDigestHmacMD5 cmd;

	//-------------------------
	private Long inicioAutenticacionServidor;
	private Long tiempoAutenticacionServidor;
	private Long inicioAutenticacionCliente;
	private Long tiempoAutenticacionCliente;
	private Long inicioRespuesta;
	private Long tiempoRespuesta;

	public ClienteSeguro(){
		try{
			//-------------
			inicioAutenticacionCliente = (long) 0;
			inicioAutenticacionServidor = (long) 0;
			inicioRespuesta= (long) 0;
			tiempoAutenticacionCliente = (long) 0;
			tiempoAutenticacionServidor = (long) 0;
			tiempoRespuesta = (long) 0;

			//-------------
			cifradorAsim = new CifradorAsimetricoRSA();
			keyAsin = cifradorAsim.darLlave();
			//-------------
			conectar();
			conversar();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	public void conectar(){
		try{
			s = new Socket(DIR, PUERTO);
			out = new PrintWriter(s.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		}catch(Exception e){
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
		//Etapa 1 Inicio de sesion

		inicioSesion();

		//Etapa 2 Intercambio de CD

		llaveServidor = ManejadorCertificado.creationYProcesamiento(keyAsin,s.getOutputStream(),out, in);

		//Etapa 3 Autenticacion
		BigInteger bg = enviarReto();

		recibirRespuestaReto(bg);

		manejarRetoServidor();

		//Recibo la llave de sesion
		recibirLlaveSesion();


		//Etapa 4 Consulta

		enviarConsulta();

		recibirRespuestaConsulta();

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

	private void inicioSesion() throws Exception {
		out.println("HOLA");
		String rta = in.readLine();
		if(!rta.equals("OK")){
			throw new Exception("Respuesta inesperada. Etapa 1.");
		}
		out.println("ALGORITMOS:"+algoritmoSimetrico+":"+algoritmoAsimetrico+":"+algoritmoDigest);
		rta = in.readLine();
		if(rta.equals("ERROR"))
		{
			throw new Exception("Algoritmos no soportados.");
		}
		else if(!rta.equals("OK")){
			throw new Exception("Respuesta inesperada");
		}
	}

	private void recibirRespuestaConsulta() throws Exception{
		String rta = in.readLine();
		//Tiempo2 consulta servidor!
		tiempoRespuesta = System.nanoTime() - inicioRespuesta;
		String[] rtaDividida = rta.split(":");
		String respuestaCifradaS = rtaDividida[0];
		byte[] respuestaCifrada = toByteArray(respuestaCifradaS);
		String respuesta = CifradorSimetricoAES.descifrar(respuestaCifrada, llaveSesion);

		String respuestaHashCifradaS = rtaDividida[1];
		byte[] respuestaHashCifrada = toByteArray(respuestaHashCifradaS);
		String respuestaHash = CifradorSimetricoAES.descifrar(respuestaHashCifrada, llaveSesion);
		byte[] respuestaHashBytes = respuestaHash.getBytes();
		byte[] respuestaBytes = cmd.calcular(respuesta, llaveSesion);
		boolean comparacion = true;
		comparacion = cmd.sonIguales(respuesta, respuestaHashBytes, llaveSesion);
		if (comparacion){
			out.println("OK");
		} else {
			out.println("OK");
		}
	}

	private void enviarConsulta() throws Exception{
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
		String cons = "1018499615";
		byte[] consultaCifrada = CifradorSimetricoAES.cifrar(cons, llaveSesion);
		byte[] consultaHash = cmd.calcular(cons, llaveSesion);
		byte[] consultahHashCifrada = CifradorSimetricoAES.cifrarB(consultaHash, llaveSesion);
		String consultaCompleta = toHexString(consultaCifrada)+":"+toHexString(consultahHashCifrada);
		System.out.println(consultaCompleta);
		//Tiempo1 consulta!
		inicioRespuesta = System.nanoTime();
		out.println(consultaCompleta);
	}

	private void recibirLlaveSesion() throws Exception{
		String rta = in.readLine();
		byte[] bytesCifrados = toByteArray(rta);
		byte[] bytes = cifradorAsim.descifrar(bytesCifrados);
		llaveSesion = new SecretKeySpec(bytes, "AES");
		cmd = new CalculadorDigestHmacMD5();
	}

	private void manejarRetoServidor() throws Exception {
		//Tiempo1 autenticacion cliente
		inicioAutenticacionCliente = System.nanoTime();
		String retoServidor = in.readLine();
		byte[] bytesCifrados = toByteArray(retoServidor);
		byte[] bytes = cifradorAsim.descifrar(bytesCifrados);
		bytesCifrados = CifradorAsimetricoRSA.cifrar(llaveServidor, bytes);
		String rta = toHexString(bytesCifrados);
		//Tiempo2 autenticacion cliente
		tiempoAutenticacionCliente = System.nanoTime() - inicioAutenticacionCliente;
		out.println(rta);
	}

	private BigInteger recibirRespuestaReto(BigInteger bg) throws Exception {
		String reto1rta = in.readLine();
		byte[] bytesCifrados = toByteArray(reto1rta);
		byte[] bytes = CifradorAsimetricoRSA.descifrar(cifradorAsim.getPrivate(), bytesCifrados);

		BigInteger respuestica = new BigInteger(bytes);
		if(bg.intValue() != respuestica.intValue()){
			out.println("ERROR");
			throw new Exception ("El servidor respondio mal el reto");
		}
		//Tiempo2 autenticacion servidor
		tiempoAutenticacionServidor=System.nanoTime()-inicioAutenticacionServidor;
		out.println("OK");
		return respuestica;
	}

	private BigInteger enviarReto() throws Exception {
		Random generador = new Random(System.currentTimeMillis());
		int aleatorio = generador.nextInt();
		BigInteger bg = new BigInteger(aleatorio+"");

		byte [] bytes = bg.toByteArray();
		byte [] bytesCifrados = CifradorAsimetricoRSA.cifrar(llaveServidor, bytes);
		String reto1 = toHexString(bytesCifrados);

		//Tiempo1 autenticacion servidor
		inicioAutenticacionServidor = System.nanoTime();
		out.println(reto1);
		return bg;
	}


	//------ Metodos Auxiliares

	/**
	 * Recibe un array de bytes y lo convierte en hexadecimal (es para enviar datos)
	 * @param array de bytes
	 * @return hexa
	 */
	public static String toHexString(byte[] array) {
		return DatatypeConverter.printHexBinary(array);
	}

	/**
	 * Recibe un string en hexa y lo convierte a arreglo de bytes (es para recibir datos)
	 * @param s hexa
	 * @return bytes
	 */
	public static byte[] toByteArray(String s) {
		return DatatypeConverter.parseHexBinary(s);
	}


	//------ Main
	public static void main(String[] args) {
		ClienteSeguro ci = new ClienteSeguro();
	}
}
