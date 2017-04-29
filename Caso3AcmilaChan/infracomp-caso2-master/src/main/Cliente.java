package main;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
//import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Base64Encoder;


public class Cliente 
{
	final static String SYM = "AES";
	final static String ASYM = "RSA";
	final static String HMAC = "HMACMD5";

	final static String END_PEM = "-----END CERTIFICATE-----";
	final static String BEGIN_PEM = "-----BEGIN CERTIFICATE-----";
	
	private static long tAuth;
	private static long tResp;
	private static boolean resp;
	
	public Cliente(){
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
		Security.addProvider(new BouncyCastleProvider());
		KeyPair keyPair = genKeyPair();
		X509CertificateHolder certH = genCertificate(keyPair);
//		System.out.println("Client Public Key: "+hashToString(keyPair.getPublic().getEncoded()));
//		System.out.println("Client Certificate: "+hashToString(certH.getEncoded()));
		
		Socket s=null;
		InputStream is=null;
		OutputStream os=null;
		try{
		s = new Socket("192.168.0.21", 4443);
		is = s.getInputStream();
		os = s.getOutputStream();
		}
		catch(Exception e)
		{
			System.out.println("Connection refused.");
			resp = false;
			throw new Exception ("Connection refused");
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		PrintWriter pw = new PrintWriter(os, true);
		
		pw.println("HOLA");
		String resp = br.readLine();
//		System.out.println(resp);
		if(!resp.equals("OK"))
		{
//			System.out.println("Ha ocurrido un error, cerrando conexión al servidor");
			s.close();
			System.exit(-1);
		}
		
		String alg = "ALGORITMOS:"+String.format("%s:%s:%s", SYM, ASYM, HMAC);
		pw.println(alg);
		resp = br.readLine();
//		System.out.println(resp);
		if(!resp.equals("OK"))
		{
//			System.out.println("Ha ocurrido un error, cerrando conexión al servidor");
			s.close();
			System.exit(-1);
		}
		
		//Empieza conteo de tiempo de auth
		long start = System.currentTimeMillis();
		Base64Encoder encoder = new Base64Encoder();
		pw.println(BEGIN_PEM);
		byte[] localCert = certH.getEncoded(); 
		encoder.encode(localCert, 0, localCert.length, os);
		pw.println("\n"+END_PEM);

		PEMParser pemP = new PEMParser(br);
		X509CertificateHolder sCert = (X509CertificateHolder) pemP.readObject();
		
		X509Certificate serverCert509 = new JcaX509CertificateConverter().setProvider("BC").getCertificate(sCert);
		PublicKey serverPK = serverCert509.getPublicKey();

//		System.out.println("Server Public Key: "+hashToString(sCert.getSubjectPublicKeyInfo().parsePublicKey().getEncoded()));
//		pw.println(hashToString(sCert.getSubjectPublicKeyInfo().parsePublicKey().getEncoded()));
		pw.println("OK");
		pw.println("OK");
		String encSK = br.readLine();
//		System.out.println("Symmetric Key (Encoded): "+encSK);
		
		Cipher servCipher = Cipher.getInstance("RSA");
		servCipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
		byte[] symKeyBytes = servCipher.doFinal(DatatypeConverter.parseHexBinary(encSK));
//		System.out.println("Symmetric Key (Decoded): "+hashToString(symKeyBytes));
		
		SecretKeySpec symKey = new SecretKeySpec(symKeyBytes, SYM);
		
		
		servCipher.init(Cipher.ENCRYPT_MODE, serverPK);
		byte[] encryptedSK = servCipher.doFinal(symKeyBytes);
		pw.println(hashToString(encryptedSK));
		resp = br.readLine();
//		System.out.println(resp);
		if(!resp.equals("OK"))
		{
//			System.out.println("Ha ocurrido un error verificando la Llave Simétrica, cerrando conexión al servidor");
			s.close();
			System.exit(-1);
		}
		//Acaba t de auth
		tAuth = System.currentTimeMillis()-start;
		
		
		String request = "1123123";
		Cipher privateCipher = Cipher.getInstance(SYM);
		privateCipher.init(Cipher.ENCRYPT_MODE, symKey);
		byte[] requestEncoded = privateCipher.doFinal(request.getBytes());
		byte[] hmacDigest = calculateDigest(request.getBytes(), symKey);
		privateCipher.init(Cipher.ENCRYPT_MODE, symKey);
		hmacDigest = privateCipher.doFinal(hmacDigest);
		String rpc = String.format("%s:%s", hashToString(requestEncoded), hashToString(hmacDigest));
//		System.out.println("Request: "+rpc);
		
		//Comienza tiempo de respuesta, se env�a mensaje a servidor
		start= System.currentTimeMillis();
		pw.println(rpc);
		
		resp = br.readLine();
//		System.out.println("Server Response (Encrypted): "+resp);
		privateCipher.init(Cipher.DECRYPT_MODE, symKey);
		byte[] responseDecoded = privateCipher.doFinal(DatatypeConverter.parseHexBinary(resp));
		resp = new String(responseDecoded);
		//Se consigue el mensaje
		tResp=System.currentTimeMillis()-start;
//		System.out.println("Server Response (Decrypted): "+resp);
		
//		System.out.println("Tiempo de auth: "+tAuth);
//		System.out.println("Tiempo de resp: "+tResp);
		
		
	}
	
	
	public static byte[] calculateDigest(byte[] data, Key key) throws NoSuchAlgorithmException, InvalidKeyException
	{
		Mac mac = Mac.getInstance(HMAC);
		mac.init(key);
		byte[] result = mac.doFinal(data);
		return result;
		
	}
	
	public static KeyPair genKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException
	{
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
		keyPairGenerator.initialize(1024, new SecureRandom());
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		return keyPair;
	}
	
	public static X509CertificateHolder genCertificate(KeyPair keyPair) throws NoSuchAlgorithmException, NoSuchProviderException, CertificateEncodingException, InvalidKeyException, IllegalStateException, SignatureException, OperatorCreationException, IOException
	{
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		Date validityBeginDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
		Date validityEndDate = new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000);
//		System.out.println("Start Date: "+df.format(validityBeginDate));
//		System.out.println("End Date: "+df.format(validityEndDate));
		
		ContentSigner sigGen = new JcaContentSignerBuilder("SHA1withRSA").setProvider("BC").build(keyPair.getPrivate()); 
		
//		byte[] encoded = keyPair.getPublic().getEncoded();
//		AlgorithmIdentifier algorithmIdentifier = new AlgorithmIdentifier(PKCSObjectIdentifiers.x509Certificate);
//		SubjectPublicKeyInfo subjectPublicKeyInfo = new SubjectPublicKeyInfo(algorithmIdentifier, encoded);
		
		//X509v3CertificateBuilder certBuild = new X509v3CertificateBuilder(new X500Name("CN=LlamaSign Inc"), 
			//	BigInteger.valueOf(System.currentTimeMillis()), validityBeginDate, validityEndDate, 
				//new X500Name("CN=NovaSoft"), subjectPublicKeyInfo);
		
		X509v3CertificateBuilder certBuild = new JcaX509v3CertificateBuilder(
			      new X500Name("CN=LlamaSign InC"), 
			      BigInteger.valueOf(System.currentTimeMillis()), 
			      validityBeginDate, validityEndDate, 
			      new X500Name("CN=NovaSoft"), keyPair.getPublic());
		
		return certBuild.build(sigGen);
		
	}
	
	private static String hashToString(byte[] hash) {
        StringBuilder buff = new StringBuilder();

        for (byte b : hash) {
            buff.append(String.format("%02x", b & 0xFF));
        }
        
        return buff.toString();
    }

}
