import java.io.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class JSONReader {
    public static void main(String[] args) {
        JSONParser parser = new JSONParser();
        try {
            JSONArray obj = (JSONArray) parser.parse(new FileReader("users.json"));
            for(Object o: obj){
                JSONObject jsonObject = (JSONObject) o;
                String question = (String)jsonObject.get("question");
                long answer = (long)jsonObject.get("answer");
                JSONArray options = (JSONArray)jsonObject.get("options");
                System.out.println("question: " + question);
                System.out.println("answer: " + answer);
                System.out.println("options:");
                Iterator iterator = options.iterator();
                while (iterator.hasNext()) {
                    System.out.println(iterator.next());
                }
            }


        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
