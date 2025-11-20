package TODO;
import java.io.Serializable;

public class Task implements Serializable {
    private String text;

    public Task(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }
}
