package es.meliseoperez.safehaven.database;

public class SecondTableContract {
    // Constructor privado para evitar la instanciación accidental
    private SecondTableContract() {}

        public static class SecondTableEntry {
        public static final String TABLE_NAME = "SecondTable";
        public static final String _ID = "_id";
        public static final String COLUMN_POLYGON = "polygon";
        public static final String COLUMN_INSTRUCTION = "instruction";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_EXPIRES = "expires";
        public static final String COLUMN_HEADLINE = "headline";
    }

}
