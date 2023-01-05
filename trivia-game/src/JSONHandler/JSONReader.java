package JSONHandler;

import java.io.*;
import java.util.*;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class JSONReader {
    static User Host;
    static ArrayList<User> Clients;

    public static void main(String[] args) {
        System.out.println(getQuestions("src/JSONHandler/questions.json").get(0).getQuestion());
    }

    public static User getHost(String fileAdd) {
        if (Host == null) {
            for (User u : getUsers(fileAdd)) {
                if (u.getType().equals("host")) {
                    Host = u;
                    break;
                }
            }
        }
        return Host;
    }

    public static ArrayList<User> getClients(String fileAdd) {
        if (Clients == null) {
            Clients = new ArrayList<>();
            for (User u : getUsers(fileAdd)) {
                if (u.getType().equals("client")) {
                    Clients.add(u);
                }
            }
        }
        return Clients;
    }

    public static ArrayList<User> getUsers(String fileAdd) {
        ArrayList<User> user = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            JSONArray obj = (JSONArray) parser.parse(new FileReader(fileAdd));
            for (Object o : obj) {
                JSONObject jsonObject = (JSONObject) o;
                String type = (String) jsonObject.get("type");
                long port = (long) jsonObject.get("port");
                String name = (String) jsonObject.get("name");
                user.add(new User(type, port, name));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public static ArrayList<Question> getQuestions(String fileAdd) {

        ArrayList<Question> questions = new ArrayList<>();
        JSONParser parser = new JSONParser();
        try {
            JSONArray obj = (JSONArray) parser.parse(new FileReader(fileAdd));
            for (Object o : obj) {
                JSONObject jsonObject = (JSONObject) o;
                String question = (String) jsonObject.get("question");
                long answer = (long) jsonObject.get("answer");
                JSONArray options = (JSONArray) jsonObject.get("options");
                ArrayList<String> op = new ArrayList<>();
                Iterator iterator = options.iterator();
                while (iterator.hasNext()) {
                    op.add(iterator.next().toString());
                }
                questions.add(new Question(question, op, answer));


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return questions;
    }


}
