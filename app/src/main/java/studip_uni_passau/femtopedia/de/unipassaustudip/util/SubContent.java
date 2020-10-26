package studip_uni_passau.femtopedia.de.unipassaustudip.util;

import de.femtopedia.studip.json.Course;
import de.femtopedia.studip.json.SubFile;
import de.femtopedia.studip.json.SubFolder;

public class SubContent {

    private final SubType type;
    private final SubFile file;
    private final SubFolder folder;
    private final Course course;

    public SubContent(SubFile file) {
        type = SubType.FILE;
        this.file = file;
        folder = null;
        course = null;
    }

    public SubContent(SubFolder folder) {
        type = SubType.FOLDER;
        file = null;
        this.folder = folder;
        course = null;
    }

    public SubContent(Course course) {
        type = SubType.COURSE;
        file = null;
        folder = null;
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

    public enum SubType {
        FILE, FOLDER, COURSE
    }
}
