package omsu.omsuts.webcore;

import static spark.Spark.*;

/**
 * Created by sds on 07.06.16.
 */
public class Main {
    public static void main(String[] args) {
        get("/hello", (req, res) -> "Hello World");
    }
}

