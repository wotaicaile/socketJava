import java.io.*;
import java.net.*;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

class Server {
    public static void main(String[] arg) {
        try {
            System.out.println("wait for client...");
            ServerSocket serverSoc = new ServerSocket(9182);
            Socket soc = serverSoc.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            PrintWriter out = new PrintWriter(soc.getOutputStream(), true);

            System.out.println("Create keys...");
            //生成密钥
            KeyPair keys = EncryptRSA.generateKeyPair();

            String publicKeyToSend = Base64.getEncoder().encodeToString(keys.getPublic().getEncoded());
            out.println(publicKeyToSend);
            System.out.println("send keys...");

            //通过socket获取到的来自服务器端的公钥SPK
            String serverPublicKey = in.readLine();

            //RSA使用X509EncodedKeySpec还原公钥
            PublicKey publicKeyClient = KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(serverPublicKey)));

            System.out.println("recive keys...");
            // 新建写线程并启动
            ServerWrite writeforClient = new ServerWrite(soc, publicKeyClient);
            writeforClient.start();


            // 新建读取线程并启动
            ServerRead ServerReadfromClient = new ServerRead(soc, keys);
            ServerReadfromClient.start();

            System.out.println("Server starting...");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

//发送文件
class sendFile extends Thread{
    public Socket client = null;
    public DataOutputStream dos = null;
    public FileInputStream fis = null;
    public File file = null;
    String filepath = null;

    public sendFile (Socket c,String filepath)
    {	try{
        client = c;
        this.filepath = filepath;

        dos = new DataOutputStream(client.getOutputStream());

    }
    catch(Exception e)
    { } }
    public void run()
    {
            try{
//                获取输入的文件名字
                String filename = filepath.split(" ")[1];
                System.out.println(filename);

                if (filepath.split(" ")[0].equalsIgnoreCase("get")){
                    String filedata ="";
                    byte[] data;
                    System.out.println("downing……0%");

                    System.out.println("downing……10%");
                    file = new File(filename);
                    if(file.isFile())
                    {

                        fis = new FileInputStream(file);
                        data = new byte[fis.available()];

                        fis.read(data);

                        filedata = new String(data);

                        fis.close();

                        dos.writeUTF("downloading……100%");

                        dos.writeUTF(filedata);

                        dos.writeUTF(filename);

                    }
                    else
                    {
                        dos.writeUTF("NO FILE FOUND"); // NO FILE FOUND
                        System.out.println("NO FILE FOUND");
                    }
                }
          }
            catch(Exception e)
            { }
    }
}


    //服务器写进程
class ServerWrite extends Thread {
    Socket soc;
    PublicKey publicKeyClient;
    //以下为负责客户端输入和输出的流
    DataOutputStream dos = null;
    public ServerWrite(Socket soc,PublicKey publicKeyClient) {
        this.soc = soc;
        this.publicKeyClient = publicKeyClient;
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
                        .encodeToString(EncryptRSA.encrypt(messageIn.getBytes("UTF-8"), publicKeyClient));
                dos.writeUTF(messageInEncrypted);
                System.out.println("All messages sent to server are encrypted.");
            }
            System.out.println("Sending shutdown message to server...");
            // telling server to save and exit too
            dos.writeUTF(Base64.getEncoder()
                    .encodeToString(EncryptRSA.encrypt(new String("/exit").getBytes("UTF-8"), publicKeyClient)));
            System.out.println("Exiting client...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



//服务器端读进程
class ServerRead extends Thread {
    Socket soc;
    KeyPair keys;
    DataInputStream dis = null;

    //以下为负责客户端输入和输出的流
    public ServerRead(Socket soc,KeyPair keys) {
        this.soc = soc;
        this.keys = keys;
        this.dis = dis;
    }
    // 用于读取从服务端发送来的消息
    public void run() {
        try {
            dis = new DataInputStream(soc.getInputStream());
            while (true) {

                String recievedMessage = dis.readUTF();


                    //通过私钥解密
                    String messageDecrypted = new String(
                            Objects.requireNonNull(EncryptRSA.decrypt(Base64.getDecoder().decode(recievedMessage), keys.getPrivate())), "UTF-8");
                System.out.println("from Client: '" + messageDecrypted + "'");


                if (messageDecrypted.equalsIgnoreCase("/exit")) {
                        break;}

                sendFile sendfile = new sendFile(soc,messageDecrypted);
                sendfile.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

