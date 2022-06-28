import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

// класс с набором полей и функций
public class Main {
    // размер одного символа
    public static final int SYMBOL = 16;
    // верхний предел для символа в бинарном виде (понадобится для перевода символов в бинарный код)
    public static final int UPPER_LIMIT_SYMBOL = pow(BigInteger.TWO, SYMBOL - 1).intValue();
    // минимальный размер блока
    public static final int LOWER_LIMIT_BLOCK = SYMBOL * 2;
    // размер блока
    public static final int BLOCK = LOWER_LIMIT_BLOCK * 4;

    // генерация открытой экспоненты e
    // в качестве параметра передаётся значение функции Эйлера от n
    public static BigInteger generateE(BigInteger fEulerFromN) {

        BigInteger e;
        do {
            e = new BigInteger(fEulerFromN.bitLength(), new Random());
            if (e.compareTo(fEulerFromN) >= 0)
                e = e.subtract(fEulerFromN);
            if (e.equals(BigInteger.ZERO) || e.equals(BigInteger.ONE))
                e = BigInteger.TWO;
            // для проверки того, что е является взаимно простым со значением функции Эйлера от n,
            // используется алгоритм Евклида
        } while (!algorithmEuclidean(e, fEulerFromN).equals(BigInteger.ONE));

        return e;
    }

    // алгоритм Евклида
    public static BigInteger algorithmEuclidean(BigInteger a, BigInteger b) {

        while (!b.equals(BigInteger.ZERO)) {
            BigInteger t = a;
            a = b;
            b = t.mod(b);
        }

        return a;
    }

    // генерация закрытой экспоненты d
    // в качестве параметров передаётся значение открытой экспоненты е и значение функции Эйлера от n
    public static BigInteger generateD(BigInteger e, BigInteger fEulerFromN) {

        BigInteger fEulerFromNCopy = fEulerFromN;
        // иницализация коэффициентов Безу
        BigInteger x0 = BigInteger.ONE;
        BigInteger d = BigInteger.ZERO;
        BigInteger x1 = BigInteger.ZERO;
        BigInteger y1 = BigInteger.ONE;
        // проверка на равенство нулю остатка
        while (!e.equals(BigInteger.ZERO)) {
            BigInteger q = fEulerFromNCopy.divide(e);
            BigInteger r = fEulerFromNCopy.mod(e);
            fEulerFromNCopy = e;
            e = r;
            BigInteger t = x0;
            x0 = x1;
            x1 = t.subtract(q.multiply(x1));
            t = d;
            d = y1;
            y1 = t.subtract(q.multiply(y1));
        }
        // проверка, что d не отрицательно
        if (d.compareTo(BigInteger.ZERO) < 0)
            d = fEulerFromN.add(d);

        return d;
    }

    // генерация простого числа
    // в качестве параметра передаётся длина числа в битах
    public static BigInteger generatePrime(int bitLength) {

        BigInteger max = pow(BigInteger.TWO, bitLength).subtract(BigInteger.ONE);
        BigInteger min = pow(BigInteger.TWO, bitLength - 1);
        BigInteger range = max.subtract(min);
        BigInteger num = new BigInteger(range.bitLength(), new Random()).add(min);
        // проверка на простоту с помощью теста Миллера-Рабина
        while (!isPrime(num)) {
            num = new BigInteger(range.bitLength(), new Random()).add(min);
        }

        return num;
    }

    // тест Миллера-Рабина на простоту
    public static boolean isPrime(BigInteger num) {
        // число раундов
        BigInteger n = log2(num);

        if (num.equals(BigInteger.TWO))
            return true;

        if (num.mod(BigInteger.TWO).equals(BigInteger.ZERO))
            return false;

        int s = 0;
        BigInteger d = num.subtract(BigInteger.ONE);
        // нахождение чисел s и d
        while (d.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
            s++;
            d = d.divide(BigInteger.TWO);
        }
        // число итераций (проверок на простоту), равных числу раундов
        for (BigInteger i = BigInteger.ZERO; i.compareTo(n) < 0; i = i.add(BigInteger.ONE)) {
            // генерация случайного числа в диапазоне от 2 до числа num, которое проверяем на простоту
            BigInteger a = new BigInteger(num.bitLength(), new Random());
            if (a.compareTo(num) >= 0)
                a = a.subtract(num);
            if (a.equals(BigInteger.ZERO) || a.equals(BigInteger.ONE))
                a = BigInteger.TWO;
            // проверка числа а на свидетеля простоты
            BigInteger result = powMod(a, d, num);
            if (result.equals(BigInteger.ONE) || result.equals(num.subtract(BigInteger.ONE)))
                continue;

            int r = 1;
            while (r < s) {
                result = powMod(a, pow(BigInteger.TWO, r).multiply(d), num);
                if (result.equals(num.subtract(BigInteger.ONE)))
                    break;
                r++;
            }
            // если число а не является свидетелем простоты, то объявляем число num составным
            if (r == s)
                return false;
            // иначе проверяем дальше различные числа а на свидетелей простоты
        }

        return true;
    }

