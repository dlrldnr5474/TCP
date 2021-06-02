import java.net.*; 
import java.io.*;
import java.util.Scanner;
import java.util.Base64;

public class ClientApp {
	Socket mySocket = null;
	String CID;//CID�� �Է¹��� ����
	int NUM; //�䱸������ ��ȣ�� ǥ���� �Է¹��� ����
	
	int CNT = 0;//����� ���� ī�����ϴ� ����

	public static void main(String[] args) {
		ClientApp client = new ClientApp();
		Scanner sc = new Scanner(System.in);
		
		System.out.print("Enter_your_CID : ");
		client.CID = sc.nextLine();
		
		try {
			client.mySocket = new Socket("localhost", 55555);
			System.out.println("����Ϸ�");
			
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
			System.out.println("�����߻�");
			
			
		}

	}

}

class MakeMessage{//�Է¹��� CID�� ��ɼ� ��ȣ�� request ���ڷ� �ٲ��ִ� Ŭ����
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
		default : //�޼ҵ��� ���ȣ���� ���� ���� ��ȣ�� ���ý� �ٽ� �Է�
			System.out.println("���� ��ȣ�Դϴ�.");
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

class CutMessage{//�������� �޾ƿ� response �޽����� �����Ͱ��� �м��ϴ� Ŭ����
	String data;//���ø��Լ��� �̿��� �߶� �����͸� �����ϴ� ����
	CutMessage(String msg){
		String[] msg1 = msg.split("///");//���ø� ������ �̿��� /// ������ ���ڸ� �ڸ�
		data = msg1[2];//�� �� �ι�° ���� �����͸� ����
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
			System.out.println("��������");
			System.exit(0);
		}
	}
}
