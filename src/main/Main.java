import http.HttpTaskServer;
import http.KVServer;

import java.io.IOException;

public class Main {


    public static void main(String[] args) throws IOException {

        KVServer nn = new KVServer();
        nn.start();
        HttpTaskServer httpTaskServer = new HttpTaskServer();

    }
}
