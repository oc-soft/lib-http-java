package net.oc_soft.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.lang.ref.Cleaner;
import java.nio.file.Path;
import java.nio.file.Files;


/**
 * body contents which are stored file.
 */
public class BodyFileContents implements Body.Contents {

    /**
     * cleaner instance
     */
    private static Cleaner CLEANER;

    /**
     * get cleaner instance
     */
    static synchronized Cleaner getClenaer() {
        if (CLEANER == null) {
            CLEANER = Cleaner.create();
        }
        return CLEANER;
    }



    /**
     * create temporary file.
     */
    static Path createTempFile()
        throws IOException {
        return File.createTempFile("htb", null).toPath();
    }

    /**
     * create body contens which keep contents in a file.
     */
    static BodyFileContents create(InputStream stream) throws IOException {
        var path = createTempFile();
        try (var outStream = Files.newOutputStream(path)) {
            stream.transferTo(outStream);
        }
        return new BodyFileContents(path, true);
    } 


    /**
     * path which contains http body contents.
     */
    private Path path;

    /**
     * delete path if this field true
     */
    private boolean deletePathOnClose;
    
    /**
     * clean resource 
     */
    private Cleaner.Cleanable resourceCleaner; 

    /**
     * consturct body contents which are saved into path.
     * @param path It keep body contents
     */
    BodyFileContents(Path path, boolean deletePath) {
        this.path = path;
        this.deletePathOnClose = deletePath;
        
        if (deletePath) {
            final var pathRef = path;
            resourceCleaner = getClenaer().register(this, ()-> { 
                pathRef.toFile().delete();
            }); 
        }
    }


    /**
     * close body contents
     */
    public void close() {
        resourceCleaner.clean();
        this.resourceCleaner = null;
        this.path = null;
    }

    /**
     * get input stream;
     */
    public InputStream getInputStream() {
        var path = this.path;
        InputStream result = null;
        if (path != null) {
            try {
                result = Files.newInputStream(path);
            } catch (IOException ex) {
            }
        }
        return result;
    }
}


// vi: se ts=4 sw=4 et:
