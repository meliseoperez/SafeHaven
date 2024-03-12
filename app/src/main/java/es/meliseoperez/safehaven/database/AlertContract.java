package es.meliseoperez.safehaven.database;

import android.provider.BaseColumns;

public class AlertContract {
    private AlertContract(){

    }
    public static class AlertEntry implements BaseColumns {
        public static final String TABLE_NAME = "alerts";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_EFFECTIVE = "effective";
        public static final String COLUMN_ONSET = "onset";
        public static final String COLUMN_EXPIRES = "expires";
        public static final String COLUMN_SENDER_NAME = "sendername";
        public static final String COLUMN_HEADLINE = "headline";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_INSTRUCTION = "instruction";
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_POLYGON ="polygon";
    }

}
