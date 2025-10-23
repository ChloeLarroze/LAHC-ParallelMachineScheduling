//./domain/Job.java
package domain;
import java.util.Objects;

/**
 * Représente un job à ordonnancer.
 * Chaque job possède une date de disponibilité (release date) et des temps de traitement
 * qui varient selon la machine sur laquelle il est exécuté.
 */

public class Job {
    private final int id;
    private final int releaseDate;
    
    //constructor with id and release date
    public Job(int id, int releaseDate) {
        this.id = id;
        this.releaseDate = releaseDate;
    }
    
    //getters / setters
    public int getId() {
        return id;
    }
    
    public int getReleaseDate() {
        return releaseDate;
    }

    //functs and helpers for equals,  and toString
    //two jobs are equal if they have the same id
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return id == job.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    //toString returns "J{id}(r={releaseDate})" -> e.g. J3(r=5)
    @Override
    public String toString() {
        return String.format("J%d(r=%d)", id, releaseDate);
    }
}



// in the constructor we suppose everything is valid
// //check that id and release date are non-negative
//         if (id < 0) {
//             throw new IllegalArgumentException("id must be non-negative");
//         }
//         if (releaseDate < 0) {
//             throw new IllegalArgumentException("releaseDate must be non-negative");
//         }
