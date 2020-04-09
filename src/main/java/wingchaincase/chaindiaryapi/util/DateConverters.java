package wingchaincase.chaindiaryapi.util;

import javax.persistence.AttributeConverter;
import java.util.Date;

public class DateConverters implements AttributeConverter<Date, Long> {

    @Override
    public Long convertToDatabaseColumn(Date attribute) {
        if (attribute == null) {
            return 0L;
        }
        return attribute.getTime() / 1000;
    }

    @Override
    public Date convertToEntityAttribute(Long dbData) {
        return new Date(dbData * 1000);
    }
}
