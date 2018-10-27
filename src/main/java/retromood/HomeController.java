package retromood;

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
import java.util.Arrays;
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

        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder
                    .standard().withRegion("eu-west-1").build();

        try {
            byte[] photoBytes = photo.getBytes();
            ByteBuffer photoBuffer = ByteBuffer.wrap(photoBytes);

            DetectTextRequest request = new DetectTextRequest()
                    .withImage(new Image().withBytes(photoBuffer));

            DetectTextResult result = rekognitionClient.detectText(request);
            List<TextDetection> textDetections = result.getTextDetections();

            List<String> textResults = textDetections.stream()
                    .map(textResult -> textResult.getDetectedText())
                    .collect(Collectors.toList());

            model.addAttribute("textResults", textResults);
        } catch(Exception ex) {
            System.err.println("Something went wrong");
            System.err.println(ex);
        }

        return "index";
    }

}