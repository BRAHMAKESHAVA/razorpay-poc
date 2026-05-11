package org.backend.documentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xwpf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

public class ApiContractGenerator {

    private static final String SWAGGER_URL =
            "http://localhost:8080/v3/api-docs";

    private static final String OUTPUT_FOLDER =
            "generated-docs";

    private static final String OUTPUT_FILE =
            OUTPUT_FOLDER + "/API_Contract_Documentation.docx";

    public static void main(String[] args) {

        try {

            createOutputFolder();

            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(
                    new URL(SWAGGER_URL)
            );

            JsonNode paths = root.get("paths");

            XWPFDocument document =
                    new XWPFDocument();

            createTitle(document);

            Iterator<Map.Entry<String, JsonNode>>
                    pathIterator = paths.fields();

            int apiCounter = 1;

            while (pathIterator.hasNext()) {

                Map.Entry<String, JsonNode>
                        pathEntry = pathIterator.next();

                String endpoint =
                        pathEntry.getKey();

                JsonNode methods =
                        pathEntry.getValue();

                Iterator<Map.Entry<String, JsonNode>>
                        methodIterator =
                        methods.fields();

                while (methodIterator.hasNext()) {

                    Map.Entry<String, JsonNode>
                            methodEntry =
                            methodIterator.next();

                    String method =
                            methodEntry.getKey()
                                    .toUpperCase();

                    JsonNode apiDetails =
                            methodEntry.getValue();

                    String summary =
                            getValue(apiDetails,
                                    "summary");

                    String description =
                            getValue(apiDetails,
                                    "description");

                    addHeading(
                            document,
                            apiCounter + ". "
                                    + summary
                    );

                    addNormalText(
                            document,
                            "Method: " + method
                    );

                    addNormalText(
                            document,
                            "Endpoint: " + endpoint
                    );

                    addSubHeading(
                            document,
                            "Description"
                    );

                    addDescription(
                            document,
                            description
                    );

                    JsonNode requestBody =
                            apiDetails.get(
                                    "requestBody"
                            );

                    if (requestBody != null) {

                        addSubHeading(
                                document,
                                "Request Body"
                        );

                        addNormalText(
                                document,
                                "Request body available"
                        );
                    }

                    JsonNode responses =
                            apiDetails.get(
                                    "responses"
                            );

                    if (responses != null) {

                        addSubHeading(
                                document,
                                "Response Codes"
                        );

                        Iterator<String>
                                responseCodes =
                                responses.fieldNames();

                        while (
                                responseCodes.hasNext()
                        ) {

                            String code =
                                    responseCodes.next();

                            addBullet(
                                    document,
                                    code
                            );
                        }
                    }

                    addSeparator(document);

                    apiCounter++;
                }
            }

            FileOutputStream out =
                    new FileOutputStream(
                            OUTPUT_FILE
                    );

            document.write(out);

            out.close();

            document.close();

            System.out.println(
                    "DOCX Generated Successfully"
            );

            System.out.println(
                    "Location: " + OUTPUT_FILE
            );

        } catch (Exception e) {

            e.printStackTrace();

            System.out.println(
                    "Failed to generate document"
            );
        }
    }

    private static void createOutputFolder() {

        File folder =
                new File(OUTPUT_FOLDER);

        if (!folder.exists()) {

            folder.mkdirs();
        }
    }

    private static String getValue(
            JsonNode node,
            String field
    ) {

        return node.has(field)
                ? node.get(field).asText()
                : "N/A";
    }

    private static void createTitle(
            XWPFDocument document
    ) {

        XWPFParagraph paragraph =
                document.createParagraph();

        paragraph.setAlignment(
                ParagraphAlignment.CENTER
        );

        XWPFRun run =
                paragraph.createRun();

        run.setText(
                "API Contract Documentation"
        );

        run.setBold(true);

        run.setFontFamily(
                "Times New Roman"
        );

        run.setFontSize(22);
    }

    private static void addHeading(
            XWPFDocument document,
            String text
    ) {

        XWPFParagraph paragraph =
                document.createParagraph();

        XWPFRun run =
                paragraph.createRun();

        run.setBold(true);

        run.setFontFamily(
                "Times New Roman"
        );

        run.setFontSize(16);

        run.setText(text);
    }

    private static void addSubHeading(
            XWPFDocument document,
            String text
    ) {

        XWPFParagraph paragraph =
                document.createParagraph();

        XWPFRun run =
                paragraph.createRun();

        run.setBold(true);

        run.setFontFamily(
                "Times New Roman"
        );

        run.setFontSize(13);

        run.setText(text);
    }

    private static void addNormalText(
            XWPFDocument document,
            String text
    ) {

        XWPFParagraph paragraph =
                document.createParagraph();

        XWPFRun run =
                paragraph.createRun();

        run.setFontFamily(
                "Times New Roman"
        );

        run.setFontSize(12);

        run.setText(text);
    }

    private static void addDescription(
            XWPFDocument document,
            String text
    ) {

        XWPFParagraph paragraph =
                document.createParagraph();

        paragraph.setIndentationLeft(300);

        XWPFRun run =
                paragraph.createRun();

        run.setFontFamily(
                "Times New Roman"
        );

        run.setFontSize(12);

        run.setText(text);
    }

    private static void addBullet(
            XWPFDocument document,
            String text
    ) {

        XWPFParagraph paragraph =
                document.createParagraph();

        paragraph.setStyle("ListBullet");

        XWPFRun run =
                paragraph.createRun();

        run.setFontFamily(
                "Times New Roman"
        );

        run.setFontSize(12);

        run.setText(text);
    }

    private static void addSeparator(
            XWPFDocument document
    ) {

        XWPFParagraph paragraph =
                document.createParagraph();

        XWPFRun run =
                paragraph.createRun();

        run.setText(
                "__________________________________________________"
        );
    }
}