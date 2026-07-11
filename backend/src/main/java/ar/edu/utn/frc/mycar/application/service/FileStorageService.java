package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.web.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Stream;

/** Stores and retrieves document PDF files on the local filesystem. */
@Service
public class FileStorageService {

    private final Path rootDir;

    public FileStorageService(@Value("${app.storage.documents-dir}") String documentsDir) {
        this.rootDir = Path.of(documentsDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear el directorio de almacenamiento de documentos.", e);
        }
    }

    /**
     * Validates and saves the file under a per-vehicle subfolder, returning its relative path.
     * Any previously stored file at {@code previousRelativePath} is deleted.
     */
    public String store(Long vehicleId, MultipartFile file, String previousRelativePath) {
        validate(file);

        String extension = ".pdf";
        String relativePath = vehicleId + "/" + UUID.randomUUID() + extension;
        Path target = rootDir.resolve(relativePath).normalize();

        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo.", e);
        }

        if (previousRelativePath != null) {
            deleteQuietly(previousRelativePath);
        }

        return relativePath;
    }

    public Resource load(String relativePath) {
        try {
            Path file = rootDir.resolve(relativePath).normalize();
            return new UrlResource(file.toUri());
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Removes every file stored for a vehicle (best-effort), used when purging a user's personal data. */
    public void deleteVehicleFolder(Long vehicleId) {
        Path dir = rootDir.resolve(String.valueOf(vehicleId)).normalize();
        if (!dir.startsWith(rootDir) || !Files.isDirectory(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(this::deletePathQuietly);
        } catch (IOException ignored) {
            // best-effort cleanup, not worth failing the request over
        }
    }

    private void deletePathQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // best-effort cleanup, not worth failing the request over
        }
    }

    private void deleteQuietly(String relativePath) {
        try {
            Files.deleteIfExists(rootDir.resolve(relativePath).normalize());
        } catch (IOException ignored) {
            // best-effort cleanup, not worth failing the request over
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("El archivo está vacío.");
        }
        if (!"application/pdf".equals(file.getContentType())) {
            throw new InvalidFileException("Solo se permiten archivos PDF.");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
            throw new InvalidFileException("Solo se permiten archivos PDF.");
        }
    }
}
