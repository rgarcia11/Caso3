package caso2_jd.ahumada10_r.garcia11SinSeguridad;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.util.encoders.Base64;


public class ClienteInseguro {

	private static final int PUERTO = 8081;
	private static final String DIR = "localhost";
	private Socket s;
	private PrintWriter out;
	private BufferedReader in;
	private ManejadorCertificado mc;
	private KeyPair keyAsin;
	private CifradorAsimetricoRSA cifradorAsim;

	public ClienteInseguro(){
		conectar();
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
			conversar();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
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
		out.println(reto1);
		
		String reto1rta = in.readLine();
		bytes = Hex.decodeHex(reto1rta.toCharArray());
		BigInteger respuestica = new BigInteger(bytes);
		if(bg.intValue() != respuestica.intValue()){
			out.println("ERROR");
			throw new Exception ("El servidor respondio mal el reto");
		}
		

		out.println("OK");
		rta = in.readLine();

		out.println(rta);
		rta = in.readLine();
		String cc = "1020802369";
		out.println(cc+":"+cc);

		rta = in.readLine();

		out.println("OK");

		System.out.println("Se termino!");
	}

	public static void main(String[] args) {
		ClienteInseguro ci = new ClienteInseguro();
	}

}
