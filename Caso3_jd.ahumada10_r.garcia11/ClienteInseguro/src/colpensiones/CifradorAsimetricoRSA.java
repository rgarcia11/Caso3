package colpensiones;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import javax.crypto.Cipher;

public class CifradorAsimetricoRSA {
	private final static String ALGORITMO="RSA";
	private KeyPair keyPair;

	public CifradorAsimetricoRSA(){
		KeyPairGenerator generator;
		try {
			generator = KeyPairGenerator.getInstance(ALGORITMO);
			generator.initialize(1024);
			keyPair = generator.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public byte[] cifrar() {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITMO);
			BufferedReader stdIn =	new BufferedReader(new InputStreamReader(System.in));
			String pwd = stdIn.readLine();
			byte [] clearText = pwd.getBytes();
			String s1 = new String (clearText);
			System.out.println("clave original: " + s1);
			cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
			long startTime = System.nanoTime();
			byte [] cipheredText = cipher.doFinal(clearText);
			long endTime = System.nanoTime();
			System.out.println("clave cifrada: " + cipheredText);
			System.out.println("Tiempo asimetrico: " + (endTime - startTime));
			return cipheredText;
		}
		catch (Exception e) {
			System.out.println("Excepcion: " + e.getMessage());
			return null;
		}
	}

	public void descifrar(byte[] cipheredText) {
		try {
			Cipher cipher = Cipher.getInstance(ALGORITMO);
			cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
			byte [] clearText = cipher.doFinal(cipheredText);
			String s3 = new String(clearText);
			System.out.println("clave original: " + s3);
		}
		catch (Exception e) {
			System.out.println("Excepcion: " + e.getMessage());
		}
	}

	public KeyPair darLlave() {
		return keyPair;
	}
	
	public PrivateKey getPrivate(){
		return keyPair.getPrivate();
	}

}
