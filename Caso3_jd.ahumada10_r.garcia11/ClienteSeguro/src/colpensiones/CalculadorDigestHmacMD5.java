package colpensiones;

import java.security.Key;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.SecretKey;

public class CalculadorDigestHmacMD5 {
	
	public CalculadorDigestHmacMD5() 
	{
		//:)
	}

	private byte[] getKeyedDigest(byte[] buffer) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(buffer);
			return md5.digest();
		} catch (Exception e) {
			return null;
		}
	}

	public byte[] calcular(String dato, SecretKey key) {
		try {
			Mac mac = Mac.getInstance("HMACMD5");
			mac.init(key);
			
			byte[] text = dato.getBytes();
			String s1 = new String(text);
			byte [] digest = mac.doFinal(text);
			String s2 = new String(digest);
			return digest;
		}
		catch (Exception e) {
			System.out.println("Excepcion: " + e.getMessage());
			return null;
		}
	}
	
    public boolean sonIguales(String msj, byte[] digest, SecretKey llave) {
        boolean son= true;
        byte[] mensaje = calcular(msj, llave);
        if (digest.length == mensaje.length){
            for (int i=0; i<mensaje.length && son;i++){
                if (digest[i]!=mensaje[i]) {
                    son = false;
                }
            }
        }
        else {
            son = false;
        }
        
        return son;
    }


}
