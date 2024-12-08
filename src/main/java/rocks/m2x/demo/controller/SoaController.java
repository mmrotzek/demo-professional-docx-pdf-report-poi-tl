package rocks.m2x.demo.controller;

import com.deepoove.poi.util.PoitlIOUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rocks.m2x.demo.service.SoaReportService;
import rocks.m2x.demo.service.report.data.SoA;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@RestController
@RequestMapping("/api/soa")
@Slf4j
@RequiredArgsConstructor
public class SoaController {
    final SoaReportService soaReportService;

    @GetMapping("/report")
    public ResponseEntity<Void> renderReport(@RequestParam(value = "download", defaultValue = "true") boolean download,
                                             @RequestParam(value = "format", defaultValue = "docx") String format,
                                             HttpServletResponse response) {
        try {
            if (format.equals("docx")) {
                Pair<SoA, ByteArrayOutputStream> r = soaReportService.renderReport();
                return reportResponse(r.getLeft(), r.getRight(), format, download, response);
            } else {
                Pair<SoA, ByteArrayOutputStream> r = soaReportService.renderReportPdf();
                return reportResponse(r.getLeft(), r.getRight(), format, download, response);
            }

        } catch (Exception e) {
            log.error("Error while rendering report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private static ResponseEntity<Void> reportResponse(SoA instance, ByteArrayOutputStream byteArrayOutputStream, String fileExt, boolean download, HttpServletResponse response) throws IOException {
        String fileName = (instance.isDraft() ? "_DRAFT_" : "") + "SoA_" + instance.getVersion() + "." + fileExt;
        if (fileExt.equals("pdf")) {
            response.setContentType("application/pdf");
        } else if (fileExt.equals("docx")) {
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }

        byte[] fileContent = byteArrayOutputStream.toByteArray();

        // Content-Length
        response.setContentLength(fileContent.length);

        // headers for download
        if (download) {
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        } else {
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + fileName);
        }

        // write  Byte-Arrays to Response-OutputStream
        OutputStream out = response.getOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(out);
        bos.write(fileContent);

        // close resources
        PoitlIOUtils.closeQuietlyMulti(byteArrayOutputStream, bos, out);

        // assure all is written to the client
        response.flushBuffer();

        return ResponseEntity.ok().build();
    }
}
