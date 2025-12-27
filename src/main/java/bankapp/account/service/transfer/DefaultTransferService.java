package bankapp.account.service.transfer;

import bankapp.account.exceptions.*;
import bankapp.account.model.account.Account;
import bankapp.account.model.transfer.PendingTransfer;
import bankapp.account.repository.PendingTransferRepository;
import bankapp.account.request.account.AccountTransactionRequest;
import bankapp.account.request.transfer.TransferAmountRequest;
import bankapp.account.request.transfer.TransferAuthRequest;
import bankapp.account.request.transfer.TransferMessageRequest;
import bankapp.account.request.transfer.TransferRecipientRequest;
import bankapp.account.response.transfer.PendingTransferResponse;
import bankapp.account.service.account.AccountService;
import bankapp.account.service.check.AccountCheckService;
import bankapp.account.service.transfer.component.*;
import bankapp.core.util.BankNameConverter;
import bankapp.member.exceptions.IncorrectPasswordException;
import bankapp.member.model.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

import static bankapp.account.model.transfer.TransferStatus.*;

@Slf4j
@Service
public class DefaultTransferService implements TransferService {

    private final PendingTransferRepository pendingTransferRepository;
    private final AccountService accountService;
    private final AccountCheckService accountCheckService;
    private final PasswordEncoder passwordEncoder;
    private final TransferRecipientValidator transferRecipientValidator;
    private final TransferRecipientInfoFinder transferRecipientInfoFinder;
    private final TransferAmountValidator transferAmountValidator;
    private final TransferMessageValidator transferMessageValidator;
    private final TransferAuthValidator transferAuthValidator;

    private final long expirationMinutes;

    @Autowired
    public DefaultTransferService(PendingTransferRepository pendingTransferRepository,
                                  AccountService accountService,
                                  AccountCheckService accountCheckService,
                                  PasswordEncoder passwordEncoder,
                                  TransferRecipientValidator transferRecipientValidator,
                                  TransferRecipientInfoFinder transferRecipientInfoFinder,
                                  TransferAmountValidator transferAmountValidator,
                                  TransferMessageValidator transferMessageValidator,
                                  TransferAuthValidator transferAuthValidator,
                                  @Value("${transfer.expiration-minutes}") long expirationMinutes) {
        this.pendingTransferRepository = pendingTransferRepository;
        this.accountService = accountService;
        this.accountCheckService = accountCheckService;
        this.passwordEncoder = passwordEncoder;
        this.transferRecipientValidator = transferRecipientValidator;
        this.transferRecipientInfoFinder = transferRecipientInfoFinder;
        this.transferAmountValidator = transferAmountValidator;
        this.transferMessageValidator = transferMessageValidator;
        this.transferAuthValidator = transferAuthValidator;
        this.expirationMinutes = expirationMinutes;
    }


    @Override
    @Transactional
    public String processRecipient(TransferRecipientRequest transferRecipientRequest, Member loginMember)
            throws ExternalTransferNotSupportedException, RecipientAccountNotFoundException, SameAccountTransferException, PrimaryAccountNotFoundException {

        TransferRecipientValidator.ValidationResult validationResult = transferRecipientValidator.validate(transferRecipientRequest, loginMember);

        String recipientName = transferRecipientInfoFinder.findRecipientName(validationResult.getToAccountNumber());

        PendingTransfer pendingTransfer = initializePendingTransfer(validationResult ,recipientName , loginMember);

        return pendingTransfer.getRequestId();
    }


    @Override
    @Transactional(readOnly = true)
    public PendingTransferResponse getPendingTransferResponse(String requestId) throws PendingTransferNotFoundException, PrimaryAccountNotFoundException {

        PendingTransfer pendingTransfer = pendingTransferRepository.findById(requestId)
                .orElseThrow(() -> new PendingTransferNotFoundException("유효하지 않은 송금 요청입니다. ID: " + requestId));

        Account senderAccount = pendingTransfer.getSenderAccount();

        return PendingTransferResponse.from(pendingTransfer, senderAccount);
    }

