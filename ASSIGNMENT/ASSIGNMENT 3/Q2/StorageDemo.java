import java.io.IOException;

class ExamException extends Exception {
    ExamException(String message) { super(message); }
    ExamException(String message, Throwable cause) { super(message, cause); }
}

class ExamWindowClosedException extends ExamException {
    ExamWindowClosedException(String message) { super(message); }
}

class StudentNotRegisteredException extends ExamException {
    StudentNotRegisteredException(String message) { super(message); }
}

class SubmissionStorageException extends ExamException {
    SubmissionStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}

class Storage {
    void save(String fileName, long sizeKb) throws SubmissionStorageException {
        try {
            if (sizeKb > 500) throw new IOException("Disk quota exceeded");
            System.out.println("Saved " + fileName);
        } catch (IOException e) {
            // TODO 1: Wrap the IOException in a SubmissionStorageException, keeping the IOException as its cause
            throw new SubmissionStorageException("Could not store " + fileName, e);
        }
    }
}

class SubmissionService {
    private final Storage storage = new Storage();

    void submitAnswer(String regNo, boolean registered, String fileName, long sizeKb) throws ExamException {
        if (!registered) {
            throw new StudentNotRegisteredException("Student " + regNo + " is not enrolled in CSA09");
        }
        storage.save(fileName, sizeKb);
    }
}

class SubmissionController {
    private final SubmissionService service = new SubmissionService();

    void handleUpload(String regNo, boolean registered, boolean windowOpen, String fileName, long sizeKb) {
        try {
            if (!windowOpen) {
                throw new ExamWindowClosedException("Submission window closed at 17:00 IST");
            }
            service.submitAnswer(regNo, registered, fileName, sizeKb);
        } catch (ExamException e) {
            System.out.println("UI Alert [" + e.getClass().getSimpleName() + "]: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("  Root Cause: " + e.getCause().getClass().getSimpleName() + " -> " + e.getCause().getMessage());
            }
        }
    }
}

public class StorageDemo {
    public static void main(String[] args) {
        // Exact Complete-the-Code verification
        Storage storage = new Storage();
        try {
            storage.save("answer01.pdf", 120);
            storage.save("answer02.pdf", 800);
        } catch (ExamException e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "null"));
        }

        // Full 3-layer propagation scenarios
        System.out.println("\n--- 3-Layer Portal Scenarios ---");
        SubmissionController controller = new SubmissionController();
        controller.handleUpload("192524478", true, false, "ans.pdf", 100);
        controller.handleUpload("192599999", false, true, "ans.pdf", 100);
        controller.handleUpload("192524478", true, true, "ans_large.pdf", 950);
    }
}
