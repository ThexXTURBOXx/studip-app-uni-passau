package studip_uni_passau.femtopedia.de.unipassaustudip;

import org.joda.time.DateTime;

public class ScheduledEvent {

    public DateTime start;
    public DateTime end;
    public String color;
    public String course;
    public String title;
    public String description;
    public String room;
    public String canceled;

    @Override
    public String toString() {
        return "Start: " + start + ", End: " + end + ", Title: " + title + ", Room: " + room + ", Canceled: " + canceled;
    }
}
