package domain;
import java.util.Date;


public class Machine {
    private final int id;
    
    public Machine(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("Machine ID must be non-negative");
        }
        this.id = id;
    }
    
    //getter
    public int getId() {
        return id;
    }
    
    //toString returns "M{id}" -> e.g. M2
    @Override
    public String toString() {
        return "M" + id;
    }
}