package retromood;

import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.*;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.TextDetection;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/")
    public String index(@RequestParam("photo") MultipartFile photo, Model model) {

        List<String> textResults = detectText(photo, model);

        detectSentiment("It is raining today in Seattle");

        batchDetectSentiment(Collections.emptyList());
//        batchDetectSentiment(textResults);

        return "index";
    }

    private List<String> detectText(@RequestParam("photo") MultipartFile photo, Model model) {

        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder
                .standard()
                .withRegion("eu-west-1")
                .build();

        List<String> textResults = new ArrayList<>();

        try {
            byte[] photoBytes = photo.getBytes();
            ByteBuffer photoBuffer = ByteBuffer.wrap(photoBytes);

            DetectTextRequest request = new DetectTextRequest()
                    .withImage(new Image().withBytes(photoBuffer));

            DetectTextResult result = rekognitionClient.detectText(request);
            List<TextDetection> textDetections = result.getTextDetections();

            textResults = textDetections.stream()
                    .map(textResult -> textResult.getDetectedText())
                    .collect(Collectors.toList());

            model.addAttribute("textResults", textResults);
        } catch (Exception exception) {
            System.err.println("Something went wrong");
            System.err.println(exception);
        }

        return textResults;
    }

    private void detectSentiment(String text) {

        AmazonComprehend comprehendClient = getAmazonComprehendClient();

        // Call detectSentiment API
        System.out.println("Calling DetectSentiment");

        DetectSentimentRequest detectSentimentRequest = new DetectSentimentRequest().withText(text)
                                                                                    .withLanguageCode("en");

        DetectSentimentResult detectSentimentResult = comprehendClient.detectSentiment(detectSentimentRequest);

        System.out.println(detectSentimentResult);
        System.out.println("End of DetectSentiment\n");

    }

    private void batchDetectSentiment(List<String> textResults) {

        AmazonComprehend comprehendClient = getAmazonComprehendClient();

        List<String> textList = Arrays.asList("I love Seattle", "Today is Sunday", "Tomorrow is Monday", "I hate Seattle");

        BatchDetectSentimentResult batchDetectSentimentResult = batchDetectSentiment(comprehendClient, textList);

        retryDetectSentimentIfAnyFailedEntities(comprehendClient, textList, batchDetectSentimentResult);

        System.out.println("End of DetectEntities");
    }

    private AmazonComprehend getAmazonComprehendClient() {
        return AmazonComprehendClientBuilder
                .standard()
                .withRegion("eu-west-1")
                .build();
    }

    private BatchDetectSentimentResult batchDetectSentiment(AmazonComprehend comprehendClient, List<String> textList) {
        System.out.println("Calling BatchDetectSentiment");

        BatchDetectSentimentResult batchDetectSentimentResult = getBatchDetectSentimentResult(comprehendClient, textList);

        printSentimentResults(batchDetectSentimentResult.getResultList());

        return batchDetectSentimentResult;
    }


    private void retryDetectSentimentIfAnyFailedEntities(AmazonComprehend comprehendClient, List<String> textList, BatchDetectSentimentResult batchDetectSentimentResult) {
        List<BatchItemError> errorList = batchDetectSentimentResult.getErrorList();

        if (errorList.size() != 0) {
            System.out.println("Retrying Failed Requests");

            List<String> textToRetry = new ArrayList<>();

            for (BatchItemError errorItem : errorList) {
                textToRetry.add(textList.get(errorItem.getIndex()));
            }

            BatchDetectSentimentResult retriedBatchDetectSentimentResult = getBatchDetectSentimentResult(comprehendClient, textToRetry);

            printSentimentResults(retriedBatchDetectSentimentResult.getResultList());
        }
    }

    private BatchDetectSentimentResult getBatchDetectSentimentResult(AmazonComprehend comprehendClient, List<String> textList) {

        BatchDetectSentimentRequest batchDetectSentimentRequest = new BatchDetectSentimentRequest()
                .withTextList(textList)
                .withLanguageCode("en");

        return comprehendClient.batchDetectSentiment(batchDetectSentimentRequest);
    }

    private void printSentimentResults(List<BatchDetectSentimentItemResult> batchDetectSentimentItemResultList) {
        for (BatchDetectSentimentItemResult item : batchDetectSentimentItemResultList) {
            System.out.println(item);
        }
    }
}
