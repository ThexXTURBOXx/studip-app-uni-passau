package studip_uni_passau.femtopedia.de.unipassaustudip.util;

import de.femtopedia.studip.json.Course;
import de.femtopedia.studip.json.SubFile;
import de.femtopedia.studip.json.SubFolder;

public class SubContent {

    public enum SubType {
        FILE, FOLDER, COURSE
    }

    private final SubType type;
    private final SubFile file;
    private final SubFolder folder;
    private final Course course;

    public SubContent(SubFile file) {
        this.type = SubType.FILE;
        this.file = file;
        this.folder = null;
        this.course = null;
    }

    public SubContent(SubFolder folder) {
        this.type = SubType.FOLDER;
        this.file = null;
        this.folder = folder;
        this.course = null;
    }

    public SubContent(Course course) {
        this.type = SubType.COURSE;
        this.file = null;
        this.folder = null;
        this.course = course;
    }

    public SubType getType() {
        return type;
    }

    public SubFile getFile() {
        return file;
    }

    public SubFolder getFolder() {
        return folder;
    }

    public Course getCourse() {
        return course;
    }
}
