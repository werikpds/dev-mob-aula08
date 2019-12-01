package aula8.com.br.chat_firebase;

import java.text.SimpleDateFormat;
import java.util.Date;

class DateHelper {

    public static String format (Date date){
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
    }
}
