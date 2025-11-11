package bankapp.account.web.controller;

import bankapp.account.exceptions.*;
import bankapp.account.request.transfer.TransferAmountRequest;
import bankapp.account.request.transfer.TransferAuthRequest;
import bankapp.account.request.transfer.TransferMessageRequest;
import bankapp.account.request.transfer.TransferRecipientRequest;
import bankapp.account.response.transfer.PendingTransferResponse;
import bankapp.account.service.transfer.TransferService;
import bankapp.core.common.SessionConst;
import bankapp.member.exceptions.IncorrectPasswordException;
import bankapp.member.model.Member;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

// TODO : SpringSecurity 배우고 해결해야 할 문제
// 주어진 경로를 이용하지 않고 URL 로 바로 요청해도 가진다.
// new(get) -> new(post) ->  amount(get) -> amount(post)
// -> message(get) -> message(post) -> auth(get) -> execute(post)

// amount(get) ~ execute(post) : RequestId 있어야 이동가능
// 반대로 말하면 , new(post) 이후 , request id 만 있다면 어디로든 이동은 가능
// 물론 가져도 DB 에서 상태 체크해서 거부 하지만 , access 자체를 원천 봉쇄할 필요 있다.

@Slf4j
@Controller
@RequestMapping("/transfer")
public class TransferController {

    private final TransferService transferService;


    @Autowired
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping("/new")
    public String showRecipientForm(Model model) {
        model.addAttribute("transferRecipientRequest", new TransferRecipientRequest());
        return "account/transfer/transfer-recipient-form";
    }


    /**
     * 송금 1단계: 수취인 계좌 정보 입력 및 유효성 검증을 처리합니다.
     *  그리고 세션에 송금 요청의 고유 id 를 저장합니다.
     *
     * @param transferRecipientRequest 수취인 정보(은행, 계좌번호)를 담은 DTO
     * @return 검증 성공 시 금액 입력 폼으로 리다이렉트, 실패 시 다시 입력(/new) 폼을 보여줌
     */
    @PostMapping("/new")
    public String processRecipientInfo(@Validated @ModelAttribute TransferRecipientRequest transferRecipientRequest,
                                     BindingResult bindingResult,
                                     @SessionAttribute(value = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                                     HttpSession session) {

        if (bindingResult.hasErrors()) {
            return "account/transfer/transfer-recipient-form";
        }

        String requestId ;
        try{
            requestId = transferService.processRecipient(transferRecipientRequest, loginMember);
            session.setAttribute("requestId", requestId);
            return "redirect:/transfer/amount";
        }catch (ExternalTransferNotSupportedException e){
            bindingResult.rejectValue("toBankCode", "Unavailable", "타행 이체는 현재 서비스 준비 중입니다.");
        }catch (RecipientAccountNotFoundException e){
            bindingResult.rejectValue("toAccountNumber", "notFound", "해당 계좌를 찾을 수 없습니다.");
        }catch(SameAccountTransferException e){
            bindingResult.rejectValue("toAccountNumber", "Same", "동일한 계좌로는 송금할 수 없습니다.");
        }

        return "account/transfer/transfer-recipient-form";
    }


    @GetMapping("/amount")
    public String showAmountForm(Model model,
                                 @SessionAttribute(value = "requestId" , required = false) String requestId) {


        prepareTransferDetailsViewModel(model , requestId);
        model.addAttribute("transferAmountRequest", new TransferAmountRequest());

        return "account/transfer/transfer-amount-form";
    }


    /**
     * 송금 2단계: 금액 입력 및 유효성 검증을 처리합니다.
     *
     * @param transferAmountRequest 송금액 정보를 담은 DTO
     * @return 검증 성공 시 금액 메시지 폼으로 리다이렉트, 실패 시 다시 입력(/amount)폼을 보여줌
     */
    @PostMapping("/amount")
    public String processAmountInfo(Model model,
                                    @SessionAttribute(value = "requestId" , required = false) String requestId,
                                    @Validated @ModelAttribute TransferAmountRequest transferAmountRequest,
                                    BindingResult bindingResult) {


        if (bindingResult.hasErrors()) {
            prepareTransferDetailsViewModel(model , requestId);
            return "account/transfer/transfer-amount-form";
        }


        try{
            transferService.processAmount(requestId, transferAmountRequest);
            return "redirect:/transfer/message";
        }catch (InsufficientBalanceException e){
            bindingResult.rejectValue("amount", "invalid", "잔액이 부족합니다.");
            prepareTransferDetailsViewModel(model , requestId);
        }

        return "account/transfer/transfer-amount-form";

    }

    @GetMapping("/message")
    public String showMessageForm(Model model,
                                  @SessionAttribute(value = "requestId" , required = false) String requestId){
        prepareTransferDetailsViewModel(model , requestId);
        return "account/transfer/transfer-message-form";
    }


    /**
     * 송금 3단계: 메시지 입력 및 유효성 검증을 처리합니다.
     *
     * @param transferMessageRequest 메시지 정보를 담은 DTO
     * @return 검증 성공 시 인증 폼으로 리다이렉트, 실패 시 다시 입력(/message)폼을 보여줌
     */
    @PostMapping("/message")
    public String processMessageInfo(@SessionAttribute(value = "requestId" , required = false) String requestId,
                                     @Validated @ModelAttribute TransferMessageRequest transferMessageRequest,
                                     BindingResult bindingResult,
                                     Model model){

        if (bindingResult.hasErrors()) {
            prepareTransferDetailsViewModel(model , requestId);
            return "account/transfer/transfer-message-form";
        }

        transferService.processMessage(requestId, transferMessageRequest);
        return "redirect:/transfer/auth";
    }



    @GetMapping("auth")
    public String showAuthForm(Model model) {
        model.addAttribute("transferAuthRequest", new TransferAuthRequest());
        return "account/transfer/transfer-auth-form";
    }


    /**
     * 송금 4단계: 비밀번호 확인 및 송금
     *
     * @param transferAuthRequest 비밀번호
     * @return 검증 성공 시 송금 완료 폼으로 리다이렉트, 실패 시 다시 입력(/auth)폼을 보여줌
     */
    @PostMapping("execute")
    public String executeTransfer(@SessionAttribute(value = "requestId" , required = false) String requestId,
                                  @Validated @ModelAttribute TransferAuthRequest transferAuthRequest,
                                  BindingResult bindingResult,
                                  Model model){

        if (bindingResult.hasErrors()) {
            return "account/transfer/transfer-auth-form";
        }


        try {
            transferService.executeTransfer(requestId, transferAuthRequest);
            prepareTransferDetailsViewModel(model , requestId);
            return "account/transfer/transfer-complete-form";
        }catch(IncorrectPasswordException e){
            bindingResult.rejectValue("password", "invalid", "비밀번호가 일치하지 않습니다.");
        }

        return "account/transfer/transfer-auth-form";

    }




    // TODO: 완료후 , 새로고침 , 뒤로가기시 , 이상 행동
    // TODO : *** 실패 기록 테이블 필요함.
    // ** (모든 걸 기록 가능 , UX 개선 , 이상탐지 (비밀번호 틀림)) **



    private void prepareTransferDetailsViewModel(Model model , String requestId){
        PendingTransferResponse pendingTransferResponse = transferService.getPendingTransferResponse(requestId);
        model.addAttribute("pendingTransferResponse", pendingTransferResponse);
    }





}