    // логарифм числа num по основанию 2
    public static BigInteger log2(BigInteger num) {

        int high = num.bitLength();
        int low = num.bitLength() - 1;

        BigInteger highE = pow(BigInteger.TWO, high).subtract(num);
        BigInteger lowE = num.subtract(pow(BigInteger.TWO, low));

        if (highE.compareTo(lowE) <= 0) {
            return BigInteger.valueOf(high);
        }
        else
            return BigInteger.valueOf(low);
    }

    // быстрое возведение в степень по модулю
    public static BigInteger powMod(BigInteger indicator, BigInteger degree, BigInteger num) {
        // перевод в бинарный код степени
        ArrayList<Integer> bin = toBinary(degree);
        BigInteger result = BigInteger.ONE;
        // число итераций зависит от длины бинарного кода степени
        for (int i = 0; i < bin.size(); i++) {
            if (bin.get(i) == 1)
                result = result.multiply(indicator).mod(num);

            indicator = indicator.multiply(indicator).mod(num);
        }

        return result;
    }

    // быстрое возведение в степень
    public static BigInteger pow(BigInteger indicator, int degree) {
        // перевод в бинарный код степени
        ArrayList<Integer> bin = toBinary(BigInteger.valueOf(degree));
        BigInteger result = BigInteger.ONE;
        // число итераций зависит от длины бинарного кода степени
        for (int i = 0; i < bin.size(); i++) {
            if (bin.get(i) == 1)
                result = result.multiply(indicator);

            indicator = indicator.multiply(indicator);
        }

        return result;
    }

    // перевод в бинарный код числа
    public static ArrayList<Integer> toBinary(BigInteger degree) {

        ArrayList<Integer> bin = new ArrayList<>();
        while (degree.compareTo(BigInteger.ZERO) > 0) {
            bin.add(degree.mod(BigInteger.TWO).intValue());
            degree = degree.divide(BigInteger.TWO);
        }

        return bin;
    }

    // шифрование
    public static String encrypt(String message, BigInteger e, BigInteger n) {
        // массив зашифрованных блоков
        String[] encryptedMessageBlocks;
        // переводим сообщение в массив символов
        char[] chars = message.toCharArray();
        StringBuilder binary = new StringBuilder();
        int index = 0;
        // перевод каждого символа в бинарный код
        for (char c : chars) {
            int num = c;
            for (int i = 0; i < SYMBOL; i++) {
                binary.append((num & UPPER_LIMIT_SYMBOL) == 0 ? 0 : 1);
                num <<= 1;
            }
        }
        // добор нулей в конец для ровного разделения на блоки
        while (binary.length() % BLOCK != 0)
            binary.append(0);
        // длина массива равна числу блоков
        encryptedMessageBlocks = new String[binary.length() / BLOCK];

        for (int i = 0; i < encryptedMessageBlocks.length; i++){
            // беру блок и перевожу в десятичную систему счисления
            BigInteger num = new BigInteger(binary.substring(index, index + BLOCK), 2);
            // шифрую число быстрым возведением в степень по модулю
            encryptedMessageBlocks[i] = powMod(num, e, n).toString();
            index += BLOCK;
        }
        // возвращаю массив зашифрованных блоков в виде строки
        return Arrays.toString(encryptedMessageBlocks);
    }

    // расшифрование
    public static String decrypt(String encryptedMessage, BigInteger d, BigInteger p, BigInteger q) {
        // делю строку на блоки
        String[] encryptedMessageBlocks = encryptedMessage.substring(1, encryptedMessage.length() - 1).split(", ");
        StringBuilder message = new StringBuilder();
        int index = 0;
        // для расшифрования используется китайская теорема об остатках
        for (int i = 0; i < encryptedMessageBlocks.length; i++) {
            // производится деление по модулю p
            BigInteger r1 = powMod(new BigInteger(encryptedMessageBlocks[i]), d, p);
            // производится деление по модулю q
            BigInteger r2 = powMod(new BigInteger(encryptedMessageBlocks[i]), d, q);
            // нахождение обратного q по модулю p
            BigInteger q_1 = generateD(q, p);
            // нахождение обратного p по модулю q
            BigInteger p_1 = generateD(p, q);
            BigInteger x = r1.multiply(q).multiply(q_1).add(r2.multiply(p).multiply(p_1));
            // последний этап расшифрования числа и перевод его в бинарный код
            BigInteger result = x.mod(p.multiply(q));
            StringBuilder binary = new StringBuilder(result.toString(2));
            // добор нулей в начало до размера блока
            while (binary.length() < BLOCK)
                binary.insert(0, "0");
            // число итераций равно числу символов в блоке
            for (int j = 0; j < binary.length() / SYMBOL; j++) {
                // извлечение бинарного кода символа, перевод его в десятичную систему счисления и после — перевод в символ
                int num = Integer.parseInt(binary.substring(index, index + SYMBOL), 2);
                if (num == 0)
                    break;
                message.append((char) num);
                index += SYMBOL;
            }
            index = 0;
        }
        // возвращает расшифрованное сообщение
        return message.toString();
    }
}
