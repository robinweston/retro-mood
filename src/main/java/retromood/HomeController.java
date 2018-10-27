package retromood;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model)
    {
        System.out.println("WOOOOOOO");
        model.addAttribute("name", "Laura");
        return "index";
    }

}