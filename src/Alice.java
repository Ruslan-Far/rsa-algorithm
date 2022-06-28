import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Scanner;
// класс клиента
public class Alice {
    // адрес сервера
    public static String IPAddress = "localhost";
    public static int port = 1010;
    // условие на выход
    public static final String EXIT = "выход";
    // минимальный предел возможной длины чисел p и q
    private static final int LOWER_LIMIT_PQ = Main.BLOCK / 2;
    // сокет клиента
    private static Socket socket;
    // потоки ввода-вывода для общения с сервером
    private static BufferedReader serverReader;
    private static PrintWriter serverWriter;
    // открытый и закрытый ключи
    public static BigInteger n;
    public static BigInteger e;
    private static BigInteger d;
    // числа p и q и значение функции Эйлера
    private static BigInteger p;
    private static BigInteger q;
    private static BigInteger fEulerFromN;
    // открытый ключ сервера
    private static BigInteger serverE;
    private static BigInteger serverN;

    public static void main(String[] args) {

        try {
            try {
                // создание сокета и потоков ввода-вывода
                socket = new Socket(IPAddress, port);
                serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                serverWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                // инициализация полей
                initialize();
                // обмен открытыми ключами с сервером
                exchangeKeysWithServer();

                while (true) {

                    Scanner scanner = new Scanner(System.in);
                    // Ввод и отправка сообщения клиенту (Bob)
                    sendMessageToClient(scanner.nextLine());
                    // Получение сообщения от клиента (Bob)
                    System.out.println(getMessageFromClient());
                }
            } finally {
                serverReader.close();
                serverWriter.close();
                socket.close();
            }
        } catch (IOException | NullPointerException e) {
            System.out.println();
        }
    }


    public static void initialize() throws IOException {
        // генерация чисел p и q
        enterPQ();
        // нахождение n
        n = p.multiply(q);
        // нахождение значения функции Эйлера от n
        fEulerFromN = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        // нахождение e
        e = Main.generateE(fEulerFromN);
        // нахождение d
        d = Main.generateD(e, fEulerFromN);
        System.out.println("Для выхода нажмите \"" + EXIT + "\"");
    }


    public static void enterPQ() {

        p = Main.generatePrime(checkBitLength("p"));

        int bitLength = checkBitLength("q");

        q = Main.generatePrime(bitLength);

        while (p.equals(q))
            q = Main.generatePrime(bitLength);
    }

    // ввод и проверка длины чисел p и q
    public static int checkBitLength(String nameNum) {

        int bitLength = 0;

        while (bitLength < LOWER_LIMIT_PQ) {
            System.out.println("Введите длину (в битах) числа " + nameNum + " не меньше, чем " + LOWER_LIMIT_PQ);
            Scanner scanner = new Scanner(System.in);
            bitLength = scanner.nextInt();
        }

        return bitLength;
    }


    public static void exchangeKeysWithServer() throws IOException {
        // получение открытого ключа сервера
        String[] publicKeyServer = serverReader.readLine().split(";");
        serverE = new BigInteger(publicKeyServer[0]);
        serverN = new BigInteger(publicKeyServer[1]);
        // отправка своего открытого ключа серверу
        serverWriter.println(e + ";" + n);
    }


    public static String getMessageFromClient() throws IOException {
        // получить и расшифровать сообщение от клиента (Bob)
        return Main.decrypt(serverReader.readLine(), d, p, q);
    }


    public static void sendMessageToClient(String message) throws IOException {
        // зашифровать и отправить сообщение клиенту (Bob)
        serverWriter.println(Main.encrypt(message, serverE, serverN));
    }
}