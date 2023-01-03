import java.util.ArrayList;

public class Question {
    String question;
    ArrayList<String> options;
    long answer;

    public Question(String question, ArrayList<String> options, long answer) {
        this.question = question;
        this.options = options;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public long getAnswer() {
        return answer;
    }
}