    @Override
    @Transactional
    public void processAmount(String requestId, TransferAmountRequest transferAmountRequest) throws InsufficientBalanceException, PendingTransferNotFoundException, InvalidAmountException, IllegalTransferStateException, IllegalStateException {

        PendingTransfer pendingTransfer = transferAmountValidator.validate(requestId,transferAmountRequest);

        applyAmountUpdatePendingTransfer(pendingTransfer, transferAmountRequest);
    }

    @Override
    @Transactional
    public void processMessage(String requestId, TransferMessageRequest transferMessageRequest) throws PendingTransferNotFoundException, IllegalTransferStateException {

       PendingTransfer pendingTransfer = transferMessageValidator.validate(requestId);

       applyMessageUpdatePendingTransfer(pendingTransfer ,  transferMessageRequest);
    }

    @Override
    @Transactional
    public void executeTransfer(String requestId, TransferAuthRequest transferAuthRequest) throws PendingTransferNotFoundException, IllegalTransferStateException, IncorrectPasswordException {

        PendingTransfer pendingTransfer = transferAuthValidator.validate(requestId);

        verifyMemberPassword(pendingTransfer, transferAuthRequest);
        debitFromSender(pendingTransfer);
        creditToReceiver(pendingTransfer);
        transferCompletion(pendingTransfer);
        pendingTransfer.setStatus(PENDING_TRANSFER);
        pendingTransfer.setUpdatedAt(LocalDateTime.now());

    }




    private PendingTransfer initializePendingTransfer(TransferRecipientValidator.ValidationResult result , String recipientName , Member loginMember) {
        PendingTransfer pendingTransfer = new PendingTransfer(loginMember
                ,result.getFromAccount()
                ,BankNameConverter.getBankNameByCode(result.getToBankCode())
                ,result.getToAccountNumber()
                ,recipientName
                ,PENDING_AMOUNT
                ,LocalDateTime.now().plusMinutes(expirationMinutes)
        );

        return pendingTransferRepository.save(pendingTransfer);
    }

    private void applyAmountUpdatePendingTransfer(PendingTransfer pendingTransfer, TransferAmountRequest transferAmountRequest) {
        pendingTransfer.setAmount(transferAmountRequest.getAmount());
        pendingTransfer.setStatus(PENDING_MESSAGE);
        pendingTransfer.setUpdatedAt(LocalDateTime.now());
    }

    private void applyMessageUpdatePendingTransfer(PendingTransfer pendingTransfer, TransferMessageRequest transferMessageRequest) {
        pendingTransfer.setMessage(transferMessageRequest.getMessage());
        pendingTransfer.setStatus(PENDING_AUTH);
        pendingTransfer.setUpdatedAt(LocalDateTime.now());
    }

    private void verifyMemberPassword(PendingTransfer pendingTransfer, TransferAuthRequest transferAuthRequest) throws IncorrectPasswordException{
        Member senderMember = pendingTransfer.getSenderMember();
        if(!passwordEncoder.matches(transferAuthRequest.getPassword(), senderMember.getPassword())) {
            throw new IncorrectPasswordException("비밀번호가 일치하지 않습니다.");
        }
    }

    private void debitFromSender(PendingTransfer pendingTransfer) {
        if(pendingTransfer.getStatus() != PENDING_TRANSFER){
            throw new IllegalTransferStateException("거래 단계가 아닙니다.");
        }

        accountService.debit(new AccountTransactionRequest(
                pendingTransfer.getSenderAccount().getAccountId(),
                pendingTransfer.getAmount(),
                "transfer"));
    }

    private void creditToReceiver(PendingTransfer pendingTransfer) {
        if(pendingTransfer.getStatus() != PENDING_TRANSFER){
            throw new IllegalTransferStateException("거래 단계가 아닙니다.");
        }

        long receiverAccountId = accountCheckService.findAccountByAccountNumber(pendingTransfer.getReceiverAccountNumber()).getAccountId();

        accountService.credit(new AccountTransactionRequest(
                receiverAccountId,
                pendingTransfer.getAmount(),
                "transfer"
        ));
    }

    private void transferCompletion(PendingTransfer pendingTransfer) {
        pendingTransfer.setStatus(COMPLETED);
        pendingTransfer.setUpdatedAt(LocalDateTime.now());
    }

}
