package retromood;

import com.amazonaws.services.comprehend.AmazonComprehend;
import com.amazonaws.services.comprehend.AmazonComprehendClientBuilder;
import com.amazonaws.services.comprehend.model.DetectSentimentRequest;
import com.amazonaws.services.comprehend.model.DetectSentimentResult;
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

        detectText(photo, model);

        detectSentiment("It is raining today in Seattle");
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
        } catch(Exception ex) {
            System.err.println("Something went wrong");
            System.err.println(ex);
        }

        return textResults;
    }

    private void detectSentiment(String text) {

        AmazonComprehend comprehendClient = AmazonComprehendClientBuilder
                .standard()
                .withRegion("eu-west-1")
                .build();

        // Call detectSentiment API
        System.out.println("Calling DetectSentiment");

        DetectSentimentRequest detectSentimentRequest = new DetectSentimentRequest().withText(text)
                .withLanguageCode("en");
        DetectSentimentResult detectSentimentResult = comprehendClient.detectSentiment(detectSentimentRequest);

        System.out.println(detectSentimentResult);
        System.out.println("End of DetectSentiment\n");
        System.out.println( "Done" );

    }

}