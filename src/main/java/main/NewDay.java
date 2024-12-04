package main;

import static functionalj.function.Func.f;
import static java.lang.Integer.parseInt;


public class NewDay {
    
    public static void main(String... args) throws Exception {
        args = new String[] { "5" };
        if (args.length == 0) {
            System.out.println("Usage: NewDay.java <date>");
            return;
        }
        
        int day    = parseInt(args[0]);
        var mapper = f((String p) -> p.replaceAll("([dD]ay)X", "$1" + day));
        var copier = new FilesCopier(mapper);
        copier.copyFiles("src/test/java/dayX");
        copier.copyFiles("data/dayX");
    }
}