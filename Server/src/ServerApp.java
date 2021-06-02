import java.net.*;  
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerApp {
	ServerSocket ss = null;
	
	static ArrayList<Client> clients = new ArrayList<Client>();//���ӵ� Ŭ���̾�Ʈ�� ����
	static ArrayList CID = new ArrayList();//Ŭ���̾�Ʈ���� ���޹��� CID�� ����
	
	Date date = new Date();//2��° �䱸������ ����ð��� ��Ÿ���� Date Ŭ����
	SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss");//Date Ŭ������ **:**:**���� ���� �ٲ��ִ� Ŭ����
	
	long start = System.currentTimeMillis();//Ŭ���̾�Ʈ�� �����ð��� Ȯ���ϱ� ���� ���۽ð��� ����
	
	public static void main(String[] args) {
		ServerApp server = new ServerApp();
		try {
			server.ss = new ServerSocket(55555);
			System.out.println("�������� �����Ϸ�....");
			while(true) {
				Socket socket = server.ss.accept();
				Client c = new Client(socket);
				server.clients.add(c);
				c.start();
			}
		}catch(SocketException e) {
			System.out.println("���Ͽ��ܹ߻�");
		}catch(IOException e) {
			System.out.println("����¿��ܹ߻�");
		}
	}
		
}

class StringCut{//Ŭ���̾�Ʈ���� �޾ƿ� �����͸� �߶��ִ� Ŭ����
	String cid;//CID�� �ӽ� �����ϴ� ����
	String rnum;//���° ��ɾ����� ����
	String cname;//��ɾ� �̸��� �����ϴ� ����
	
	StringCut(String msg){
		String[] cut = msg.split("///");//���ø��Լ��� ���� ���ڿ��� �ڸ�
		String[] cut1 = cut[2].split(":");//CID�� �ڸ�
		String[] cut2 = cut[3].split(":");//��ɾ� ī��Ʈ�� �ڸ�
		
		cname = cut[1];
		cid = cut1[1];
		rnum = cut2[1];
	}
}

class StringAdd{//4��° �䱸���׿��� ����� Ŭ���̾�Ʈ IP�ּҿ� CID�� �� ���ڿ��� �����ִ� Ŭ����
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
				case "HI" ://ù��° �䱸���׿� ���� response �޽���
					s.CID.add(sc.cid);
					dout.writeUTF(ED.EncdingMSG("RP///100///Success_Save_CID///END"));
					break;
				case "CURRENT_TIME" ://�ι�° �䱸���׿� ���� response �޽��� 
					dout.writeUTF(ED.EncdingMSG("RP///130///"+s.time.format(s.date)+"///END"));
					break;
				case "CONNECTION_TIME" ://����° �䱸���׿� ���� response �޽���
					long end = System.currentTimeMillis();
					dout.writeUTF(ED.EncdingMSG("RP///150///"+((end-s.start)/1000)+"��///END"));
					break;
				case "CLIENT_LIST" ://�׹�° �䱸���׿� ���� response �޽���
					StringAdd sa = new StringAdd();
					dout.writeUTF(ED.EncdingMSG("RP///200///"+sa.msg+"///END"));
					break;
				case "QUIT" ://�ټ���° �䱸���׿� ���� response �޽���
					dout.writeUTF(ED.EncdingMSG("RP///250///Close_Server///END"));
					socket.close();
					break;
				default : //������ ���� response �޽���
					dout.writeUTF("RP///300///fail///END");
					break;
				
				}
			}
			
		}catch(Exception e) {
			e.getMessage();
		}
	}
}