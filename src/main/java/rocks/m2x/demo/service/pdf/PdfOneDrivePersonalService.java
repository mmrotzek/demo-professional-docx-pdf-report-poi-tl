package rocks.m2x.demo.service.pdf;

import com.microsoft.graph.beta.drives.item.items.item.createuploadsession.CreateUploadSessionPostRequestBody;
import com.microsoft.graph.beta.models.DriveItem;
import com.microsoft.graph.beta.models.DriveItemCollectionResponse;
import com.microsoft.graph.beta.models.DriveItemUploadableProperties;
import com.microsoft.graph.beta.models.UploadSession;
import com.microsoft.graph.beta.models.odataerrors.ODataError;
import com.microsoft.graph.beta.serviceclient.GraphServiceClient;
import com.microsoft.graph.core.models.IProgressCallback;
import com.microsoft.graph.core.models.UploadResult;
import com.microsoft.graph.core.tasks.LargeFileUploadTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rocks.m2x.demo.config.ApplicationConfigurationProperties;
import rocks.m2x.demo.service.exc.GraphApiException;
import rocks.m2x.demo.service.exc.PdfConversionException;
import rocks.m2x.demo.service.msgraph.Ms365Client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfOneDrivePersonalService implements ConverterService {

    final Ms365Client ms365Client;
    private GraphServiceClient graphClient;
    private String userPrincipalNameOrId;

    @Override
    public ByteArrayOutputStream convert(byte[] docxData, ApplicationConfigurationProperties.PdfConversionConfig config) throws PdfConversionException {
        ApplicationConfigurationProperties.PdfConversionConfig.PdfConversionGraphApiConfig graphApi = config.getGraphApi();
        Objects.requireNonNull(graphApi, "config.export.pdf-conversion.graph-api is required");

        // ugly, but for demo case okay
        this.graphClient = ms365Client.getGraphServiceClient(graphApi);
        this.userPrincipalNameOrId = graphApi.getUserPrincipalNameOrId();

        try {
            String fileName = uniqueFileName();
            String itemId = uploadDocx(new ByteArrayInputStream(docxData), docxData.length, fileName);
            try (InputStream inputStream = downloadAsPdf(itemId)) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                outputStream.write(inputStream.readAllBytes());

                if (config.getGraphApi().isDeleteAfterConversion()) {
                    deleteFileInDrive(itemId);
                } else {
                    log.warn("Not deleting DOCX in OneDrive. You'll need to do it manually! filename={}", fileName);
                }

                return outputStream;
            }

        } catch (Exception e) {
            throw new PdfConversionException("Error converting document to PDF", e);
        }
    }

    private String uploadDocx(InputStream inputStream, Integer size, String fileName) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InterruptedException {
        Long fileSize = Long.valueOf(size);
        log.debug("Uploading DOCX. {} ({} bytes)", fileName, fileSize);

        // Set the DriveItemUploadableProperties
        // This is used to populate the request to create an upload session
        DriveItemUploadableProperties driveItemUploadableProperties = new DriveItemUploadableProperties();
        driveItemUploadableProperties.setName(fileName);
        driveItemUploadableProperties.setFileSize(fileSize);
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("@microsoft.graph.conflictBehavior", "replace");
        driveItemUploadableProperties.setAdditionalData(additionalData);

        // Finish setting up the request body
        CreateUploadSessionPostRequestBody uploadSessionPostRequestBody = new CreateUploadSessionPostRequestBody();
        uploadSessionPostRequestBody.setItem(driveItemUploadableProperties);

        DriveItem folderItem = graphClient.drives().byDriveId(getDriveId()).root().get();
        UploadSession uploadSession = graphClient.drives().byDriveId(getDriveId())
                .items().byDriveItemId(folderItem.getId())
                .createUploadSession().post(uploadSessionPostRequestBody);

        // Create the large file upload task
        LargeFileUploadTask<DriveItem> uploadTask = new LargeFileUploadTask<>(
                graphClient.getRequestAdapter(), uploadSession, inputStream, fileSize,
                DriveItem::createFromDiscriminatorValue);

        // Create a callback used by the upload provider
        IProgressCallback callback = new IProgressCallback() {
            @Override
            public void report(long current, long max) {
                log.debug("Uploaded {} bytes of {} total bytes", current, max);
            }
        };

        // Do the upload
        UploadResult<DriveItem> uploadResult = uploadTask.upload(0, callback);
        if (uploadResult.isUploadSuccessful()) {
            log.debug("Upload complete");
            return uploadResult.itemResponse.getId();
        } else {
            throw new IOException("Upload to OneDrive failed");
        }
    }

    public InputStream downloadAsPdf(String itemId) throws PdfConversionException {
        log.debug("Downloading as PDF. driveId={}", itemId);

        try {
            return graphClient.drives().byDriveId(getDriveId()).items().byDriveItemId(itemId).content().get(rc -> {
                Objects.requireNonNull(rc.queryParameters).format = "pdf";
            });

        } catch (ODataError e) {
            String errorIdentifier = e.getError() != null ? e.getError().getCode() : "Unknown";
            throw new GraphApiException(errorIdentifier, e.getMessage(), e.getResponseStatusCode(), e);
        } catch (Exception e) {
            // handle alle other runtime exceptions and wrap them in a PdfConversionException
            throw new PdfConversionException(MessageFormat.format("Downloading DriveItem as PDF failed. driveId={0} ", itemId), e);
        }
    }

    private void deleteFileInDrive(String itemId) {
        log.debug("Deleting DOCX. driveId={}", itemId);
        try {
            graphClient.drives().byDriveId(getDriveId()).items().byDriveItemId(itemId).delete();
        } catch (ODataError e) {
            String errorIdentifier = e.getError() != null ? e.getError().getCode() : "Unknown";
            throw new GraphApiException(errorIdentifier, e.getMessage(), e.getResponseStatusCode(), e);
        }
    }

    private String getDriveId() {
        log.debug("Getting DriveId for user={}", userPrincipalNameOrId);
        try {
            return Objects.requireNonNull(graphClient.users().byUserId(userPrincipalNameOrId).drive().get()).getId();
        } catch (ODataError e) {
            String errorIdentifier = e.getError() != null ? e.getError().getCode() : "Unknown";
            throw new GraphApiException(errorIdentifier, e.getMessage(), e.getResponseStatusCode(), e);
        }
    }

    private String uniqueFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss_SSS");
        return String.format("%s_%s.docx", sdf.format(new Date()), Double.valueOf(Math.random()).intValue());
    }


}
