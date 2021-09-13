import java.io.*;
import java.net.*;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
public class Client{
    public static void main(String[] args) {

        try {
            System.out.println("connecting server...");

            Socket soc = new Socket("127.0.0.1", 9182);

            //读取对方发送来的公钥
            BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            //发送公钥给对方
            PrintWriter out = new PrintWriter(soc.getOutputStream(), true);

            System.out.println("Create keys...");
            //生成密钥
            KeyPair keys = EncryptRSA.generateKeyPair();

            //获取公钥，发送
            String CPK = Base64.getEncoder().encodeToString(keys.getPublic().getEncoded());
            out.println(CPK);
            System.out.println("send keys...");

            //通过socket获取到的来自服务器端的公钥SPK
            String serverPublicKey = in.readLine();

            //使用X509EncodedKeySpec还原公钥
            PublicKey publicKeyServer = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(serverPublicKey)));
            System.out.println("recive keys...");
            // 新建写线程并启动
            ClientWrite writeforServer = new ClientWrite(soc, publicKeyServer);
            writeforServer.start();

            // 新建读取线程并启动
            ClientRead ClientReadfromServer = new ClientRead(soc, keys);
            ClientReadfromServer.start();

            System.out.println("Client starting...");
        } catch (Exception e) {
            System.out.println("server closed,please restart client while server is running ");
        }
    }
}

//写进程
class ClientWrite extends Thread {
    Socket soc;
    PublicKey publicKeyServer;
    DataOutputStream dos = null;
    public ClientWrite(Socket soc,PublicKey publicKeyServer) {
        this.soc = soc;
        this.publicKeyServer = publicKeyServer;
    }
    // 用于发送信息到服务端
    public void run() {
        try {

            dos = new DataOutputStream(soc.getOutputStream());
            Scanner userIn = new Scanner(System.in);
            while (true) {
                String messageIn = userIn.nextLine();
                if (messageIn.equalsIgnoreCase("/exit")) {
                    break;
                }
                //对信息进行加密
                String messageInEncrypted = Base64.getEncoder()
                        .encodeToString(EncryptRSA.encrypt(messageIn.getBytes("UTF-8"), publicKeyServer));
                dos.writeUTF(messageInEncrypted);
                System.out.println("All messages sent to server are encrypted.");
            }
            System.out.println("Sending shutdown message to server...");
            // 退出
            dos.writeUTF(Base64.getEncoder()
                    .encodeToString(EncryptRSA.encrypt(new String("/exit").getBytes("UTF-8"), publicKeyServer)));
            System.out.println("Exiting client...");
        } catch (Exception e) {
            System.out.println("server closed,please restart client while server is running ");
        }
    }
}



//服务器端读进程
class ClientRead extends Thread {
    Socket soc;
    KeyPair keys;
    DataInputStream dis = null;
    String currentPath;

    //以下为负责客户端输入和输出的流
    public ClientRead(Socket soc,KeyPair keys ) {
        this.soc = soc;
        this.keys = keys;
        this.currentPath = System.getProperty("user.dir");//user.dir指定了当前的路径
    }
    // 用于读取从服务端发送来的消息
    public void run() {
        try {
            dis = new DataInputStream(soc.getInputStream());
            while (true) {

                String recievedMessage =  dis.readUTF();
                if (recievedMessage.equalsIgnoreCase("NO FILE FOUND")){
                    System.out.println("from server:NO FILE FOUND");
                }
                else if (recievedMessage.equalsIgnoreCase("downloading……100%")) {
                   
                    recievedMessage  =  dis.readUTF();
                    String filename  =  dis.readUTF();

                    FileOutputStream fos = null;

                    fos = new FileOutputStream(this.currentPath+"\\"+filename);
                    System.out.println(this.currentPath+filename);

                    fos.write( recievedMessage.getBytes());

                    fos.close();

                    System.out.println("from server:download finished");


                }else {
                    //通过私钥解密
                    String messageDecrypted = new String(
                            Objects.requireNonNull(EncryptRSA.decrypt(Base64.getDecoder().decode(recievedMessage), keys.getPrivate())), "UTF-8");
                    System.out.println("from Server: '" + messageDecrypted + "'");
                    if (messageDecrypted.equalsIgnoreCase("/exit")) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("server closed,please restart client while server is running ");
        }
    }
}



