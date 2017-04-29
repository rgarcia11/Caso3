package main;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;


public class ClienteSinSeguridad 
{
	final static String SYM = "AES";
	final static String ASYM = "RSA";
	final static String HMAC = "HMACMD5";
	
	private static long tAuth;
	private static long tResp;
	private static boolean resp;
	
	public ClienteSinSeguridad(){
		resp=true;
	}
	
	public long gettAuth() {
		return tAuth;
	}



	public void settAuth(long tAuth) {
		this.tAuth = tAuth;
	}



	public long gettResp() {
		return tResp;
	}



	public void settResp(long tResp) {
		this.tResp = tResp;
	}



	public boolean isResp() {
		return resp;
	}



	public void setResp(boolean resp) {
		this.resp = resp;
	}
	
	public static void main(String... args) throws Exception
	{	
		Socket s = new Socket("192.168.0.21", 4444);
		InputStream is = s.getInputStream();
		OutputStream os = s.getOutputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		PrintWriter pw = new PrintWriter(os, true);
		
		pw.println("HOLA");
		String resp = br.readLine();
		System.out.println(resp);
		if(!resp.equals("OK"))
		{
			System.out.println("Ha ocurrido un error, cerrando conexión al servidor");
			s.close();
			System.exit(-1);
		}
		
		String alg = "ALGORITMOS:"+String.format("%s:%s:%s", SYM, ASYM, HMAC);
		pw.println(alg);
		resp = br.readLine();
		System.out.println(resp);
		if(!resp.equals("OK"))
		{
			System.out.println("Ha ocurrido un error, cerrando conexión al servidor");
			s.close();
			System.exit(-1);
		}
		//Empieza conteo de tiempo de auth
		long start = System.currentTimeMillis();
		pw.println("CERTFICADOCLIENTE");
		
		resp = br.readLine();
		
		System.out.println("Server: "+resp);
		
		//Acaba t de auth
		tAuth = System.currentTimeMillis()-start;
		
		pw.println("OK");
//		pw.println("OK");
		
		resp = br.readLine();
		System.out.println("Server: "+resp);
		
		pw.println("CIFRADOKS+");
		resp = br.readLine();
		
		if(!resp.equals("OK"))
		{
			System.out.println("Ha ocurrido un error, cerrando conexión al servidor");
			s.close();
			System.exit(-1);
		}
		//Comienza tiempo de respuesta, se envía mensaje a servidor
		start= System.currentTimeMillis();
		pw.println("CIFRADOLS1");
		resp = br.readLine();
		System.out.println("Server: "+resp);
		//Se consigue el mensaje
		tResp=System.currentTimeMillis()-start;
		
		
		
		
	}
	
	private static String hashToString(byte[] hash) {
        StringBuilder buff = new StringBuilder();

        for (byte b : hash) {
            buff.append(String.format("%02x", b & 0xFF));
        }
        
        return buff.toString();
    }
	

}