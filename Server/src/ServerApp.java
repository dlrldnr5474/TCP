import java.net.*;  
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerApp {
	ServerSocket ss = null;
	
	static ArrayList<Client> clients = new ArrayList<Client>();//접속된 클라이언트를 저장
	static ArrayList CID = new ArrayList();//클라이언트에서 전달받은 CID를 저장
	
	Date date = new Date();//2번째 요구사항인 현재시간을 나타내줄 Date 클래스
	SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss");//Date 클래스를 **:**:**으로 폼을 바꿔주는 클래스
	
	long start = System.currentTimeMillis();//클라이언트의 유지시간을 확인하기 위해 시작시간을 저장
	
	public static void main(String[] args) {
		ServerApp server = new ServerApp();
		try {
			server.ss = new ServerSocket(55555);
			System.out.println("서버소켓 생성완료....");
			while(true) {
				Socket socket = server.ss.accept();
				Client c = new Client(socket);
				server.clients.add(c);
				c.start();
			}
		}catch(SocketException e) {
			System.out.println("소켓예외발생");
		}catch(IOException e) {
			System.out.println("입출력예외발생");
		}
	}
		
}

class StringCut{//클라이언트에서 받아온 데이터를 잘라주는 클래스
	String cid;//CID를 임시 저장하는 변수
	String rnum;//몇번째 명령어인지 저장
	String cname;//명령어 이름을 저장하는 변수
	
	StringCut(String msg){
		String[] cut = msg.split("///");//스플릿함수를 통해 문자열을 자름
		String[] cut1 = cut[2].split(":");//CID를 자름
		String[] cut2 = cut[3].split(":");//명령어 카운트를 자름
		
		cname = cut[1];
		cid = cut1[1];
		rnum = cut2[1];
	}
}

class StringAdd{//4번째 요구사항에서 저장된 클라이언트 IP주소와 CID를 한 문자열로 묶어주는 클래스
	ServerApp ss = new ServerApp();
	String msg = "";
	
	StringAdd(){
		for(int i=0; i<ss.CID.size(); i++) {
			msg = msg+ss.clients.get(i)+"-"+ss.CID.get(i)+"\n";
		}
		
	}
}

class Encoding_Decoding{
	String EncdingMSG(String msg) {
		byte[] targetByte = msg.getBytes();
		msg = Base64.getEncoder().encodeToString(targetByte);
		return msg;
	}
	String DecodingMSG(String msg) {
		byte[] targetByte = Base64.getDecoder().decode(msg);
		msg = new String(targetByte);
		return msg;
	}
}

class Client extends Thread{
	ServerApp s = new ServerApp();
	Encoding_Decoding ED = new Encoding_Decoding();
	Socket socket;
	
	Client(Socket _s){
		this.socket = _s;
	}
	
	public void run() {
		try {
			OutputStream out =socket.getOutputStream();
			DataOutputStream dout = new DataOutputStream(out);
			InputStream in = socket.getInputStream();
			DataInputStream din = new DataInputStream(in);
			
			while(true) {
				String msg = din.readUTF();
				System.out.println(msg);
				msg = ED.DecodingMSG(msg);
				//System.out.println(msg);
				StringCut sc = new StringCut(msg);
				switch(sc.cname) {
				case "HI" ://첫번째 요구사항에 대한 response 메시지
					s.CID.add(sc.cid);
					dout.writeUTF(ED.EncdingMSG("RP///100///Success_Save_CID///END"));
					break;
				case "CURRENT_TIME" ://두번째 요구사항에 대한 response 메시지 
					dout.writeUTF(ED.EncdingMSG("RP///130///"+s.time.format(s.date)+"///END"));
					break;
				case "CONNECTION_TIME" ://세번째 요구사항에 대한 response 메시지
					long end = System.currentTimeMillis();
					dout.writeUTF(ED.EncdingMSG("RP///150///"+((end-s.start)/1000)+"초///END"));
					break;
				case "CLIENT_LIST" ://네번째 요구사항에 대한 response 메시지
					StringAdd sa = new StringAdd();
					dout.writeUTF(ED.EncdingMSG("RP///200///"+sa.msg+"///END"));
					break;
				case "QUIT" ://다섯번째 요구사항에 대한 response 메시지
					dout.writeUTF(ED.EncdingMSG("RP///250///Close_Server///END"));
					socket.close();
					break;
				default : //오류에 대한 response 메시지
					dout.writeUTF("RP///300///fail///END");
					break;
				
				}
			}
			
		}catch(Exception e) {
			e.getMessage();
		}
	}
}