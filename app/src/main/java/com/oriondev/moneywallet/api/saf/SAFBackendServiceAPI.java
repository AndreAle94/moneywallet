package com.oriondev.moneywallet.api.saf;

import android.content.Context;
import android.content.UriPermission;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.oriondev.moneywallet.api.AbstractBackendServiceAPI;
import com.oriondev.moneywallet.api.BackendException;
import com.oriondev.moneywallet.model.IFile;
import com.oriondev.moneywallet.model.SAFFile;
import com.oriondev.moneywallet.utils.ProgressInputStream;
import com.oriondev.moneywallet.utils.ProgressOutputStream;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import static com.oriondev.moneywallet.api.saf.SAFBackendService.getUri;

public class SAFBackendServiceAPI extends AbstractBackendServiceAPI<SAFFile> {

    private final Context mContext;
    private final DocumentFile mRoot;

    public SAFBackendServiceAPI(Context context) throws BackendException {
        super(SAFFile.class);
        mContext = context;
        Uri uri = getUri(context);
        if (uri == null) {
            throw new BackendException("SAFBackendService not enabled");
        }
        UriPermission permission = getPermissionFor(getUri(context));
        if (permission == null || !(permission.isReadPermission() && permission.isWritePermission())) {
            throw new BackendException("Root uri not accessible (no permission)");
        }
        mRoot = DocumentFile.fromTreeUri(mContext, permission.getUri());
    }

    private UriPermission getPermissionFor(Uri uri) {
        for (UriPermission permission : mContext.getContentResolver().getPersistedUriPermissions()) {
            if (Objects.equals(permission.getUri(), uri)) {
                return permission;
            }
        }
        return null;
    }

    @Override
    public SAFFile upload(@Nullable SAFFile folder, File file, ProgressInputStream.UploadProgressListener listener) throws BackendException {
        DocumentFile docFile = createDocumentFile(folder, file);
        if (docFile == null) {
            throw new BackendException(
                    String.format("Couldn't create output file %s under %s",
                            file.getName(),
                            folder == null ? "root directory" : folder.getName()
                    )
            );
        }

        try (InputStream in = openUploadInputStream(file, listener);
             OutputStream out = openUploadOutputStream(docFile)) {
            IOUtils.copy(in, out);
        } catch (IOException e) {
            throw new BackendException(
                    String.format("Failed to upload %s to %s", file.getName(), docFile.getUri()),
                    e, true
            );
        }
        return new SAFFile(docFile);
    }

    private OutputStream openUploadOutputStream(DocumentFile docFile) {
        OutputStream out;
        try {
            out = mContext.getContentResolver().openOutputStream(docFile.getUri());
        } catch (FileNotFoundException e) {
            throw new AssertionError("Document file should have been created!");
        }
        return out;
    }

    private InputStream openUploadInputStream(File file, ProgressInputStream.UploadProgressListener listener) throws BackendException {
        InputStream in;
        try {
            in = new ProgressInputStream(file, listener);
        } catch (FileNotFoundException e) {
            throw new BackendException(
                    String.format("File '%s' doesn't exist", file.getName()), e
            );
        } catch (IOException e) {
            throw new BackendException(
                    String.format("Couldn't access file '%s'", file.getName()), e
            );
        }
        return in;
    }

    private DocumentFile createDocumentFile(@Nullable SAFFile folder, File file) throws BackendException {
        DocumentFile docFolder;
        if (folder == null) {
            docFolder = mRoot;
        } else {
            docFolder = DocumentFile.fromTreeUri(mContext, folder.getUri());
            if (docFolder == null) {
                throw new BackendException(
                        String.format("Couldn't open folder %s to create file %s",
                                folder.getUri(), file.getName())
                );
            }
        }
        return docFolder.createFile(getFileType(file), file.getName());
    }

    private String getFileType(File file) {
        String name = file.getName();
        final int lastDot = name.lastIndexOf('.');
        if (lastDot >= 0) {
            final String extension = name.substring(lastDot + 1).toLowerCase();
            final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {
                return mime;
            }
        }

        return "application/octet-stream";
    }

    @Override
    public File download(File folder, @NonNull SAFFile file, ProgressOutputStream.DownloadProgressListener listener) throws BackendException {
        File destination = new File(folder, file.getName());

        try (InputStream in = openDownloadInputStream(file);
             OutputStream out = openDownloadOutputStream(file, destination, listener)) {
            IOUtils.copy(in, out);
        } catch (IOException e) {
            throw new BackendException(
                    String.format("Failed to download %s to %s",
                            file.getName(),
                            destination.getName()
                    ), e, true
            );
        }
        return destination;
    }

    private InputStream openDownloadInputStream(@NonNull SAFFile file) throws BackendException {
        InputStream in;
        try {
            in = mContext.getContentResolver().openInputStream(file.getUri());
        } catch (FileNotFoundException e) {
            throw new BackendException(
                    String.format("Unable to open uri %s", file.getUri()), e, true
            );
        }

        if (in == null) {
            throw new BackendException(
                    String.format("Couldn't open file %s its provider has recently crashed",
                            file.getUri()
                    ), true
            );
        }
        return in;
    }

    private OutputStream openDownloadOutputStream(SAFFile source, File destination, ProgressOutputStream.DownloadProgressListener listener) throws BackendException {
        OutputStream out;
        try {
            out = new ProgressOutputStream(destination, source.getSize(), listener);
        } catch (FileNotFoundException e) {
            throw new BackendException(
                    String.format(
                            "Couldn't open '%s' for writing (might be a folder or inaccessible)",
                            destination.getName()
                    ), e, true
            );
        }
        return out;
    }

    @Override
    public List<IFile> list(@Nullable SAFFile folder) throws BackendException {
        DocumentFile docFolder;
        if (folder == null) {
            docFolder = mRoot;
        } else {
            docFolder = DocumentFile.fromTreeUri(mContext, folder.getUri());
            if (docFolder == null) {
                throw new BackendException(
                    String.format("Couldn't access folder '%s' " +
                            "(maybe this was called on SDK_Version <21)", folder.getUri())
                );
            }
        }
        LinkedList<IFile> folderContents = new LinkedList<>();
        for (DocumentFile file : docFolder.listFiles()) {
            folderContents.add(new SAFFile(file));
        }
        return folderContents;
    }

    @Override
    public SAFFile newFolder(SAFFile parent, String name) throws BackendException {
        DocumentFile docParent = DocumentFile.fromTreeUri(mContext, parent.getUri());
        if (docParent == null) {
            throw new BackendException(
                    String.format("Couldn't access parent '%s'", parent.getUri())
            );
        }
        DocumentFile docFolder = docParent.createDirectory(name);
        if (docFolder == null) {
            throw new BackendException(
                    String.format(
                            "Couldn't create folder '%s' under parent '%s'", name, parent.getUri()
                    )
            );
        }
        return new SAFFile(docFolder);
    }
}
