import java.net.*; 
import java.io.*;
import java.util.Scanner;
import java.util.Base64;

public class ClientApp {
	Socket mySocket = null;
	String CID;//CID를 입력받을 변수
	int NUM; //요구사항을 번호로 표현해 입력받을 변수
	
	int CNT = 0;//명령의 수를 카운터하는 변수

	public static void main(String[] args) {
		ClientApp client = new ClientApp();
		Scanner sc = new Scanner(System.in);
		
		System.out.print("Enter_your_CID : ");
		client.CID = sc.nextLine();
		
		try {
			client.mySocket = new Socket("localhost", 55555);
			System.out.println("연결완료");
			
			OutputStream out = client.mySocket.getOutputStream();
			DataOutputStream dout = new DataOutputStream(out);
			
			MessageListener ml = new MessageListener(client.mySocket);
			ml.start();
			
			while(true) {
				client.CNT++;
				System.out.println("Select_Number");
				System.out.println("1. HI 2. CURRENT_TIME 3. CONNECTION_TIME 4. CLIENT_LIST 5.QUIT");
				System.out.print("-->  ");
				client.NUM = sc.nextInt();
				MakeMessage msg = new MakeMessage();
				msg.MakeMessage(client.CID, client.NUM, client.CNT);
				dout.writeUTF(msg.emsg);
				Thread.sleep(10);
				
			}
		}catch(Exception e) {
			System.out.println("문제발생");
			
			
		}

	}

}

class MakeMessage{//입력받은 CID와 명령수 번호를 request 문자로 바꿔주는 클래스
	String RQ;
	String emsg;
	void MakeMessage(String cid, int num, int cnt) {
		switch(num) {
		case 1 : 
			RQ = "RQ///HI///CID:"+cid+"///Num_Req:"+cnt+"///END";
			break;
		case 2 : 
			RQ = "RQ///CURRENT_TIME///CID:"+cid+"///Num_Req:"+cnt+"///END";
			break;
		case 3 : 
			RQ = "RQ///CONNECTION_TIME///CID:"+cid+"///Num_Req:"+cnt+"///END";
			break;
		case 4 : 
			RQ = "RQ///CLIENT_LIST///CID:"+cid+"///Num_Req:"+cnt+"///END";
			break;
		case 5 : 
			RQ = "RQ///QUIT///CID:"+cid+"///Num_Req:"+cnt+"///END";
			break;
		default : //메소드의 재귀호출을 통해 없는 번호가 나올시 다시 입력
			System.out.println("없는 번호입니다.");
			System.out.print("Select_Num : ");
			System.out.print("1. HI 2. CURRENT_TIME 3. CONNECTION_TIME 4. CLIENT_LIST 5.QUIT\n-->");
			Scanner sc = new Scanner(System.in);
			int a = sc.nextInt();
			MakeMessage(cid,a,cnt);
		}
		Encoding_Decoding ec = new Encoding_Decoding();
		emsg = ec.EncodingMSG(RQ);
		//System.out.println(emsg);
	}
}

class Encoding_Decoding{
	String EncodingMSG(String msg){
		byte[] targetByte = msg.getBytes();
		msg = Base64.getEncoder().encodeToString(targetByte);
		//System.out.println(msg);
		return msg;
	}
	String DecodingMSG(String msg) {
		byte[] targetByte = Base64.getDecoder().decode(msg);
		msg = new String(targetByte);
		return msg;
	}
}

class CutMessage{//서버에서 받아온 response 메시지의 데이터값을 분석하는 클래스
	String data;//스플릿함수를 이용해 잘라낸 데이터를 저장하는 변수
	CutMessage(String msg){
		String[] msg1 = msg.split("///");//스플릿 변수를 이용해 /// 단위로 문자를 자름
		data = msg1[2];//그 중 두번째 값인 데이터를 저장
	}
}

class MessageListener extends Thread{
	Encoding_Decoding ED = new Encoding_Decoding();
	Socket socket;
	MessageListener(Socket _s){
		this.socket = _s;
	}
	public void run() {
		try {
			InputStream in = this.socket.getInputStream();
			DataInputStream din = new DataInputStream(in);
			while(true) {
				String msg = din.readUTF();
				System.out.println(msg);
				msg = ED.DecodingMSG(msg);
				CutMessage cm = new CutMessage(msg);				
				System.out.println(cm.data);
			}
			
		}catch(Exception e) {
			System.out.println("접속종료");
			System.exit(0);
		}
	}
}
