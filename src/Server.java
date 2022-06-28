import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
// класс сервера
public class Server {
    // номер порта
    public static final int PORT = 1010;
    // условие на выход
    public static final String EXIT = "выход";

    private static ServerSocket server;
    // список сокетов клиентов
    private static LinkedList<Socket> listClientSockets;
    // потоки ввода-вывода для общения с клиентами
    private static BufferedReader clientReader;
    private static PrintWriter clientWriter;
    private static BufferedReader client2Reader;
    private static PrintWriter client2Writer;
    // открытый и закрытый ключи
    public static BigInteger n;
    public static BigInteger e;
    private static BigInteger d;
    // длина по умолчанию чисел p и q
    private static final int BIT_LENGTH = 512;
    // числа p и q и значение функции Эйлера
    private static BigInteger p;
    private static BigInteger q;
    private static BigInteger fEulerFromN;
    // открытые ключи двух клиентов
    private static BigInteger clientE;
    private static BigInteger clientN;
    private static BigInteger client2E;
    private static BigInteger client2N;

    public static void main(String[] args) {

        try {
            try {
                // инициализация полей
                initialize();
                // запуск сервера
                server = new ServerSocket(PORT);

                listClientSockets = new LinkedList<>();

                System.out.println("Сервер запущен");
                // присоединение клиентов
                connectClients();
                // обмен открытыми ключами с клиентами
                exchangeKeysWithClients();

                while (true) {
                    // получить сообщение от клиента (Alice)
                    String message = getMessageFromClient();
                    // проверка на выход
                    checkExit(message);

                    System.out.println(message);
                    // отправить сообщение клиенту 2 (Bob)
                    sendMessageToClient2(message);
                    // получить сообщение от клиента 2 (Bob)
                    message = getMessageFromClient2();

                    checkExit(message);

                    System.out.println(message);
                    // отправить сообщение клиенту (Alice)
                    sendMessageToClient(message);
                }
            } finally {
                for (int i = 0; i < 2; i++)
                    listClientSockets.get(i).close();
                clientReader.close();
                clientWriter.close();
                client2Reader.close();
                client2Writer.close();
                server.close();
                System.out.println("Сервер остановлен");
            }
        } catch (IOException | RuntimeException e) {
            System.out.println();
        }
    }


    public static void initialize() {
        // генерация p и q
        p = Main.generatePrime(BIT_LENGTH);
        q = Main.generatePrime(BIT_LENGTH);
        while (p.equals(q))
            q = Main.generatePrime(BIT_LENGTH);
        // нахождение n
        n = p.multiply(q);
        // нахождение значения функции Эйлера от n
        fEulerFromN = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        // нахождение e
        e = Main.generateE(fEulerFromN);
        // нахождение d
        d = Main.generateD(e, fEulerFromN);
    }


    public static void connectClients() throws IOException {

        for (int i = 0; i < 2; i++)
            listClientSockets.add(server.accept());
        // создание потоков ввода-вывода
        clientReader = new BufferedReader(new InputStreamReader(listClientSockets.get(0).getInputStream()));
        clientWriter = new PrintWriter(new OutputStreamWriter(listClientSockets.get(0).getOutputStream()), true);
        client2Reader = new BufferedReader(new InputStreamReader(listClientSockets.get(1).getInputStream()));
        client2Writer = new PrintWriter(new OutputStreamWriter(listClientSockets.get(1).getOutputStream()), true);
    }


    public static void exchangeKeysWithClients() throws IOException {

        String[] publicKey;
        // отправка открытого ключа клиенту (Alice)
        clientWriter.println(e + ";" + n);
        // получение открытого ключа от клиента (Alice)
        publicKey = clientReader.readLine().split(";");
        clientE = new BigInteger(publicKey[0]);
        clientN = new BigInteger(publicKey[1]);
        // отправка открытого ключа клиенту 2 (Bob)
        client2Writer.println(e + ";" + n);
        // получение открытого ключа от клиента 2 (Bob)
        publicKey = client2Reader.readLine().split(";");
        client2E = new BigInteger(publicKey[0]);
        client2N = new BigInteger(publicKey[1]);
    }


    public static String getMessageFromClient() throws IOException {
        // получить и расшифровать сообщение от клиента (Alice)
        return Main.decrypt(clientReader.readLine(), d, p, q);
    }


    public static void sendMessageToClient(String message) {
        // зашифровать и отправить сообщение клиенту (Alice)
        clientWriter.println(Main.encrypt(message, clientE, clientN));
    }


    public static String getMessageFromClient2() throws IOException {
        // получить и расшифровать сообщение от клиента 2 (Bob)
        return Main.decrypt(client2Reader.readLine(), d, p, q);
    }


    public static void sendMessageToClient2(String message) {
        // зашифровать и отправить сообщение клиенту 2 (Bob)
        client2Writer.println(Main.encrypt(message, client2E, client2N));
    }


    public static void checkExit(String message) {
        // если условие на выход выполняется, то работа завершается
        if (message.equals(EXIT)) {
            System.out.println(message);
            throw new RuntimeException();
        }
    }
}