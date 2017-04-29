package generator;

public class Valores {
	
	private static long promTauth;
	private static long promTresp;
	private static int totalNoResp;
	
	public static synchronized void addTAuth(long tAuth){
		promTauth+=tAuth;
	}
	
	public static synchronized void addTResp(long tAuth){
		promTresp+=tAuth;
	}
	
	public static synchronized void incNoResp(){
		totalNoResp++;	
	}

	public static synchronized long getPromTauth() {
		return promTauth;
	}

	public static synchronized long getPromTresp() {
		return promTresp;
	}

	public static synchronized int getTotalNoResp() {
		return totalNoResp;
	}
	
	public static synchronized void init(){
		promTauth=0;
		promTresp=0;
		totalNoResp=0;
	}

}
