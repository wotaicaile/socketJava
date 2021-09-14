# socketJava
双向rsa加密解密，服务器客户端随时双向通信，服务器提供文件下载  
1、运行server.java  
2、运行client.java  
双向随时发消息，通过线程接受   
在client端输入get xxx.后缀名，即可开启文件传输线程，默认下载到client同级目录，文件名相同。  
实验过程：  
1、启动server：  
![image](https://user-images.githubusercontent.com/50273609/133199772-d59cf0ae-f8b1-45fc-a5e2-c83ab9a7eaa5.png)   
2、启动client：  
![image](https://user-images.githubusercontent.com/50273609/133199821-87e45b94-0d6e-4916-a702-da59fd851948.png)  
3、两个进程自动互相创建rsa密钥，并发送给对方：  
![image](https://user-images.githubusercontent.com/50273609/133199886-b8e617f0-7854-47ee-84af-2d842d377d57.png)  
![image](https://user-images.githubusercontent.com/50273609/133199893-e3494a90-8bb9-482e-88c7-f3e598b47397.png)  
4、在client端输入：hello
![image](https://user-images.githubusercontent.com/50273609/133199990-0628d24f-0003-4150-8780-73a2553db80c.png)  
server端收到：  
![image](https://user-images.githubusercontent.com/50273609/133200019-68ab0270-73a7-4c56-a5af-0a014d856265.png)  
5、服务器端输入：hello：  
![image](https://user-images.githubusercontent.com/50273609/133200085-be2d2632-774c-4c88-bbf3-6aab0b9bf6c9.png)
client端收到：  
![image](https://user-images.githubusercontent.com/50273609/133200120-19f836c6-e813-4267-b2bc-4106a2d1a213.png)  
6、client端输入：get test.txt  
![image](https://user-images.githubusercontent.com/50273609/133200215-4eabf93b-54d3-4358-984d-7526526e074e.png)
由于server和client在同一个目录下，所以传输的文件覆盖了  
![image](https://user-images.githubusercontent.com/50273609/133201583-5c83f231-28ea-44db-84e9-eae3d326c2a9.png)

