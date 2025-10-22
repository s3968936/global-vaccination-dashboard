package app;

/**
 * Class represeting a Movie from the Movies database
 * For simplicity this uses public fields
 * You could use private fields with getters to be safer
 *
 * @author Timothy Wiley, 2023. email: timothy.wiley@rmit.edu.au
 */
public class Movie {
   // Movie id
   public int id;

   // Movie name
   public String name;

   // Year the movie was made
   public int year;

   // String representing the movie genre
   public String genre;

   /**
    * Create a movie withou setting any of the fields,
    * Fields will have default "empty" values
    */
   public Movie() {
   }

   /**
    * Create a movie setting all of the fields
    */
   public Movie(int id, String name, int year, String genre) {
      this.id = id;
      this.name = name;
      this.year = year;
      this.genre = genre;
   }
}
