package studip_uni_passau.femtopedia.de.unipassaustudip;

public class ScheduledEvent {

    long start;
    long end;
    String color;
    String course;
    String title;
    String description;
    String room;
    String canceled;

    @Override
    public String toString() {
        return "Start: " + start + ", End: " + end + ", Title: " + title + ", Room: " + room + ", Canceled: " + canceled;
    }
}
