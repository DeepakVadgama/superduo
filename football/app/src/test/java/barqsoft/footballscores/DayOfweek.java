package barqsoft.footballscores;

import org.junit.Test;

import java.util.Calendar;
import java.util.Locale;


public class DayOfweek {

    @Test
    public void testDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 2);
        System.out.println(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()));
        assert true;
    }
}
