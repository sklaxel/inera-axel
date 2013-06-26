package se.inera.axel.shs.cmdline;

import com.beust.jcommander.IStringConverter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class ISO8601DateTimeConverter implements IStringConverter<DateTime> {
    DateTimeFormatter dateFormatter = ISODateTimeFormat.dateHourMinuteSecond();

    @Override
    public DateTime convert(String s) {
        return dateFormatter.parseDateTime(s);
    }
}
