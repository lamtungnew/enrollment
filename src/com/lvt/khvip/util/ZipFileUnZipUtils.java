package com.lvt.khvip.util;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class ZipFileUnZipUtils {
    public static List<String> unzipzarFolder(Path source, Path target, String folderExceltion) throws Exception {
        try {
            if (source.getFileName().toString().toLowerCase().indexOf(".zip") >= 0) {
                return unzipFolder(source, target, folderExceltion);
            } else {
                return unzarFolder(source, target, folderExceltion);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public static List<String> unzarFolder(Path source, Path target, String folderExceltion) throws Exception {
        List<String> lstFile = new ArrayList<>();
        Archive a = new Archive(new FileInputStream(source.toFile()));
        if (a != null) {
            a.getMainHeader().print();
            FileHeader fh = a.nextFileHeader();
            while (fh != null) {
                String compressFileName = fh.getFileNameString().trim();
                File out = new File(target + "/" + compressFileName);
                lstFile.add(out.getName());
                log.info("unzarFolder: " + out.getAbsolutePath());
                FileOutputStream os = new FileOutputStream(out);
                a.extractFile(fh, os);
                os.close();

                fh = a.nextFileHeader();
            }
        }
        return lstFile;
    }

    public static List<String> unzarFolder1(Path source, Path target, String folderExceltion) throws Exception {
        List<String> lstFile = new ArrayList<>();
        try (Archive archive = new Archive(new FileInputStream(source.toFile()))) {
            FileHeader fh = archive.nextFileHeader();
            while (fh != null) {
                String compressFileName = fh.getFileNameString().trim();
                File destFile = new File(target + "/" + compressFileName);
                lstFile.add(destFile.getName());
                if (fh.isDirectory()) {
                    if (!destFile.exists()) {
                        destFile.mkdirs();
                    }
                    fh = archive.nextFileHeader();
                    continue;
                }

                if (!destFile.getParentFile().exists()) {
                    destFile.getParentFile().mkdirs();
                }

                try (FileOutputStream fos = new FileOutputStream(destFile)) {
                    archive.extractFile(fh, fos);
                }
                fh = archive.nextFileHeader();
            }
        }
        return lstFile;
    }

    public static List<String> unzipFolder(Path source, Path target, String folderExceltion) throws Exception {
        List<String> lstFile = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source.toFile()))) {

            // list files in zip
            ZipEntry zipEntry = zis.getNextEntry();

            while (zipEntry != null) {

                boolean isDirectory = false;
                // example 1.1
                // some zip stored files and folders separately
                // e.g data/
                // data/folder/
                // data/folder/file.txt
                if (zipEntry.getName().endsWith("/")) {
                    isDirectory = true;
//					throw new Exception(folderExceltion);
                }

                Path newPath = zipSlipProtect(zipEntry, target);

                if (isDirectory) {
                    Files.createDirectories(newPath);
                } else {

                    // example 1.2
                    // some zip stored file path only, need create parent directories
                    // e.g data/folder/file.txt
                    if (newPath.getParent() != null) {
                        if (Files.notExists(newPath.getParent())) {
                            Files.createDirectories(newPath.getParent());
                        }
                    }

                    // copy files, nio
                    Files.copy(zis, newPath, StandardCopyOption.REPLACE_EXISTING);
                    lstFile.add(newPath.getFileName().toString());
                    // copy files, classic
                    /*
                     * try (FileOutputStream fos = new FileOutputStream(newPath.toFile())) { byte[]
                     * buffer = new byte[1024]; int len; while ((len = zis.read(buffer)) > 0) {
                     * fos.write(buffer, 0, len); } }
                     */
                }

                zipEntry = zis.getNextEntry();

            }
            zis.closeEntry();
        }
        return lstFile;
    }

    // protect zip slip attack
    public static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir) throws IOException {

        // test zip slip vulnerability
        // Path targetDirResolved = targetDir.resolve("../../" + zipEntry.getName());

        Path targetDirResolved = targetDir.resolve(zipEntry.getName());

        // make sure normalized file still has targetDir as its prefix
        // else throws exception
        Path normalizePath = targetDirResolved.normalize();
        if (!normalizePath.startsWith(targetDir)) {
            throw new IOException("Bad zip entry: " + zipEntry.getName());
        }

        return normalizePath;
    }
}
