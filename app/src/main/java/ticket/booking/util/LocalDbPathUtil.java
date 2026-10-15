package ticket.booking.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Resolves writable local DB JSON files whether the process is started from
 * the repo root or the app module directory.
 */
public final class LocalDbPathUtil {

    private LocalDbPathUtil() {}

    public static File resolveDbFile(String fileName) {
        Path[] candidates = {
                Paths.get("app", "src", "main", "resources", "localDb", fileName),
                Paths.get("src", "main", "resources", "localDb", fileName),
                Paths.get("localDb", fileName)
        };

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate.toFile();
            }
        }

        Path preferred = Files.exists(Paths.get("app")) ? candidates[0] : candidates[1];
        try {
            Files.createDirectories(preferred.getParent());
        } catch (Exception ignored) {
            // Caller will surface IO errors on read/write.
        }
        return preferred.toFile();
    }
}
