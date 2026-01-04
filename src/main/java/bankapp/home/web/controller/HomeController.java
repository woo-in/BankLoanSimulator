package bankapp.home.web.controller;

import bankapp.account.response.account.AccountResponse;
import bankapp.account.service.check.AccountCheckService;
import bankapp.core.common.SessionConst;
import bankapp.member.model.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;



@Slf4j
@Controller
@RequestMapping("/")
public class HomeController {

    private final AccountCheckService accountCheckService;


    @Autowired
    public HomeController(AccountCheckService accountCheckService) {
        this.accountCheckService = accountCheckService;
    }

    @GetMapping
    public String showHome(@SessionAttribute(name = SessionConst.LOGIN_MEMBER , required = false) Member loginMember,
                           Model model) {

        prepareHomeDetailsViewModel(model, loginMember);
        return "home/home";

    }

    @GetMapping("product")
    public String showProduct(){
        return "home/product-home";
    }

    @GetMapping("management")
    public String showManagement(){
        return "home/management-home";
    }


    private void prepareHomeDetailsViewModel(Model model , Member loginMember){
        AccountResponse accountResponse = AccountResponse.from(accountCheckService.findPrimaryAccountByMember(loginMember));
        model.addAttribute("accountResponse", accountResponse);
    }

}
